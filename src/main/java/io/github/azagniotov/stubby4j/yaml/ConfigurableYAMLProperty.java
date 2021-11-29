package io.github.azagniotov.stubby4j.yaml;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum ConfigurableYAMLProperty {

    // allows for YAML sub-configs
    INCLUDES("includes"),

    // web-socket, proxy-config & http lifecycle properties
    DESCRIPTION("description"),
    UUID("uuid"),

    // web-socket properties
    WEB_SOCKET("web-socket"),
    SUB_PROTOCOLS("sub-protocols"),
    ON_OPEN_SERVER_RESPONSE("on-open"),
    ON_MESSAGE("on-message"),
    CLIENT_REQUEST("client-request"),
    SERVER_RESPONSE("server-response"),
    SERVER_RESPONSE_POLICY("policy"),
    MESSAGE_TYPE("message-type"),
    DELAY("delay"),

    // proxy-config properties
    PROXY_CONFIG("proxy-config"),
    STRATEGY("strategy"),
    PROPERTIES("properties"),
    ENDPOINT("endpoint"),

    HTTPLIFECYCLE("httplifecycle"),
    REQUEST("request"),
    RESPONSE("response"),

    // stub request specific
    URL("url"), // web-socket properties
    METHOD("method"),
    POST("post"),
    QUERY("query"),

    // stub response specific
    BODY("body"), // web-socket properties
    LATENCY("latency"),
    STATUS("status"),

    // stub request & response properties
    FILE("file"), // request, response, web-socket properties
    HEADERS("headers"); // request, response, proxy-config properties


    private static final Map<String, ConfigurableYAMLProperty> PROPERTY_NAME_TO_ENUM_MEMBER;
    private static final Map<String, Set<String>> PROPERTY_NAME_TO_FAMILY;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final ConfigurableYAMLProperty enumMember : EnumSet.allOf(ConfigurableYAMLProperty.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    static {
        PROPERTY_NAME_TO_FAMILY = new HashMap<>();

        final Set<String> httpLifecycleProperties = new HashSet<>();
        httpLifecycleProperties.add(UUID.toString());
        httpLifecycleProperties.add(DESCRIPTION.toString());
        httpLifecycleProperties.add(REQUEST.toString());
        httpLifecycleProperties.add(RESPONSE.toString());
        PROPERTY_NAME_TO_FAMILY.put(HTTPLIFECYCLE.toString(), httpLifecycleProperties);

        final Set<String> requestProperties = new HashSet<>();
        requestProperties.add(URL.toString());
        requestProperties.add(METHOD.toString());
        requestProperties.add(POST.toString());
        requestProperties.add(QUERY.toString());
        requestProperties.add(FILE.toString());
        requestProperties.add(HEADERS.toString());
        PROPERTY_NAME_TO_FAMILY.put(REQUEST.toString(), requestProperties);

        final Set<String> responseProperties = new HashSet<>();
        responseProperties.add(BODY.toString());
        responseProperties.add(LATENCY.toString());
        responseProperties.add(STATUS.toString());
        responseProperties.add(FILE.toString());
        responseProperties.add(HEADERS.toString());
        PROPERTY_NAME_TO_FAMILY.put(RESPONSE.toString(), responseProperties);

        final Set<String> proxyConfigProperties = new HashSet<>();
        proxyConfigProperties.add(UUID.toString());
        proxyConfigProperties.add(DESCRIPTION.toString());
        proxyConfigProperties.add(STRATEGY.toString());
        proxyConfigProperties.add(PROPERTIES.toString());
        proxyConfigProperties.add(ENDPOINT.toString());
        proxyConfigProperties.add(HEADERS.toString());
        PROPERTY_NAME_TO_FAMILY.put(PROXY_CONFIG.toString(), proxyConfigProperties);

        final Set<String> webSocketProperties = new HashSet<>();
        webSocketProperties.add(UUID.toString());
        webSocketProperties.add(DESCRIPTION.toString());
        webSocketProperties.add(URL.toString());
        webSocketProperties.add(SUB_PROTOCOLS.toString());
        webSocketProperties.add(ON_OPEN_SERVER_RESPONSE.toString());
        webSocketProperties.add(ON_MESSAGE.toString());
        PROPERTY_NAME_TO_FAMILY.put(WEB_SOCKET.toString(), webSocketProperties);

        final Set<String> webSocketClientRequestProperties = new HashSet<>();
        webSocketClientRequestProperties.add(MESSAGE_TYPE.toString());
        webSocketClientRequestProperties.add(BODY.toString());
        webSocketClientRequestProperties.add(FILE.toString());
        PROPERTY_NAME_TO_FAMILY.put(CLIENT_REQUEST.toString(), webSocketClientRequestProperties);

        final Set<String> webSocketServerResponseProperties = new HashSet<>();
        webSocketServerResponseProperties.add(MESSAGE_TYPE.toString());
        webSocketServerResponseProperties.add(SERVER_RESPONSE_POLICY.toString());
        webSocketServerResponseProperties.add(BODY.toString());
        webSocketServerResponseProperties.add(FILE.toString());
        webSocketServerResponseProperties.add(DELAY.toString());
        PROPERTY_NAME_TO_FAMILY.put(SERVER_RESPONSE.toString(), webSocketServerResponseProperties);

        final Set<String> webSocketOnMessageProperties = new HashSet<>();
        webSocketOnMessageProperties.add(CLIENT_REQUEST.toString());
        webSocketOnMessageProperties.add(SERVER_RESPONSE.toString());
        PROPERTY_NAME_TO_FAMILY.put(ON_MESSAGE.toString(), webSocketOnMessageProperties);
    }

    private final String value;

    ConfigurableYAMLProperty(final String value) {
        this.value = value;
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    public static boolean isUnknownFamilyProperty(final String stubbedProperty, final String propertyFamily) {
        return !PROPERTY_NAME_TO_FAMILY.get(toLower(propertyFamily)).contains(toLower(stubbedProperty));
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
