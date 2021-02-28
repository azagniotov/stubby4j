package io.github.azagniotov.stubby4j.client;

public final class StubbyResponse {

    private final int responseCode;
    private final String content;

    public StubbyResponse(final int newResponseCode, final String newContent) {
        this.responseCode = newResponseCode;
        this.content = newContent;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getContent() {
        return content;
    }
}