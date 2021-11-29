package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.stubs.proxy.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.proxy.StubProxyStrategy;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketClientRequest;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketOnMessageLifeCycle;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponse;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedArrayList;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logUnmarshalledProxyConfig;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logUnmarshalledStub;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.FileUtils.constructInputStream;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.FileUtils.uriToFile;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase64;
import static io.github.azagniotov.stubby4j.utils.StringUtils.objectToString;
import static io.github.azagniotov.stubby4j.utils.StringUtils.trimIfSet;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.CLIENT_REQUEST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.DESCRIPTION;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HTTPLIFECYCLE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.MESSAGE_TYPE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.METHOD;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ON_MESSAGE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ON_OPEN_SERVER_RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.REQUEST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.SERVER_RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.SERVER_RESPONSE_POLICY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.STRATEGY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.UUID;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.fromString;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.isUnknownFamilyProperty;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.isUnknownProperty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle;

public class YamlParser {
    static final String FAILED_TO_LOAD_FILE_ERR = "Failed to retrieveLoadedStubs response content using relative path specified in 'file'. Check that response content exists in relative path specified in 'file'";
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlParser.class);
    private final static Yaml SNAKE_YAML = SnakeYaml.INSTANCE.getSnakeYaml();
    private final AtomicInteger parsedStubCounter = new AtomicInteger();
    private String dataConfigHomeDirectory;

    public Object loadRawYamlConfig(final InputStream configAsStream) {
        return SNAKE_YAML.load(configAsStream);
    }

    public boolean isMainYamlHasIncludes(final Object loadedYamlConfig) {
        return loadedYamlConfig instanceof Map &&
                ((Map) loadedYamlConfig).containsKey(ConfigurableYAMLProperty.INCLUDES.toString());
    }

    public List<File> getYamlIncludes(final String dataConfigHomeDirectory, final Object loadedYamlConfig) throws IOException {
        final Object includePathsObject = ((Map) loadedYamlConfig).get(ConfigurableYAMLProperty.INCLUDES.toString());
        final List<String> includePaths = asCheckedArrayList(includePathsObject, String.class);

        final List<File> yamlIncludes = new ArrayList<>();
        for (final String includePath : includePaths) {
            final File yamlInclude = uriToFile(dataConfigHomeDirectory, includePath);
            yamlIncludes.add(yamlInclude);
        }

        return yamlIncludes;
    }

    public YamlParseResultSet parse(final String dataConfigHomeDirectory, final String configContent) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configContent));
    }

    public YamlParseResultSet parse(final String dataConfigHomeDirectory, final File configFile) throws IOException {
        return parse(dataConfigHomeDirectory, constructInputStream(configFile));
    }

    private YamlParseResultSet parse(final String dataConfigHomeDirectory, final InputStream configAsStream) throws IOException {
        this.dataConfigHomeDirectory = dataConfigHomeDirectory;

        final Object loadedConfig = loadYamlFromInputStream(configAsStream);

        final List<StubHttpLifecycle> stubs = new LinkedList<>();
        final Map<String, StubHttpLifecycle> uuidToStubs = new HashMap<>();
        final Map<String, StubProxyConfig> proxyConfigs = new HashMap<>();
        final Map<String, StubWebSocketConfig> webSocketConfigs = new LinkedHashMap<>();

        final List<Map> yamlMappings = asCheckedArrayList(loadedConfig, Map.class);

        for (final Map yamlMapping : yamlMappings) {
            final Map<String, Object> yamlMappingProperties = asCheckedLinkedHashMap(yamlMapping, String.class, Object.class);
            if (isProxyConfigMapping(yamlMapping)) {
                // the YAML config file contains a top-level:
                // - proxy-config
                final StubProxyConfig stubProxyConfig = parseStubProxyConfig(yamlMappingProperties);
                if (proxyConfigs.containsKey(stubProxyConfig.getUUID())) {
                    throw new IOException("Proxy config YAML contains duplicate UUIDs: " + stubProxyConfig.getUUID());
                }

                proxyConfigs.put(stubProxyConfig.getUUID(), stubProxyConfig);
            } else if (isWebSocketConfigMapping(yamlMapping)) {
                // the YAML config file contains a top-level:
                // - web-socket
                final StubWebSocketConfig stubWebSocketConfig = parseStubWebSocketConfig(yamlMappingProperties);
                if (webSocketConfigs.containsKey(stubWebSocketConfig.getUrl())) {
                    throw new IOException("Web socket config YAML contains duplicate URL: " + stubWebSocketConfig.getUrl());
                }

                if (ObjectUtils.isNull(stubWebSocketConfig.getOnOpenServerResponse()) && stubWebSocketConfig.getOnMessage().isEmpty()) {
                    throw new IOException("Web socket config must have at least one of the two 'on-open' or 'on-message' defined");
                }

                webSocketConfigs.put(stubWebSocketConfig.getUrl(), stubWebSocketConfig);
            } else {
                // the YAML config file contains a top-level:
                // - request
                final StubHttpLifecycle stubHttpLifecycle = parseStubbedHttpLifecycleConfig(yamlMappingProperties);

                if (StringUtils.isSet(stubHttpLifecycle.getUUID())) {
                    if (uuidToStubs.containsKey(stubHttpLifecycle.getUUID())) {
                        throw new IOException("Stubs YAML contains duplicate UUIDs: " + stubHttpLifecycle.getUUID());
                    }
                    uuidToStubs.put(stubHttpLifecycle.getUUID(), stubHttpLifecycle);
                }
                stubs.add(stubHttpLifecycle);
            }
        }

        return new YamlParseResultSet(stubs, uuidToStubs, proxyConfigs, webSocketConfigs);
    }

    private Object loadYamlFromInputStream(final InputStream configAsStream) throws IOException {
        Object loadedConfig = loadRawYamlConfig(configAsStream);

        // This means that our main YAML config includes other files, i.e.:
        //
        // includes:
        //  - service-1-stubs.yaml
        //  - service-2-stubs.yaml
        //  - service-3-stubs.yaml
        //
        if (isMainYamlHasIncludes(loadedConfig)) {
            final List<File> yamlIncludes = getYamlIncludes(dataConfigHomeDirectory, loadedConfig);

            final StringBuilder uberYamlBuilder = new StringBuilder();
            for (final File yamlInclude : yamlIncludes) {
                try (final InputStream pathInputStream = constructInputStream(yamlInclude)) {
                    uberYamlBuilder.append(StringUtils.inputStreamToString(pathInputStream));
                    uberYamlBuilder.append(BR).append(BR).append(BR);
                }
            }

            loadedConfig = loadRawYamlConfig(constructInputStream(uberYamlBuilder.toString()));
        }

        if (!(loadedConfig instanceof List)) {
            throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
        }
        return loadedConfig;
    }

    private StubProxyConfig parseStubProxyConfig(final Map<String, Object> yamlMappingProperties) {
        final StubProxyConfig.Builder proxyConfigBuilder = new StubProxyConfig.Builder();

        for (final Map.Entry<String, Object> stubType : yamlMappingProperties.entrySet()) {
            final Object stubTypeValue = stubType.getValue();

            final Map<String, Object> stubbedProperties = asCheckedLinkedHashMap(stubTypeValue, String.class, Object.class);

            buildReflectableStub(stubbedProperties, proxyConfigBuilder);
            proxyConfigBuilder.withProxyConfigAsYAML(toCompleteYamlListString(yamlMappingProperties));
        }

        final StubProxyConfig stubProxyConfig = proxyConfigBuilder.build();
        logUnmarshalledProxyConfig(stubProxyConfig);

        return stubProxyConfig;
    }

    private StubWebSocketConfig parseStubWebSocketConfig(final Map<String, Object> yamlMappingProperties) {
        final StubWebSocketConfig.Builder webSocketConfigBuilder = new StubWebSocketConfig.Builder();

        for (final Map.Entry<String, Object> stubType : yamlMappingProperties.entrySet()) {
            final String stubTypeKey = stubType.getKey();
            final Object stubTypeValue = stubType.getValue();

            if (DESCRIPTION.isA(stubTypeKey)) {
                webSocketConfigBuilder.withDescription((String) stubType.getValue());
            } else if (UUID.isA(stubTypeKey)) {
                webSocketConfigBuilder.withUuid((String) stubType.getValue());
            } else if (stubTypeValue instanceof Map) {
                final Map<String, Object> webSocketProperties = asCheckedLinkedHashMap(stubTypeValue, String.class, Object.class);

                for (final Map.Entry<String, Object> webSocketPropertyEntries : webSocketProperties.entrySet()) {
                    final String webSocketPropertyKey = webSocketPropertyEntries.getKey();
                    final Object webSocketPropertyValue = webSocketPropertyEntries.getValue();

                    if (ON_OPEN_SERVER_RESPONSE.isA(webSocketPropertyKey)) {

                        final Map<String, Object> onOpenServerResponseProperties = asCheckedLinkedHashMap(webSocketPropertyValue, String.class, Object.class);

                        final StubWebSocketServerResponse.Builder serverResponseStubBuilder = buildReflectableStub(onOpenServerResponseProperties, new StubWebSocketServerResponse.Builder());
                        serverResponseStubBuilder.withWebSocketServerResponseAsYAML(toYaml(webSocketProperties, ON_OPEN_SERVER_RESPONSE));
                        webSocketConfigBuilder.withOnOpenServerResponse(serverResponseStubBuilder.build());
                    } else if (ON_MESSAGE.isA(webSocketPropertyKey)) {

                        final Set<Integer> clientRequestBodyTextHashCodeCache = new HashSet<>();
                        final Set<Integer> clientRequestBodyBytesHashCodeCache = new HashSet<>();

                        final List<StubWebSocketOnMessageLifeCycle> lifeCycles = new LinkedList<>();

                        final List<Map> onMessageLifeCycles = asCheckedArrayList(webSocketPropertyValue, Map.class);
                        for (Map onMessageLifeCycle : onMessageLifeCycles) {
                            final Map<String, Object> onMessageLifeCycleObjects = asCheckedLinkedHashMap(onMessageLifeCycle, String.class, Object.class);

                            final Object rawClientRequest = onMessageLifeCycleObjects.get(CLIENT_REQUEST.toString());
                            final StubWebSocketClientRequest clientRequest = buildReflectableStub(
                                    asCheckedLinkedHashMap(rawClientRequest, String.class, Object.class),
                                    new StubWebSocketClientRequest.Builder())
                                    .withWebSocketClientRequestAsYAML(toYaml(onMessageLifeCycleObjects, CLIENT_REQUEST))
                                    .build();

                            checkAndThrowWhenClientRequestDuplicateBodies(clientRequestBodyTextHashCodeCache, clientRequestBodyBytesHashCodeCache, clientRequest);

                            final Object rawServerResponse = onMessageLifeCycleObjects.get(SERVER_RESPONSE.toString());
                            final StubWebSocketServerResponse serverResponse = buildReflectableStub(
                                    asCheckedLinkedHashMap(rawServerResponse, String.class, Object.class),
                                    new StubWebSocketServerResponse.Builder())
                                    .withWebSocketServerResponseAsYAML(toYaml(onMessageLifeCycleObjects, SERVER_RESPONSE))
                                    .build();

                            final String lifeCycleCompleteYAML = toCompleteYamlListString(asCheckedLinkedHashMap(onMessageLifeCycle, String.class, Object.class));

                            lifeCycles.add(new StubWebSocketOnMessageLifeCycle(clientRequest, serverResponse, lifeCycleCompleteYAML));
                        }

                        webSocketConfigBuilder.withOnMessage(lifeCycles);
                    } else {
                        webSocketConfigBuilder.stage(fromString(webSocketPropertyKey), of(webSocketPropertyValue));
                    }
                }
            }

            webSocketConfigBuilder.withWebSocketConfigAsYAML(toCompleteYamlListString(yamlMappingProperties));
        }

        return webSocketConfigBuilder.build();
    }

    private void checkAndThrowWhenClientRequestDuplicateBodies(final Set<Integer> clientRequestBodyTextHashCodeCache,
                                                               final Set<Integer> clientRequestBodyBytesHashCodeCache,
                                                               final StubWebSocketClientRequest clientRequest) {
        if (clientRequest.getMessageType() == StubWebSocketMessageType.TEXT) {
            final int clientRequestBodyTextHashCode = clientRequest.getBodyAsString().hashCode();
            if (clientRequestBodyTextHashCodeCache.contains(clientRequestBodyTextHashCode)) {
                throw new UncheckedIOException(new IOException("Web socket on-message contains multiple client-request with the same body text"));
            } else {
                clientRequestBodyTextHashCodeCache.add(clientRequestBodyTextHashCode);
            }
        } else {
            final int clientRequestBodyBytesHashCode = Arrays.hashCode(clientRequest.getBodyAsBytes());
            if (clientRequestBodyBytesHashCodeCache.contains(clientRequestBodyBytesHashCode)) {
                throw new UncheckedIOException(new IOException("Web socket on-message contains multiple client-request with the same body bytes"));
            } else {
                clientRequestBodyBytesHashCodeCache.add(clientRequestBodyBytesHashCode);
            }
        }
    }

    private StubHttpLifecycle parseStubbedHttpLifecycleConfig(final Map<String, Object> yamlMappingProperties) {
        final StubHttpLifecycle.Builder stubBuilder = new StubHttpLifecycle.Builder();

        for (final Map.Entry<String, Object> stubType : yamlMappingProperties.entrySet()) {
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
            } else {
                checkStubbedProperty(stubTypeKey, HTTPLIFECYCLE.toString());
            }
        }

        StubHttpLifecycle loadedStub = stubBuilder.withCompleteYAML(toCompleteYamlListString(yamlMappingProperties))
                .withRequestAsYAML(toYaml(yamlMappingProperties, REQUEST))
                .withResponseAsYAML(toYaml(yamlMappingProperties, RESPONSE))
                .withResourceId(parsedStubCounter.getAndIncrement())
                .build();

        logUnmarshalledStub(loadedStub);

        return loadedStub;
    }

    private void parseStubbedRequestConfig(final StubHttpLifecycle.Builder stubBuilder, final Map<String, Object> requestProperties) {
        final StubRequest.Builder requestStubBuilder = buildReflectableStub(requestProperties, new StubRequest.Builder());
        final StubRequest stubRequest = requestStubBuilder.build();
        stubRequest.compileRegexPatternsAndCache();
        stubBuilder.withRequest(stubRequest);
    }

    private void parseStubbedResponseConfig(final StubHttpLifecycle.Builder stubBuilder, final Map<String, Object> responseProperties) {
        final StubResponse.Builder responseStubBuilder = buildReflectableStub(responseProperties, new StubResponse.Builder());
        stubBuilder.withResponse(responseStubBuilder.build());
    }

    private <T extends ReflectableStub, B extends AbstractBuilder<T>> B buildReflectableStub(final Map<String, Object> stubbedProperties, final B stubTypeBuilder) {

        for (final Map.Entry<String, Object> propertyPair : stubbedProperties.entrySet()) {

            final String stageableFieldName = propertyPair.getKey();
            checkStubbedProperty(stageableFieldName, stubTypeBuilder.yamlFamilyName());

            final Object rawFieldNameValue = propertyPair.getValue();
            if (rawFieldNameValue instanceof List) {
                stubTypeBuilder.stage(fromString(stageableFieldName), of(rawFieldNameValue));
                continue;
            }

            if (rawFieldNameValue instanceof Map) {
                final Map<String, String> rawHeaders = asCheckedLinkedHashMap(rawFieldNameValue, String.class, String.class);
                final Map<String, String> headers = configureAuthorizationHeader(rawHeaders);
                stubTypeBuilder.stage(fromString(stageableFieldName), of(headers));
                continue;
            }

            if (METHOD.isA(stageableFieldName)) {
                final ArrayList<String> methods = new ArrayList<>(Collections.singletonList(objectToString(rawFieldNameValue)));
                stubTypeBuilder.stage(fromString(stageableFieldName), of(methods));
                continue;
            }

            if (FILE.isA(stageableFieldName)) {
                final Optional<Object> fileContentOptional = loadFileContentFromFileUrl(rawFieldNameValue);
                stubTypeBuilder.stage(fromString(stageableFieldName), fileContentOptional);
                continue;
            }

            if (STRATEGY.isA(stageableFieldName)) {

                final String stubbedProperty = objectToString(rawFieldNameValue);
                if (StubProxyStrategy.isUnknownProperty(stubbedProperty)) {
                    throw new IllegalArgumentException(stubbedProperty);
                }

                final Optional<StubProxyStrategy> stubProxyStrategyOptional = StubProxyStrategy.ofNullableProperty(stubbedProperty);
                final Optional<Object> stubProxyStrategyObjectOptional = stubProxyStrategyOptional.map(stubProxyStrategy -> stubProxyStrategy);
                stubTypeBuilder.stage(fromString(stageableFieldName), stubProxyStrategyObjectOptional);
                continue;
            }

            if (SERVER_RESPONSE_POLICY.isA(stageableFieldName)) {

                final String stubbedProperty = objectToString(rawFieldNameValue);
                if (StubWebSocketServerResponsePolicy.isUnknownProperty(stubbedProperty)) {
                    throw new IllegalArgumentException(stubbedProperty);
                }

                final Optional<StubWebSocketServerResponsePolicy> stubWebSocketServerResponseStrategy = StubWebSocketServerResponsePolicy.ofNullableProperty(stubbedProperty);
                final Optional<Object> stubWebSocketServerResponseStrategyOptional = stubWebSocketServerResponseStrategy.map(stubProxyStrategy -> stubProxyStrategy);
                stubTypeBuilder.stage(fromString(stageableFieldName), stubWebSocketServerResponseStrategyOptional);
                continue;
            }

            if (MESSAGE_TYPE.isA(stageableFieldName)) {

                final String stubbedProperty = objectToString(rawFieldNameValue);
                if (StubWebSocketMessageType.isUnknownProperty(stubbedProperty)) {
                    throw new IllegalArgumentException(stubbedProperty);
                }

                final Optional<StubWebSocketMessageType> stubWebSocketMessageType = StubWebSocketMessageType.ofNullableProperty(stubbedProperty);
                final Optional<Object> stubWebSocketMessageTypeOptional = stubWebSocketMessageType.map(stubProxyStrategy -> stubProxyStrategy);
                stubTypeBuilder.stage(fromString(stageableFieldName), stubWebSocketMessageTypeOptional);
                continue;
            }

            stubTypeBuilder.stage(fromString(stageableFieldName), ofNullable(objectToString(rawFieldNameValue)));
        }

        return stubTypeBuilder;
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
                checkStubbedProperty(stageableFieldName, RESPONSE.toString());

                if (FILE.isA(stageableFieldName)) {
                    final Optional<Object> fileContentOptional = loadFileContentFromFileUrl(propertyPair.getValue());
                    stubResponseBuilder.stage(fromString(stageableFieldName), fileContentOptional);
                } else {
                    stubResponseBuilder.stage(fromString(stageableFieldName), ofNullable(propertyPair.getValue()));
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

            return of(uriToFile(dataConfigHomeDirectory, filePath));
        } catch (final IOException ex) {
            ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
            LOGGER.error(FAILED_TO_LOAD_FILE_ERR, ex);
        }

        return Optional.empty();
    }

    private String toCompleteYamlListString(final Map<String, Object> yamlMappingProperties) {
        final List<Map<String, Object>> root = new ArrayList<Map<String, Object>>() {{
            add(yamlMappingProperties);
        }};

        return SNAKE_YAML.dumpAs(root, null, FlowStyle.BLOCK);
    }

    private String toYaml(final Map<String, Object> yamlMappingProperties, final ConfigurableYAMLProperty stubName) {
        final Map<String, Object> httpType = new HashMap<String, Object>() {{
            put(stubName.toString(), yamlMappingProperties.get(stubName.toString()));
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

    private boolean isProxyConfigMapping(final Object loadedYamlConfig) {
        return loadedYamlConfig instanceof Map &&
                ((Map) loadedYamlConfig).containsKey(ConfigurableYAMLProperty.PROXY_CONFIG.toString());
    }

    private boolean isWebSocketConfigMapping(final Object loadedYamlConfig) {
        return loadedYamlConfig instanceof Map &&
                ((Map) loadedYamlConfig).containsKey(ConfigurableYAMLProperty.WEB_SOCKET.toString());
    }

    private void checkStubbedProperty(final String stageableFieldName, final String propertyFamilyName) {
        if (isUnknownProperty(stageableFieldName)) {
            throw new IllegalStateException("An unknown property configured: " + stageableFieldName);
        } else if (isUnknownFamilyProperty(stageableFieldName, propertyFamilyName)) {
            if (propertyFamilyName.equals(HTTPLIFECYCLE.toString())) {
                throw new IllegalStateException(String.format("Invalid property '%s' configured. This property cannot be configured above the 'request'", stageableFieldName));
            } else {
                throw new IllegalStateException(String.format("Invalid property '%s' configured. This property does not belong in object '%s'", stageableFieldName, propertyFamilyName));
            }
        }
    }
}

