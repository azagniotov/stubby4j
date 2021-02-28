package io.github.azagniotov.stubby4j.http;

public enum HttpMethodExtended {

    PATCH;

    HttpMethodExtended() {

    }

    public String asString() {
        return toString();
    }
}
