package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketClientRequest;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketOnMessageLifeCycle;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponse;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.server.websocket.StubsServerWebSocket.EMPTY_BYTE_BUFFER;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StubsServerWebSocketTest {

    @Mock
    private Session mockSession;

    @Mock
    private RemoteEndpoint mockRemoteEndpoint;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<ByteBuffer> byteBufferCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private ScheduledExecutorService spyScheduledExecutorService;

    private StubsServerWebSocket serverWebSocket;

    @Before
    public void setUp() throws Exception {
        spyScheduledExecutorService = spy(Executors.newScheduledThreadPool(10));
        when(mockSession.getRemote()).thenReturn(mockRemoteEndpoint);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerTextResponseOnce() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("250")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.ONCE.toString())
                .withBody("hello-from-server")
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).schedule(
                runnableCaptor.capture(),
                eq(250L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the future in order to invoke the lambda which invokes the RemoteEndpoint
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendStringByFuture(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo("hello-from-server");
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerPeriodicPingResponse() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("1337")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.PING.toString())
                // Although body is set, the PING strategy sends a special Ping DataFrame with empty byte[]
                .withBody("hello-from-server")
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(1337L),
                eq(1337L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the future in order to invoke the lambda which invokes the RemoteEndpoint
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendPing(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(EMPTY_BYTE_BUFFER);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerPeriodicBinaryResponse() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("18")
                .withMessageType(StubWebSocketMessageType.BINARY.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.PUSH.toString())
                .withBody("hello-from-server")
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(18L),
                eq(18L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the future in order to invoke the lambda which invokes the RemoteEndpoint
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendBytesByFuture(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(ByteBuffer.wrap(StringUtils.getBytesUtf8("hello-from-server")));
    }

    private StubWebSocketConfig buildStubWebSocketConfig(final StubWebSocketServerResponse webSocketServerResponse) {
        final StubWebSocketClientRequest webSocketClientRequest = new StubWebSocketClientRequest.Builder()
                .withBody("hello-from-client")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .build();

        final StubWebSocketOnMessageLifeCycle webSocketOnMessageLifeCycle =
                new StubWebSocketOnMessageLifeCycle(webSocketClientRequest, webSocketServerResponse, "");

        return new StubWebSocketConfig.Builder()
                .withUuid("123-abd-def")
                .withUrl("/web-socket/uri/path")
                .withSubProtocols("echo")
                .withOnOpenServerResponse(webSocketServerResponse)
                .withOnMessage(Collections.singletonList(webSocketOnMessageLifeCycle))
                .build();
    }
}