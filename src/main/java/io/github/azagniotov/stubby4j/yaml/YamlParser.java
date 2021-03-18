package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubProxyStrategy;
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
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logUnmarshalledProxyConfig;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logUnmarshalledStub;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
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
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.STRATEGY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.UUID;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.fromString;
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

        return new YamlParseResultSet(stubs, uuidToStubs, proxyConfigs);
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
            checkStubbedProperty(stageableFieldName);

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
                checkStubbedProperty(stageableFieldName);

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

    private void checkStubbedProperty(String stageableFieldName) {
        if (isUnknownProperty(stageableFieldName)) {
            throw new IllegalStateException("An unknown property configured: " + stageableFieldName);
        }
    }
}

