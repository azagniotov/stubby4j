package io.github.azagniotov.stubby4j.stubs.websocket;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class StubWebSocketServerResponsePolicyTest {

    @Test
    public void returnsTrueOnKnownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("once")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("push")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("fragmentation")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("ping")).isFalse();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("disconnect")).isFalse();
    }

    @Test
    public void returnsFalseOnUnknownProperties() throws Exception {
        // Double negative logic
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("apple")).isTrue();
        assertThat(StubWebSocketServerResponsePolicy.isUnknownProperty("")).isTrue();
    }
}