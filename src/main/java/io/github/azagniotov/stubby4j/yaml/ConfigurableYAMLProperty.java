package io.github.azagniotov.stubby4j.yaml;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum ConfigurableYAMLProperty {

    // allows for YAML sub-configs
    INCLUDES("includes"),

    // proxy-config & http lifecycle properties
    DESCRIPTION("description"),
    UUID("uuid"),

    // proxy-config properties
    PROXY_CONFIG("proxy-config"),
    STRATEGY("strategy"),
    PROPERTIES("properties"),
    ENDPOINT("endpoint"),

    HTTPLIFECYCLE("httplifecycle"),
    REQUEST("request"),
    RESPONSE("response"),

    // stub request specific
    URL("url"),
    METHOD("method"),
    POST("post"),
    QUERY("query"),

    // stub response specific
    BODY("body"),
    LATENCY("latency"),
    STATUS("status"),

    // stub request & response properties
    FILE("file"),
    HEADERS("headers");


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

    public static ConfigurableYAMLProperty fromString(final String stubbedProperty) {
        return PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty));
    }

    public boolean isA(final String stubbedProperty) {
        return this.toString().equals(stubbedProperty.toLowerCase(Locale.US));
    }

    @Override
    public String toString() {
        return toLower(this.value);
    }
}
