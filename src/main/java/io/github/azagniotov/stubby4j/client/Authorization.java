package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;

public final class Authorization {

    private final AuthorizationType authorizationType;
    private final String value;

    public Authorization(final AuthorizationType authorizationType, final String value) {
        this.authorizationType = authorizationType;
        this.value = value;
    }


    String asFullValue() {
        if (authorizationType == AuthorizationType.CUSTOM) {
            return value;
        } else {
            return String.format("%s %s", authorizationType.asString(), value);
        }
    }

    @Override
    @GeneratedCodeCoverageExclusion
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Authorization");
        sb.append("{type=").append(authorizationType);
        sb.append(", value=").append(value);
        sb.append('}');

        return sb.toString();
    }

    enum AuthorizationType {
        BASIC("Basic"),
        BEARER("Bearer"),
        CUSTOM("Custom");
        private final String type;

        AuthorizationType(final String type) {
            this.type = type;
        }

        public String asString() {
            return type;
        }
    }
}
