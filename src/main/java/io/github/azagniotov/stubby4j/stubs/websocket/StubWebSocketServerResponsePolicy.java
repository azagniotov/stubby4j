package io.github.azagniotov.stubby4j.stubs.websocket;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum StubWebSocketServerResponsePolicy {

    ONCE("once"),
    PUSH("push"),
    PARTIAL("partial"),
    DISCONNECT("disconnect");

    private static final Map<String, StubWebSocketServerResponsePolicy> PROPERTY_NAME_TO_ENUM_MEMBER;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final StubWebSocketServerResponsePolicy enumMember : EnumSet.allOf(StubWebSocketServerResponsePolicy.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    private final String value;

    StubWebSocketServerResponsePolicy(final String value) {
        this.value = value;
    }

    public static Optional<StubWebSocketServerResponsePolicy> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty)));
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    @Override
    public String toString() {
        return toLower(this.value);
    }
}
