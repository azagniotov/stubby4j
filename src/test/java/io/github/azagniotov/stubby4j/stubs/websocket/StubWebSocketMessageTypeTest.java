package io.github.azagniotov.stubby4j.stubs.websocket;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class StubWebSocketMessageTypeTest {
    @Test
    public void returnsTrueOnKnownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketMessageType.isUnknownProperty("text")).isFalse();
        assertThat(StubWebSocketMessageType.isUnknownProperty("BiNary")).isFalse();
    }

    @Test
    public void returnsFalseOnUnknownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketMessageType.isUnknownProperty("apple")).isTrue();
        assertThat(StubWebSocketMessageType.isUnknownProperty("")).isTrue();
    }
}