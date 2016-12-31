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
import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import org.yaml.snakeyaml.Yaml;

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
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.FileUtils.constructInputStream;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.FileUtils.uriToFile;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase64;
import static io.github.azagniotov.stubby4j.utils.StringUtils.objectToString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.trimIfSet;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.METHOD;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.REQUEST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.isUnknownProperty;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ofNullableProperty;
import static java.util.Optional.ofNullable;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YAMLParser {

    static final String FAILED_TO_LOAD_FILE_ERR = "Failed to retrieveLoadedStubs response content using relative path specified in 'file'. Check that response content exists in relative path specified in 'file'";
    private final static Yaml SNAKE_YAML = SnakeYaml.INSTANCE.getSnakeYaml();
    private final AtomicInteger parsedStubCounter = new AtomicInteger();
    private String dataConfigHomeDirectory;

    @CoberturaIgnore
    public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final String configContent) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configContent));
    }

    @CoberturaIgnore
    public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final File configFile) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configFile));
    }

    private List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final InputStream configAsStream) throws IOException {
        this.dataConfigHomeDirectory = dataConfigHomeDirectory;

        final Object loadedConfig = SNAKE_YAML.load(configAsStream);
        if (!(loadedConfig instanceof List)) {
            throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
        }

        final List<StubHttpLifecycle> stubs = new LinkedList<>();
        final List<Map> httpMessageConfigs = asCheckedArrayList(loadedConfig, Map.class);

        for (final Map rawHttpMessageConfig : httpMessageConfigs) {
            final Map<String, Object> httpMessageConfig = asCheckedLinkedHashMap(rawHttpMessageConfig, String.class, Object.class);
            stubs.add(parseConfigToStubHttpLifecycle(httpMessageConfig));
        }

        return stubs;
    }

    private StubHttpLifecycle parseConfigToStubHttpLifecycle(final Map<String, Object> httpMessageConfig) throws IOException {

        final StubHttpLifecycle.Builder stubBuilder = new StubHttpLifecycle.Builder();
        for (final Map.Entry<String, Object> httpType : httpMessageConfig.entrySet()) {

            if (httpType.getValue() instanceof Map) {
                parseMapProperties(stubBuilder, httpType);
            } else if (httpType.getValue() instanceof List) {
                parseStubResponseList(stubBuilder, httpType);
            }
        }

        return stubBuilder.withCompleteYAML(toYAMLString(httpMessageConfig))
                .withRequestAsYAML(toYAMLString(httpMessageConfig, REQUEST))
                .withResponseAsYAML(toYAMLString(httpMessageConfig, RESPONSE))
                .withResourceId(parsedStubCounter.getAndIncrement())
                .build();
    }

    private void parseMapProperties(final StubHttpLifecycle.Builder stubBuilder, final Map.Entry<String, Object> httpTypeConfig) throws IOException {
        final Map<String, Object> httpTypeProperties = asCheckedLinkedHashMap(httpTypeConfig.getValue(), String.class, Object.class);

        if (httpTypeConfig.getKey().equals(REQUEST.toString())) {
            final StubRequest requestStub = buildReflectableStub(httpTypeProperties, new StubRequest.Builder());

            requestStub.compileRegexPatternsAndCache();

            stubBuilder.withRequest(requestStub);
            ConsoleUtils.logUnmarshalledStubRequest(requestStub.getMethod(), requestStub.getUrl());

        } else {
            final StubResponse responseStub = buildReflectableStub(httpTypeProperties, new StubResponse.Builder());
            stubBuilder.withResponse(responseStub);
        }
    }


    private <T extends ReflectableStub, B extends AbstractBuilder<T>> T buildReflectableStub(final Map<String, Object> httpTypeProperties, final B stubTypeBuilder) throws IOException {

        for (final Map.Entry<String, Object> propertyPair : httpTypeProperties.entrySet()) {

            final Object rawFieldName = propertyPair.getValue();
            final String stageableFieldName = propertyPair.getKey();

            if (isUnknownProperty(stageableFieldName)) {
                throw new IllegalStateException("An unknown property configured: " + stageableFieldName);
            }

            final Object stageableFieldValue;

            if (rawFieldName instanceof List) {
                stageableFieldValue = rawFieldName;

            } else if (rawFieldName instanceof Map) {
                final Map<String, String> rawHeaders = asCheckedLinkedHashMap(rawFieldName, String.class, String.class);
                stageableFieldValue = configureAuthorizationHeader(rawHeaders);

            } else if (stageableFieldName.toLowerCase().equals(METHOD.toString())) {

                final ArrayList<String> methods = new ArrayList<>(1);
                methods.add(objectToString(rawFieldName));
                stageableFieldValue = methods;

            } else if (isFileProperty(stageableFieldName)) {
                stageableFieldValue = loadFileContentFromFileUrl(rawFieldName);
            } else {
                stageableFieldValue = objectToString(rawFieldName);
            }
            stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), ofNullable(stageableFieldValue));
        }

        return stubTypeBuilder.build();
    }

    private void parseStubResponseList(final StubHttpLifecycle.Builder stubBuilder, final Map.Entry<String, Object> httpTypeConfig) throws IOException {
        final List<Map> responseProperties = asCheckedArrayList(httpTypeConfig.getValue(), Map.class);

        stubBuilder.withResponse(buildStubResponseList(responseProperties, new StubResponse.Builder()));
    }

    private List<StubResponse> buildStubResponseList(final List<Map> responseProperties, final StubResponse.Builder stubResponseBuilder) throws IOException {
        final List<StubResponse> stubResponses = new LinkedList<>();

        for (final Map rawPropertyPairs : responseProperties) {
            final Map<String, Object> propertyPairs = asCheckedLinkedHashMap(rawPropertyPairs, String.class, Object.class);
            for (final Map.Entry<String, Object> propertyPair : propertyPairs.entrySet()) {
                final String stageableFieldName = propertyPair.getKey();
                if (isUnknownProperty(stageableFieldName)) {
                    throw new IllegalStateException("An unknown property configured: " + stageableFieldName);
                }
                Object stageableFieldValue = propertyPair.getValue();
                if (isFileProperty(stageableFieldName)) {
                    stageableFieldValue = loadFileContentFromFileUrl(stageableFieldValue);
                }
                stubResponseBuilder.stage(ofNullableProperty(stageableFieldName), ofNullable(stageableFieldValue));
            }

            stubResponses.add(stubResponseBuilder.build());
        }

        return stubResponses;
    }

    private boolean isFileProperty(final String stubbedProperty) {
        return stubbedProperty.toLowerCase().equals(FILE.toString());
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

    private String toYAMLString(final Map<String, Object> httpMessageConfig) {
        final List<Map<String, Object>> root = new ArrayList<Map<String, Object>>() {{
            add(httpMessageConfig);
        }};

        return SNAKE_YAML.dumpAs(root, null, FlowStyle.BLOCK);
    }

    private String toYAMLString(final Map<String, Object> httpMessageConfig, final ConfigurableYAMLProperty httpTypeName) {
        final Map<String, Object> httpType = new HashMap<String, Object>() {{
            put(httpTypeName.toString(), httpMessageConfig.get(httpTypeName.toString()));
        }};

        return SNAKE_YAML.dumpAs(httpType, null, FlowStyle.BLOCK);
    }

    private Map<String, String> configureAuthorizationHeader(final Map<String, String> rawHeaders) {

        final Map<String, String> headers = new LinkedHashMap<>();

        for (final Map.Entry<String, String> entry : rawHeaders.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());

            if (headers.containsKey(BASIC.asYAMLProp())) {
                final String headerValue = headers.get(BASIC.asYAMLProp());
                final String authorizationHeader = trimIfSet(headerValue);
                final String encodedAuthorizationHeader = String.format("%s %s", BASIC.asString(), encodeBase64(authorizationHeader));
                headers.put(BASIC.asYAMLProp(), encodedAuthorizationHeader);

            } else if (headers.containsKey(BEARER.asYAMLProp())) {
                final String headerValue = headers.get(BEARER.asYAMLProp());
                headers.put(BEARER.asYAMLProp(), String.format("%s %s", BEARER.asString(), trimIfSet(headerValue)));

            } else if (headers.containsKey(CUSTOM.asYAMLProp())) {
                final String headerValue = headers.get(CUSTOM.asYAMLProp());
                headers.put(CUSTOM.asYAMLProp(), trimIfSet(headerValue));
            }
        }

        return headers;
    }
}

