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
import io.github.azagniotov.stubby4j.utils.StringUtils;
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

import static io.github.azagniotov.stubby4j.utils.FileUtils.constructInputStream;
import static io.github.azagniotov.stubby4j.utils.FileUtils.doesFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.FileUtils.uriToFile;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase64;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BASIC;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BEARER;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.CUSTOM;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YAMLParser {

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
        for (final Object rawHttpMessageConfig : (List) loadedConfig) {
            final Map httpMessageConfig = (Map) rawHttpMessageConfig;
            final StubHttpLifecycle stub = unmarshallHttpMessageConfigToStub(httpMessageConfig);
            stubs.add(stub);
            stub.setResourceId(stubs.size() - 1);
        }

        return stubs;
    }

    private StubHttpLifecycle unmarshallHttpMessageConfigToStub(final Map httpMessageConfig) throws Exception {

        final StubHttpLifecycle stub = new StubHttpLifecycle();
        for (final Object rawHttpType : httpMessageConfig.entrySet()) {

            final Map.Entry httpType = (Map.Entry) rawHttpType;
            final Object rawHttpTypeProperties = httpType.getValue();

            if (rawHttpTypeProperties instanceof Map) {
                unmarshallMapProperties(stub, httpType);

            } else if (rawHttpTypeProperties instanceof List) {
                unmarshallStubResponseList(stub, httpType);
            }
        }

        stub.setCompleteYAML(marshallHttpMessage(httpMessageConfig));
        stub.setRequestAsYAML(marshallHttpType(httpMessageConfig, YamlProperties.REQUEST));
        stub.setResponseAsYAML(marshallHttpType(httpMessageConfig, YamlProperties.RESPONSE));

        return stub;
    }

    private void unmarshallMapProperties(final StubHttpLifecycle stub, final Map.Entry httpTypeConfig) throws Exception {

        final Map httpTypeProperties = (Map) httpTypeConfig.getValue();
        if (httpTypeConfig.getKey().toString().equals(YamlProperties.REQUEST)) {
            final StubRequest requestStub = buildStubFromHttpTypeProperties(httpTypeProperties, new StubRequestBuilder());

            requestStub.computeRegexPatterns();

            stub.setRequest(requestStub);
            ConsoleUtils.logUnmarshalledStubRequest(requestStub.getMethod(), requestStub.getUrl());

        } else {
            final StubResponse responseStub = buildStubFromHttpTypeProperties(httpTypeProperties, new StubResponseBuilder());
            stub.setResponse(responseStub);
        }
    }


    private <T, B extends StubBuilder<T>> T buildStubFromHttpTypeProperties(final Map httpTypeProperties, final B stubTypeBuilder) throws Exception {

        for (final Object rawPropertyPair : httpTypeProperties.entrySet()) {

            final Map.Entry propertyPair = (Map.Entry) rawPropertyPair;

            final Object rawFieldName = propertyPair.getValue();
            final String stageableFieldName = propertyPair.getKey().toString();
            final Object stageableFieldValue;

            if (rawFieldName instanceof List) {
                stageableFieldValue = rawFieldName;

            } else if (rawFieldName instanceof Map) {
                stageableFieldValue = configureAuthorizationHeader((Map) rawFieldName);

            } else if (stageableFieldName.toLowerCase().equals(YamlProperties.METHOD)) {

                final ArrayList<String> methods = new ArrayList<>(1);
                methods.add(StringUtils.objectToString(rawFieldName));
                stageableFieldValue = methods;

            } else if (isConfigPropertyNamedFile(stageableFieldName)) {
                stageableFieldValue = loadFileContentFromFileUrl(rawFieldName);
            } else {
                stageableFieldValue = StringUtils.objectToString(rawFieldName);
            }
            stubTypeBuilder.stage(stageableFieldName, stageableFieldValue);
        }

        return stubTypeBuilder.build();
    }

    private void unmarshallStubResponseList(final StubHttpLifecycle stub, final Map.Entry httpTypeConfig) throws Exception {
        final List responseProperties = (List) httpTypeConfig.getValue();
        stub.setResponse(buildStubResponseList(responseProperties, new StubResponseBuilder()));
    }

    private List<StubResponse> buildStubResponseList(final List responseProperties, final StubResponseBuilder stubResponseBuilder) throws Exception {
        final List<StubResponse> stubResponses = new LinkedList<>();

        for (final Object rawResponseProperty : responseProperties) {
            final Map rawPropertyPairs = (Map) rawResponseProperty;

            for (final Object rawPropertyPair : rawPropertyPairs.entrySet()) {
                final Map.Entry propertyPair = (Map.Entry) rawPropertyPair;
                final String stageableFieldName = propertyPair.getKey().toString();
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
        final String filePath = StringUtils.objectToString(configPropertyNamedFile);
        try {
            if (doesFilePathContainTemplateTokens(new File(filePath))) {
                return new File(dataConfigHomeDirectory, filePath);
            }

            return uriToFile(dataConfigHomeDirectory, filePath);
        } catch (final IOException ex) {
            ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
        }

        return null;
    }

    private String marshallHttpMessage(final Map httpMessageConfig) {
        final List<Map<?, ?>> root = new ArrayList<Map<?, ?>>() {{
            add(httpMessageConfig);
        }};

        return SNAKE_YAML.dumpAs(root, null, FlowStyle.BLOCK);
    }

    private String marshallHttpType(final Map httpMessageConfig, final String httpTypeName) {
        final Map<String, Object> httpType = new HashMap<String, Object>() {{
            put(httpTypeName, httpMessageConfig.get(httpTypeName));
        }};

        return SNAKE_YAML.dumpAs(httpType, null, FlowStyle.BLOCK);
    }

    private Map<String, String> configureAuthorizationHeader(final Map rawPairValue) {

        final Map<String, String> headers = new LinkedHashMap<>();

        for (final Object rawMapEntry : rawPairValue.entrySet()) {
            final Map.Entry mapEntry = (Map.Entry) rawMapEntry;

            headers.put(mapEntry.getKey().toString(), mapEntry.getValue().toString());

            if (headers.containsKey(BASIC.asYamlProp())) {
                final String rawHeader = headers.get(BASIC.asYamlProp());
                final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
                final String encodedAuthorizationHeader = String.format("%s %s", BASIC.asString(), encodeBase64(authorizationHeader));
                headers.put(BASIC.asYamlProp(), encodedAuthorizationHeader);

            } else if (headers.containsKey(BEARER.asYamlProp())) {
                final String rawHeader = headers.get(BEARER.asYamlProp());
                final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
                headers.put(BEARER.asYamlProp(), String.format("%s %s", BEARER.asString(), authorizationHeader));

            } else if (headers.containsKey(CUSTOM.asYamlProp())) {
                final String rawHeader = headers.get(CUSTOM.asYamlProp());
                final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
                headers.put(CUSTOM.asYamlProp(), authorizationHeader);
            }
        }

        return headers;
    }
}

