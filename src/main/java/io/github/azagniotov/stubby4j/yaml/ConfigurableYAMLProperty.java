package io.github.azagniotov.stubby4j.yaml;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum ConfigurableYAMLProperty {

    // proxy-config properties
    PROXY_CONFIG("proxy-config"),
    PROXY_NAME("proxy-name"),
    PROXY_STRATEGY("proxy-strategy"),
    PROXY_PROPERTIES("proxy-properties"),
    ENDPOINT("endpoint"),

    INCLUDES("includes"),
    BODY("body"),
    FILE("file"),
    HEADERS("headers"),
    HTTPLIFECYCLE("httplifecycle"),
    LATENCY("latency"),
    METHOD("method"),
    POST("post"),
    QUERY("query"),
    REQUEST("request"),
    RESPONSE("response"),
    STATUS("status"),
    URL("url"),
    DESCRIPTION("description"),
    UUID("uuid");

    private static final Map<String, ConfigurableYAMLProperty> PROPERTY_NAME_TO_ENUM_MEMBER;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final ConfigurableYAMLProperty enumMember : EnumSet.allOf(ConfigurableYAMLProperty.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    private final String value;

    ConfigurableYAMLProperty(final String value) {
        this.value = value;
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    public static Optional<ConfigurableYAMLProperty> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty)));
    }

    public boolean isA(final String stubbedProperty) {
        return this.toString().equals(stubbedProperty.toLowerCase(Locale.US));
    }

    @Override
    public String toString() {
        return toLower(this.value);
    }
}
