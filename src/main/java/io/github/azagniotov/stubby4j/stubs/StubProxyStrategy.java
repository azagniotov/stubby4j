package io.github.azagniotov.stubby4j.stubs;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;

public enum StubProxyStrategy {
    AS_IS("as-is"),
    CUSTOM("custom");

    private static final Map<String, StubProxyStrategy> PROPERTY_NAME_TO_ENUM_MEMBER;

    static {
        PROPERTY_NAME_TO_ENUM_MEMBER = new HashMap<>();
        for (final StubProxyStrategy enumMember : EnumSet.allOf(StubProxyStrategy.class)) {
            PROPERTY_NAME_TO_ENUM_MEMBER.put(enumMember.toString(), enumMember);
        }
    }

    private final String value;

    StubProxyStrategy(final String value) {
        this.value = value;
    }

    public static boolean isUnknownProperty(final String stubbedProperty) {
        return !PROPERTY_NAME_TO_ENUM_MEMBER.containsKey(toLower(stubbedProperty));
    }

    public static Optional<StubProxyStrategy> ofNullableProperty(final String stubbedProperty) {
        return Optional.ofNullable(PROPERTY_NAME_TO_ENUM_MEMBER.get(toLower(stubbedProperty)));
    }

    public boolean isA(final String strategy) {
        return this.toString().equals(strategy.toLowerCase(Locale.US));
    }

    @Override
    public String toString() {
        return toLower(this.value);
    }
}
