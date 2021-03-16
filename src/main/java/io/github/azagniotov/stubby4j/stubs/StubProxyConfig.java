package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ENDPOINT;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.PROXY_NAME;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.PROXY_PROPERTIES;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.PROXY_STRATEGY;

public class StubProxyConfig implements ReflectableStub {

    private final String proxyName;
    private final StubProxyStrategy proxyStrategy;
    private final Map<String, String> proxyProperties;
    private final String proxyConfigAsYAML;

    private StubProxyConfig(final String proxyName,
                            final StubProxyStrategy proxyStrategy,
                            final Map<String, String> proxyProperties,
                            final String proxyConfigAsYAML) {
        this.proxyName = proxyName;
        this.proxyStrategy = proxyStrategy;
        this.proxyProperties = proxyProperties;
        this.proxyConfigAsYAML = proxyConfigAsYAML;
    }

    public String getProxyName() {
        return proxyName;
    }

    public StubProxyStrategy getProxyStrategy() {
        return proxyStrategy;
    }

    public Map<String, String> getProxyProperties() {
        return new HashMap<>(proxyProperties);
    }

    public String getProxyEndpoint() {
        return proxyProperties.get(ENDPOINT.toString());
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
        return Objects.equals(proxyName, that.proxyName) &&
                proxyStrategy == that.proxyStrategy &&
                proxyProperties.equals(that.proxyProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyName, proxyStrategy, proxyProperties);
    }

    public static final class Builder extends AbstractBuilder<StubProxyConfig> {

        public static final String DEFAULT_NAME = "CATCH_ALL";
        private String proxyName;
        private StubProxyStrategy proxyStrategy;
        private Map<String, String> proxyProperties;
        private String proxyConfigAsYAML;

        public Builder() {
            super();
            this.proxyName = DEFAULT_NAME;
            this.proxyStrategy = null;
            this.proxyProperties = new LinkedHashMap<>();
            this.proxyConfigAsYAML = null;
        }

        public Builder withProxyName(final String proxyName) {
            this.proxyName = proxyName;

            return this;
        }

        public Builder withProxyStrategy(final String proxyStrategy) {
            this.proxyStrategy = StubProxyStrategy.ofNullableProperty(proxyStrategy)
                    .orElseThrow(() -> new IllegalArgumentException(proxyStrategy));

            return this;
        }

        public Builder withProxyProperty(final String key, final String value) {
            this.proxyProperties.put(key, value);

            return this;
        }

        public Builder withProxyPropertyEndpoint(final String value) {
            this.proxyProperties.put(ENDPOINT.toString(), value);

            return this;
        }

        public Builder withProxyConfigAsYAML(final String proxyConfigAsYAML) {
            this.proxyConfigAsYAML = proxyConfigAsYAML;

            return this;
        }


        @Override
        public StubProxyConfig build() {
            this.proxyName = getStaged(String.class, PROXY_NAME, proxyName);
            this.proxyStrategy = getStaged(StubProxyStrategy.class, PROXY_STRATEGY, proxyStrategy);
            this.proxyProperties = asCheckedLinkedHashMap(getStaged(Map.class, PROXY_PROPERTIES, proxyProperties), String.class, String.class);

            final StubProxyConfig stubProxyConfig = new StubProxyConfig(proxyName, proxyStrategy, proxyProperties, proxyConfigAsYAML);

            this.proxyName = DEFAULT_NAME;
            this.proxyStrategy = null;
            this.proxyProperties = new LinkedHashMap<>();
            this.proxyConfigAsYAML = null;
            this.fieldNameAndValues.clear();

            return stubProxyConfig;
        }
    }
}
