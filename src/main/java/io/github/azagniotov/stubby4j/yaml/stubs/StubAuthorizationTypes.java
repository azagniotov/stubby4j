package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.utils.StringUtils;

public enum StubAuthorizationTypes {

    BASIC("Basic"),
    BEARER("Bearer"),
    CUSTOM("Custom");

    private final String name;
    private final String property;

    StubAuthorizationTypes(final String name) {
        this.name = name;
        this.property = String.format("authorization-%s", StringUtils.toLower(this.name));
    }

    public String asYamlProp() {
        return property;
    }

    public String asString() {
        return name;
    }
}
