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

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedArrayList;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logUnmarshalledStub;
import static io.github.azagniotov.stubby4j.utils.FileUtils.constructInputStream;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.FileUtils.uriToFile;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase64;
import static io.github.azagniotov.stubby4j.utils.StringUtils.objectToString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.trimIfSet;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.DESCRIPTION;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.METHOD;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.REQUEST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.UUID;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.isUnknownProperty;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ofNullableProperty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YamlParser {
    static final String FAILED_TO_LOAD_FILE_ERR = "Failed to retrieveLoadedStubs response content using relative path specified in 'file'. Check that response content exists in relative path specified in 'file'";
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlParser.class);
    private final static Yaml SNAKE_YAML = SnakeYaml.INSTANCE.getSnakeYaml();
    private final AtomicInteger parsedStubCounter = new AtomicInteger();
    private String dataConfigHomeDirectory;


    public YamlParseResultSet parse(final String dataConfigHomeDirectory, final String configContent) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configContent));
    }


    public YamlParseResultSet parse(final String dataConfigHomeDirectory, final File configFile) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configFile));
    }

    private YamlParseResultSet parse(final String dataConfigHomeDirectory, final InputStream configAsStream) throws IOException {
        this.dataConfigHomeDirectory = dataConfigHomeDirectory;

        final Object loadedConfig = SNAKE_YAML.load(configAsStream);
        if (!(loadedConfig instanceof List)) {
            throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
        }

        final List<StubHttpLifecycle> stubs = new LinkedList<>();
        final Map<String, StubHttpLifecycle> uuidToStubs = new HashMap<>();

        final List<Map> httpLifecycleConfigs = asCheckedArrayList(loadedConfig, Map.class);

        for (final Map rawHttpLifecycleConfig : httpLifecycleConfigs) {
            final Map<String, Object> httpLifecycleProperties = asCheckedLinkedHashMap(rawHttpLifecycleConfig, String.class, Object.class);
            final StubHttpLifecycle stubHttpLifecycle = parseStubbedHttpLifecycleConfig(httpLifecycleProperties);

            if (StringUtils.isSet(stubHttpLifecycle.getUUID())) {
                if (uuidToStubs.containsKey(stubHttpLifecycle.getUUID())) {
                    throw new IOException("Stubbed YAML contains duplicates of UUID " + stubHttpLifecycle.getUUID());
                }
                uuidToStubs.put(stubHttpLifecycle.getUUID(), stubHttpLifecycle);
            }
            stubs.add(stubHttpLifecycle);
        }

        return new YamlParseResultSet(stubs, uuidToStubs);
    }

    private StubHttpLifecycle parseStubbedHttpLifecycleConfig(final Map<String, Object> httpLifecycleConfig) {
        final StubHttpLifecycle.Builder stubBuilder = new StubHttpLifecycle.Builder();

        for (final Map.Entry<String, Object> stubType : httpLifecycleConfig.entrySet()) {
            final Object stubTypeValue = stubType.getValue();
            final String stubTypeKey = stubType.getKey();

            if (DESCRIPTION.isA(stubTypeKey)) {
                stubBuilder.withDescription((String) stubType.getValue());
            } else if (UUID.isA(stubTypeKey)) {
                stubBuilder.withUUID((String) stubType.getValue());
            } else if (stubTypeValue instanceof Map) {
                final Map<String, Object> stubbedProperties = asCheckedLinkedHashMap(stubTypeValue, String.class, Object.class);

                if (REQUEST.isA(stubTypeKey)) {
                    parseStubbedRequestConfig(stubBuilder, stubbedProperties);
                } else {
                    parseStubbedResponseConfig(stubBuilder, stubbedProperties);
                }

            } else if (stubTypeValue instanceof List) {
                parseStubbedResponseListConfig(stubBuilder, stubType);
            }
        }

        StubHttpLifecycle loadedStub = stubBuilder.withCompleteYAML(toCompleteYAMLString(httpLifecycleConfig))
                .withRequestAsYAML(toYaml(httpLifecycleConfig, REQUEST))
                .withResponseAsYAML(toYaml(httpLifecycleConfig, RESPONSE))
                .withResourceId(parsedStubCounter.getAndIncrement())
                .build();

        logUnmarshalledStub(loadedStub);

        return loadedStub;
    }

    private void parseStubbedRequestConfig(final StubHttpLifecycle.Builder stubBuilder, final Map<String, Object> requestProperties) {
        final StubRequest requestStub = buildReflectableStub(requestProperties, new StubRequest.Builder());
        requestStub.compileRegexPatternsAndCache();
        stubBuilder.withRequest(requestStub);
    }

    private void parseStubbedResponseConfig(final StubHttpLifecycle.Builder stubBuilder, final Map<String, Object> responseProperties) {
        final StubResponse responseStub = buildReflectableStub(responseProperties, new StubResponse.Builder());
        stubBuilder.withResponse(responseStub);
    }

    private <T extends ReflectableStub, B extends AbstractBuilder<T>> T buildReflectableStub(final Map<String, Object> stubbedProperties, final B stubTypeBuilder) {

        for (final Map.Entry<String, Object> propertyPair : stubbedProperties.entrySet()) {

            final String stageableFieldName = propertyPair.getKey();
            checkStubbedProperty(stageableFieldName);

            final Object rawFieldName = propertyPair.getValue();
            if (rawFieldName instanceof List) {
                stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), of(rawFieldName));
                continue;
            }

            if (rawFieldName instanceof Map) {
                final Map<String, String> rawHeaders = asCheckedLinkedHashMap(rawFieldName, String.class, String.class);
                final Map<String, String> headers = configureAuthorizationHeader(rawHeaders);
                stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), of(headers));
                continue;
            }

            if (METHOD.isA(stageableFieldName)) {
                final ArrayList<String> methods = new ArrayList<>(Collections.singletonList(objectToString(rawFieldName)));
                stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), of(methods));
                continue;
            }

            if (FILE.isA(stageableFieldName)) {
                final Optional<Object> fileContentOptional = loadFileContentFromFileUrl(rawFieldName);
                stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), fileContentOptional);
                continue;
            }

            stubTypeBuilder.stage(ofNullableProperty(stageableFieldName), ofNullable(objectToString(rawFieldName)));
        }

        return stubTypeBuilder.build();
    }

    private void parseStubbedResponseListConfig(final StubHttpLifecycle.Builder stubBuilder, final Map.Entry<String, Object> httpTypeConfig) {
        final List<Map> responseProperties = asCheckedArrayList(httpTypeConfig.getValue(), Map.class);

        stubBuilder.withResponse(buildStubResponseList(responseProperties, new StubResponse.Builder()));
    }

    private List<StubResponse> buildStubResponseList(final List<Map> responseProperties, final StubResponse.Builder stubResponseBuilder) {
        final List<StubResponse> stubResponses = new LinkedList<>();

        for (final Map rawPropertyPairs : responseProperties) {
            final Map<String, Object> propertyPairs = asCheckedLinkedHashMap(rawPropertyPairs, String.class, Object.class);
            for (final Map.Entry<String, Object> propertyPair : propertyPairs.entrySet()) {
                final String stageableFieldName = propertyPair.getKey();
                checkStubbedProperty(stageableFieldName);

                if (FILE.isA(stageableFieldName)) {
                    final Optional<Object> fileContentOptional = loadFileContentFromFileUrl(propertyPair.getValue());
                    stubResponseBuilder.stage(ofNullableProperty(stageableFieldName), fileContentOptional);
                } else {
                    stubResponseBuilder.stage(ofNullableProperty(stageableFieldName), ofNullable(propertyPair.getValue()));
                }
            }

            stubResponses.add(stubResponseBuilder.build());
        }

        return stubResponses;
    }

    private Optional<Object> loadFileContentFromFileUrl(final Object configPropertyNamedFile) {
        final String filePath = objectToString(configPropertyNamedFile);
        try {
            if (isFilePathContainTemplateTokens(new File(filePath))) {
                return of(new File(dataConfigHomeDirectory, filePath));
            }

            return ofNullable(uriToFile(dataConfigHomeDirectory, filePath));
        } catch (final IOException ex) {
            ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
            LOGGER.error(FAILED_TO_LOAD_FILE_ERR, ex);
        }

        return Optional.empty();
    }

    private String toCompleteYAMLString(final Map<String, Object> httpLifecycleConfig) {
        final List<Map<String, Object>> root = new ArrayList<Map<String, Object>>() {{
            add(httpLifecycleConfig);
        }};

        return SNAKE_YAML.dumpAs(root, null, FlowStyle.BLOCK);
    }

    private String toYaml(final Map<String, Object> httpLifecycleConfig, final ConfigurableYAMLProperty stubName) {
        final Map<String, Object> httpType = new HashMap<String, Object>() {{
            put(stubName.toString(), httpLifecycleConfig.get(stubName.toString()));
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

    private void checkStubbedProperty(String stageableFieldName) {
        if (isUnknownProperty(stageableFieldName)) {
            throw new IllegalStateException("An unknown property configured: " + stageableFieldName);
        }
    }
}

