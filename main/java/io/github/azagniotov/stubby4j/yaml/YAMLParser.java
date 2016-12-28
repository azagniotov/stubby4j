/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.yaml.snakeyaml.Yaml;
import parser.yaml.SnakeYaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedArrayList;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.utils.FileUtils.constructInputStream;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.FileUtils.uriToFile;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase64;
import static io.github.azagniotov.stubby4j.utils.StringUtils.objectToString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.trimIfSet;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BASIC;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BEARER;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.CUSTOM;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YAMLParser {

    private final AtomicInteger unmarshalledStubCounter = new AtomicInteger();

    static final String FAILED_TO_LOAD_FILE_ERR = "Failed to retrieveLoadedStubs response content using relative path specified in 'file'. Check that response content exists in relative path specified in 'file'";
    private String dataConfigHomeDirectory;
    private final static Yaml SNAKE_YAML = SnakeYaml.INSTANCE.getSnakeYaml();

    @CoberturaIgnore
    public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final String configContent) throws Exception {
        return parse(dataConfigHomeDirectory, constructInputStream(configContent));
    }

    @CoberturaIgnore
    public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final File configFile) throws Exception {
        return parse(dataConfigHomeDirectory, constructInputStream(configFile));
    }

    private List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final InputStream configAsStream) throws Exception {
        this.dataConfigHomeDirectory = dataConfigHomeDirectory;

        final Object loadedConfig = SNAKE_YAML.load(configAsStream);
        if (!(loadedConfig instanceof List)) {
            throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
        }

        final List<StubHttpLifecycle> stubs = new LinkedList<>();
        final List<Map> httpMessageConfigs = asCheckedArrayList(loadedConfig, Map.class);

        for (final Map rawHttpMessageConfig : httpMessageConfigs) {
            final Map<String, Object> httpMessageConfig = asCheckedLinkedHashMap(rawHttpMessageConfig, String.class, Object.class);
            stubs.add(unmarshallHttpMessageConfigToStub(httpMessageConfig));
        }

        return stubs;
    }

    private StubHttpLifecycle unmarshallHttpMessageConfigToStub(final Map<String, Object> httpMessageConfig) throws Exception {

        final StubHttpLifecycle stub = new StubHttpLifecycle();
        for (final Map.Entry<String, Object> httpType : httpMessageConfig.entrySet()) {

            if (httpType.getValue() instanceof Map) {
                unmarshallMapProperties(stub, httpType);
            } else if (httpType.getValue() instanceof List) {
                unmarshallStubResponseList(stub, httpType);
            }
        }

        stub.setCompleteYAML(marshallHttpMessage(httpMessageConfig));
        stub.setRequestAsYAML(marshallHttpType(httpMessageConfig, YamlProperties.REQUEST));
        stub.setResponseAsYAML(marshallHttpType(httpMessageConfig, YamlProperties.RESPONSE));
        stub.setResourceId(unmarshalledStubCounter.getAndIncrement());

        return stub;
    }

    private void unmarshallMapProperties(final StubHttpLifecycle stub, final Map.Entry<String, Object> httpTypeConfig) throws Exception {
        final Map<String, Object> httpTypeProperties = asCheckedLinkedHashMap(httpTypeConfig.getValue(), String.class, Object.class);

        if (httpTypeConfig.getKey().equals(YamlProperties.REQUEST)) {
            final StubRequest requestStub = buildStubFromHttpTypeProperties(httpTypeProperties, new StubRequestBuilder());

            requestStub.computeRegexPatterns();

            stub.setRequest(requestStub);
            ConsoleUtils.logUnmarshalledStubRequest(requestStub.getMethod(), requestStub.getUrl());

        } else {
            final StubResponse responseStub = buildStubFromHttpTypeProperties(httpTypeProperties, new StubResponseBuilder());
            stub.setResponse(responseStub);
        }
    }


    private <T, B extends StubBuilder<T>> T buildStubFromHttpTypeProperties(final Map<String, Object> httpTypeProperties, final B stubTypeBuilder) throws Exception {

        for (final Map.Entry<String, Object> propertyPair : httpTypeProperties.entrySet()) {

            final Object rawFieldName = propertyPair.getValue();
            final String stageableFieldName = propertyPair.getKey();
            final Object stageableFieldValue;

            if (rawFieldName instanceof List) {
                stageableFieldValue = rawFieldName;

            } else if (rawFieldName instanceof Map) {
                final Map<String, String> rawHeaders = asCheckedLinkedHashMap(rawFieldName, String.class, String.class);
                stageableFieldValue = configureAuthorizationHeader(rawHeaders);

            } else if (stageableFieldName.toLowerCase().equals(YamlProperties.METHOD)) {

                final ArrayList<String> methods = new ArrayList<>(1);
                methods.add(objectToString(rawFieldName));
                stageableFieldValue = methods;

            } else if (isConfigPropertyNamedFile(stageableFieldName)) {
                stageableFieldValue = loadFileContentFromFileUrl(rawFieldName);
            } else {
                stageableFieldValue = objectToString(rawFieldName);
            }
            stubTypeBuilder.stage(stageableFieldName, stageableFieldValue);
        }

        return stubTypeBuilder.build();
    }

    private void unmarshallStubResponseList(final StubHttpLifecycle stub, final Map.Entry<String, Object> httpTypeConfig) throws Exception {
        final List<Map> responseProperties = asCheckedArrayList(httpTypeConfig.getValue(), Map.class);
        stub.setResponse(buildStubResponseList(responseProperties, new StubResponseBuilder()));
    }

    private List<StubResponse> buildStubResponseList(final List<Map> responseProperties, final StubResponseBuilder stubResponseBuilder) throws Exception {
        final List<StubResponse> stubResponses = new LinkedList<>();

        for (final Map rawPropertyPairs : responseProperties) {
            final Map<String, Object> propertyPairs = asCheckedLinkedHashMap(rawPropertyPairs, String.class, Object.class);
            for (final Map.Entry<String, Object> propertyPair : propertyPairs.entrySet()) {
                final String stageableFieldName = propertyPair.getKey();
                Object stageableFieldValue = propertyPair.getValue();
                if (isConfigPropertyNamedFile(stageableFieldName)) {
                    stageableFieldValue = loadFileContentFromFileUrl(stageableFieldValue);
                }
                stubResponseBuilder.stage(stageableFieldName, stageableFieldValue);
            }

            stubResponses.add(stubResponseBuilder.build());
        }

        return stubResponses;
    }

    private boolean isConfigPropertyNamedFile(final String pairKey) {
        return pairKey.toLowerCase().equals(YamlProperties.FILE);
    }

    private Object loadFileContentFromFileUrl(final Object configPropertyNamedFile) throws IOException {
        final String filePath = objectToString(configPropertyNamedFile);
        try {
            if (isFilePathContainTemplateTokens(new File(filePath))) {
                return new File(dataConfigHomeDirectory, filePath);
            }

            return uriToFile(dataConfigHomeDirectory, filePath);
        } catch (final IOException ex) {
            ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
        }

        return null;
    }

    private String marshallHttpMessage(final Map<String, Object> httpMessageConfig) {
        final List<Map<String, Object>> root = new ArrayList<Map<String, Object>>() {{
            add(httpMessageConfig);
        }};

        return SNAKE_YAML.dumpAs(root, null, FlowStyle.BLOCK);
    }

    private String marshallHttpType(final Map<String, Object> httpMessageConfig, final String httpTypeName) {
        final Map<String, Object> httpType = new HashMap<String, Object>() {{
            put(httpTypeName, httpMessageConfig.get(httpTypeName));
        }};

        return SNAKE_YAML.dumpAs(httpType, null, FlowStyle.BLOCK);
    }

    private Map<String, String> configureAuthorizationHeader(final Map<String, String> rawHeaders) {

        final Map<String, String> headers = new LinkedHashMap<>();

        for (final Map.Entry<String, String> entry : rawHeaders.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());

            if (headers.containsKey(BASIC.asYamlProp())) {
                final String headerValue = headers.get(BASIC.asYamlProp());
                final String authorizationHeader = trimIfSet(headerValue);
                final String encodedAuthorizationHeader = String.format("%s %s", BASIC.asString(), encodeBase64(authorizationHeader));
                headers.put(BASIC.asYamlProp(), encodedAuthorizationHeader);

            } else if (headers.containsKey(BEARER.asYamlProp())) {
                final String headerValue = headers.get(BEARER.asYamlProp());
                headers.put(BEARER.asYamlProp(), String.format("%s %s", BEARER.asString(), trimIfSet(headerValue)));

            } else if (headers.containsKey(CUSTOM.asYamlProp())) {
                final String headerValue = headers.get(CUSTOM.asYamlProp());
                headers.put(CUSTOM.asYamlProp(), trimIfSet(headerValue));
            }
        }

        return headers;
    }
}

