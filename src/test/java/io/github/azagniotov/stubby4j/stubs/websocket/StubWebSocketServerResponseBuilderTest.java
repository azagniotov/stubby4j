package io.github.azagniotov.stubby4j.stubs.websocket;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType.TEXT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.ONCE;

@RunWith(MockitoJUnitRunner.class)
public class StubWebSocketServerResponseBuilderTest {

    private StubWebSocketServerResponse.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubWebSocketServerResponse.Builder();
    }

    @Test
    public void stubbedWebSocketServerResponseHasExpectedDefaults() throws Exception {

        final StubWebSocketServerResponse webSocketServerResponse = builder.build();
        assertThat(webSocketServerResponse.getDelay()).isEqualTo(0);
        assertThat(webSocketServerResponse.getPolicy()).isEqualTo(ONCE);
        assertThat(webSocketServerResponse.getMessageType()).isEqualTo(TEXT);
    }
}