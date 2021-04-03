package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.utils.ReflectionUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.DESCRIPTION;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ENDPOINT;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.PROPERTIES;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.STRATEGY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.UUID;

public class StubProxyConfig implements ReflectableStub {

    private final String description;
    private final String uuid;
    private final StubProxyStrategy strategy;
    private final Map<String, String> headers;
    private final Map<String, String> properties;
    private final String proxyConfigAsYAML;

    private StubProxyConfig(final String description,
                            final String uuid,
                            final StubProxyStrategy strategy,
                            final Map<String, String> headers,
                            final Map<String, String> properties,
                            final String proxyConfigAsYAML) {
        this.description = description;
        this.uuid = uuid;
        this.strategy = strategy;
        this.headers = headers;
        this.properties = properties;
        this.proxyConfigAsYAML = proxyConfigAsYAML;
    }

    public String getDescription() {
        return description;
    }

    public String getUUID() {
        return uuid;
    }

    public StubProxyStrategy getStrategy() {
        return strategy;
    }

    public boolean isAdditiveStrategy() {
        return strategy == StubProxyStrategy.ADDITIVE;
    }

    public final Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public boolean hasHeaders() {
        return !getHeaders().isEmpty();
    }

    public Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }

    public String getPropertyEndpoint() {
        return properties.get(ENDPOINT.toString());
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getProxyConfigAsYAML() {
        return proxyConfigAsYAML;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StubProxyConfig)) return false;
        StubProxyConfig that = (StubProxyConfig) o;
        return Objects.equals(uuid, that.uuid) &&
                strategy == that.strategy &&
                headers.equals(that.headers) &&
                properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, strategy, properties);
    }

    public static final class Builder extends AbstractBuilder<StubProxyConfig> {

        public static final String DEFAULT_UUID = "default";
        private String description;
        private String uuid;
        private StubProxyStrategy strategy;
        private Map<String, String> headers;
        private Map<String, String> properties;
        private String proxyConfigAsYAML;

        public Builder() {
            super();
            reset();
        }

        public Builder withDescription(final String description) {
            this.description = description;

            return this;
        }

        public Builder withUuid(final String uuid) {
            this.uuid = uuid;

            return this;
        }

        public Builder withStrategy(final String strategy) {
            this.strategy = StubProxyStrategy.ofNullableProperty(strategy)
                    .orElseThrow(() -> new IllegalArgumentException(strategy));

            return this;
        }

        public Builder withHeader(final String key, final String value) {
            this.headers.put(key, value);

            return this;
        }

        public Builder withProperty(final String key, final String value) {
            this.properties.put(key, value);

            return this;
        }

        public Builder withPropertyEndpoint(final String endpoint) {
            this.properties.put(ENDPOINT.toString(), endpoint);

            return this;
        }

        public Builder withProxyConfigAsYAML(final String proxyConfigAsYAML) {
            this.proxyConfigAsYAML = proxyConfigAsYAML;

            return this;
        }


        @Override
        public StubProxyConfig build() {
            this.description = getStaged(String.class, DESCRIPTION, description);
            this.uuid = getStaged(String.class, UUID, uuid);
            this.strategy = getStaged(StubProxyStrategy.class, STRATEGY, strategy);
            this.headers = asCheckedLinkedHashMap(getStaged(Map.class, HEADERS, headers), String.class, String.class);
            this.properties = asCheckedLinkedHashMap(getStaged(Map.class, PROPERTIES, properties), String.class, String.class);

            final StubProxyConfig stubProxyConfig = new StubProxyConfig(
                    description,
                    uuid,
                    strategy,
                    headers,
                    properties,
                    proxyConfigAsYAML);

            reset();

            this.fieldNameAndValues.clear();

            return stubProxyConfig;
        }

        private void reset() {
            this.description = null;
            this.uuid = DEFAULT_UUID;
            this.strategy = StubProxyStrategy.AS_IS;
            this.headers = new LinkedHashMap<>();
            this.properties = new LinkedHashMap<>();
            this.proxyConfigAsYAML = null;
        }
    }
}
