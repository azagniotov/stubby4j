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
    URL,
    DESCRIPTION,
    UUID;

    private static final Map<String, ConfigurableYAMLProperty> PROPERTY_NAME_TO_ENUM_MEMBER;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final ConfigurableYAMLProperty enumMember : EnumSet.allOf(ConfigurableYAMLProperty.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    public static Optional<ConfigurableYAMLProperty> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty)));
    }

    public boolean isA(final String stubbedProperty) {
        return this.toString().equals(stubbedProperty.toLowerCase());
    }

    @Override
    public String toString() {
        return toLower(this.name());
    }
}
