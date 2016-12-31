package io.github.azagniotov.stubby4j.yaml;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum ConfigurableYAMLProperty {

    BODY,
    FILE,
    HEADERS,
    HTTPLIFECYCLE,
    LATENCY,
    METHOD,
    POST,
    QUERY,
    REQUEST,
    RESPONSE,
    STATUS,
    URL;

    private static final Map<String, ConfigurableYAMLProperty> CACHE;

    static {
        CACHE = new HashMap<>();
        for (final ConfigurableYAMLProperty enumMember : EnumSet.allOf(ConfigurableYAMLProperty.class)) {
            CACHE.put(enumMember.toString(), enumMember);
        }
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !CACHE.containsKey(toLower(stubbedProperty));
    }

    public static Optional<ConfigurableYAMLProperty> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(CACHE.get(toLower(stubbedProperty)));
    }

    @Override
    public String toString() {
        return toLower(this.name());
    }
}
