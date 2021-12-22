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
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.server.websocket.StubsServerWebSocket.EMPTY_BYTE_BUFFER;
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StubsServerWebSocketTest {

    private static final String HELLO_FROM_CLIENT = "hello-from-client";
    private static final String HELLO_FROM_SERVER = "hello-from-server";
    private static final ByteBuffer BYTE_BUFFER_HELLO_FROM_SERVER = ByteBuffer.wrap(StringUtils.getBytesUtf8(HELLO_FROM_SERVER));

    @Mock
    private Session mockSession;

    @Mock
    private RemoteEndpoint mockRemoteEndpoint;

    @Mock
    private ServletUpgradeRequest mockServletUpgradeRequest;

    @Spy
    private ScheduledExecutorService spyScheduledExecutorService = Executors.newScheduledThreadPool(10);

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<ByteBuffer> byteBufferCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private StubsServerWebSocket serverWebSocket;

    @Before
    public void setUp() throws Exception {
        when(mockSession.getRemote()).thenReturn(mockRemoteEndpoint);
        when(mockSession.getUpgradeRequest()).thenReturn(mockServletUpgradeRequest);
        when(mockServletUpgradeRequest.isSecure()).thenReturn(false);
        when(mockServletUpgradeRequest.getMethod()).thenReturn("GET");
        when(mockServletUpgradeRequest.getRequestURI()).thenReturn(URI.create("/this/is/uri/path"));
    }

    @After
    public void cleanUp() throws Exception {
        spyScheduledExecutorService.shutdown();

        assertThat(spyScheduledExecutorService.isShutdown()).isTrue();
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerTextResponseWhenPolicyOnce() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("250")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.ONCE.toString())
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).schedule(
                runnableCaptor.capture(),
                eq(250L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendStringByFuture(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(HELLO_FROM_SERVER);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerTextResponseWhenPolicyDisconnect() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("250")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.DISCONNECT.toString())
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        // 1. 1st future is scheduled when sending configured text response
        // 2. 2nd future is scheduled when session disconnects
        verify(spyScheduledExecutorService, times(2)).schedule(
                runnableCaptor.capture(),
                eq(250L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured futures which were passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        for (final Runnable captured : runnableCaptor.getAllValues()) {
            captured.run();
        }

        verify(mockRemoteEndpoint, times(1)).sendStringByFuture(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(HELLO_FROM_SERVER);

        verify(mockSession, times(1)).close(eq(StatusCode.NORMAL), eq("bye"));
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerBinaryResponseWhenPolicyDisconnect() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("250")
                .withMessageType(StubWebSocketMessageType.BINARY.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.DISCONNECT.toString())
                .withFile(tempFileFromString(HELLO_FROM_SERVER))
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        // 1. 1st future is scheduled when sending configured text response
        // 2. 2nd future is scheduled when session disconnects
        verify(spyScheduledExecutorService, times(2)).schedule(
                runnableCaptor.capture(),
                eq(250L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured futures which were passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        for (final Runnable captured : runnableCaptor.getAllValues()) {
            captured.run();
        }

        verify(mockRemoteEndpoint, times(1)).sendBytesByFuture(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(BYTE_BUFFER_HELLO_FROM_SERVER);

        verify(mockSession, times(1)).close(eq(StatusCode.NORMAL), eq("bye"));
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerPeriodicPingResponseWhenPolicyPing() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("1337")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.PING.toString())
                // Although body is set, the PING strategy sends a special Ping DataFrame with empty byte[]
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(1337L),
                eq(1337L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendPing(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(EMPTY_BYTE_BUFFER);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerPeriodicTextResponseWhenPolicyPush() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("5000")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.PUSH.toString())
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(5000L),
                eq(5000L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendStringByFuture(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(HELLO_FROM_SERVER);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerPeriodicBinaryResponseWhenPolicyPush() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("18")
                .withMessageType(StubWebSocketMessageType.BINARY.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.PUSH.toString())
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).scheduleAtFixedRate(
                runnableCaptor.capture(),
                eq(18L),
                eq(18L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendBytesByFuture(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(BYTE_BUFFER_HELLO_FROM_SERVER);
    }

    @Test
    public void onWebSocketConnect_DispatchesExpectedServerBinaryResponseWhenPolicyFragmentation() throws Exception {
        final String tanuki = "The Japanese raccoon dog is known as the tanuki.";
        final byte[] originalStringBytes = StringUtils.getBytesUtf8(tanuki);

        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("5")
                .withStrategy(StubWebSocketServerResponsePolicy.FRAGMENTATION.toString())
                .withBody(tanuki)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(true, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        serverWebSocket.onWebSocketConnect(mockSession);

        verify(spyScheduledExecutorService, times(1)).schedule(
                runnableCaptor.capture(),
                eq(5L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        // FYI: tanuki string bytes[] divided by StubsServerWebSocket.FRAGMENTATION_FRAMES produces 48 chunks
        verify(mockRemoteEndpoint, times(48)).sendPartialBytes(byteBufferCaptor.capture(), anyBoolean());
        final List<ByteBuffer> allCapturedFragments = byteBufferCaptor.getAllValues();
        assertThat(allCapturedFragments.size()).isEqualTo(48);

        // Currently I do not see an easy way to enforce the right order of byte frames through
        // the Argument captor in order to correctly assemble them into a string for assertion.
        // Normally, the web socket client ensures that all partial frames are assembled correctly
//        ByteBuffer allocatedByteBuffer = ByteBuffer.allocate(originalStringBytes.length);
//        for (ByteBuffer allCapturedFragment : allCapturedFragments) {
//            allocatedByteBuffer = allocatedByteBuffer.put(allCapturedFragment);
//        }
//        final byte[] actualStringBytes = allocatedByteBuffer.array();
//
//        assertThat(tanuki).isEqualTo(new String(actualStringBytes, StandardCharsets.UTF_8));
    }

    @Test
    public void onWebSocketText_DispatchesExpectedServerTextResponseWhenPolicyOnce() throws Exception {
        final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse.Builder()
                .withDelay("250")
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .withStrategy(StubWebSocketServerResponsePolicy.ONCE.toString())
                .withBody(HELLO_FROM_SERVER)
                .build();
        final StubWebSocketConfig stubWebSocketConfig = buildStubWebSocketConfig(false, webSocketServerResponse);
        serverWebSocket = new StubsServerWebSocket(stubWebSocketConfig, spyScheduledExecutorService);

        // Sets the session and remote endpoint. On Open event will be disabled due to 'null' as OnOpenServerResponse
        serverWebSocket.onWebSocketConnect(mockSession);
        serverWebSocket.onWebSocketText(HELLO_FROM_CLIENT);

        verify(spyScheduledExecutorService, times(1)).schedule(
                runnableCaptor.capture(),
                eq(250L),
                eq(TimeUnit.MILLISECONDS));

        // Execute the captured future which was passed in to the
        // ScheduledExecutorService, in order to trigger the behavior
        runnableCaptor.getValue().run();

        verify(mockRemoteEndpoint, times(1)).sendStringByFuture(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(HELLO_FROM_SERVER);
    }

    private StubWebSocketConfig buildStubWebSocketConfig(final boolean setOnOpen,
                                                         final StubWebSocketServerResponse webSocketServerResponse) {
        final StubWebSocketClientRequest webSocketClientRequest = new StubWebSocketClientRequest.Builder()
                .withBody(HELLO_FROM_CLIENT)
                .withMessageType(StubWebSocketMessageType.TEXT.toString())
                .build();

        final StubWebSocketOnMessageLifeCycle webSocketOnMessageLifeCycle =
                new StubWebSocketOnMessageLifeCycle(webSocketClientRequest, webSocketServerResponse, "");

        return new StubWebSocketConfig.Builder()
                .withUuid("123-abd-def")
                .withUrl("/web-socket/uri/path")
                .withSubProtocols("echo")
                .withOnOpenServerResponse(setOnOpen ? webSocketServerResponse : null)
                .withOnMessage(Collections.singletonList(webSocketOnMessageLifeCycle))
                .build();
    }
}