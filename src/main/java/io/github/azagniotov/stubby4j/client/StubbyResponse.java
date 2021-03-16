package io.github.azagniotov.stubby4j.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StubbyResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headerFields;

    public StubbyResponse(final int statusCode,
                          final String body,
                          final Map<String, List<String>> headerFields) {
        this.statusCode = statusCode;
        this.body = body;
        this.headerFields = headerFields;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public Map<String, List<String>> headers() {
        return new HashMap<>(headerFields);
    }
}