package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketClientRequest;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketOnMessageLifeCycle;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponse;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType.TEXT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.DISCONNECT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.ONCE;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.PUSH;

@WebSocket
public class StubsServerWebSocket {

    private final static Set<String> SUB_PROTOCOLS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("chat", "echo")));
    private static final String NORMAL_CLOSE_REASON = "bye";

    private final StubWebSocketConfig stubWebSocketConfig;
    private final ScheduledExecutorService scheduledExecutorService;

    private volatile Session session;
    private RemoteEndpoint remote;

    public StubsServerWebSocket(final StubWebSocketConfig stubWebSocketConfig) {
        this.stubWebSocketConfig = stubWebSocketConfig;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    @OnWebSocketConnect
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        this.remote = this.session.getRemote();

        dispatchServerResponse(stubWebSocketConfig.getOnOpenServerResponse());
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(final byte[] array, final int offset, final int length) {
//        final String message = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(array, offset, length)).toString();
//        if (message.toLowerCase(Locale.US).trim().contains("bye")) {
//            this.remote.sendStringByFuture("Client requested to close socket via binary message!");
//            this.session.close(StatusCode.NORMAL, "Thanks");
//        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(final String message) {

        boolean found = false;
        final List<StubWebSocketOnMessageLifeCycle> onMessage = stubWebSocketConfig.getOnMessage();
        for (final StubWebSocketOnMessageLifeCycle lifeCycle : onMessage) {
            final StubWebSocketClientRequest clientRequest = lifeCycle.getClientRequest();
            final StubWebSocketServerResponse serverResponse = lifeCycle.getServerResponse();

            if (clientRequest.getBodyAsString().equals(message.trim())) {
                found = true;
                dispatchServerResponse(serverResponse);
            }
        }

        if (!found) {
            this.remote.sendStringByFuture(String.format("404 Not Found: client request %s", message));
        }
    }

    @OnWebSocketClose
    public void onWebSocketClose(final int statusCode, final String reason) {
//        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

    public void checkAndSetAcceptedProtocols(final ServletUpgradeRequest servletUpgradeRequest,
                                             final ServletUpgradeResponse servletUpgradeResponse) {
        if (servletUpgradeRequest.getSubProtocols() != null && !servletUpgradeRequest.getSubProtocols().isEmpty()) {
            final String clientSubProtocol = servletUpgradeRequest.getSubProtocols().get(0);
            if (!clientSubProtocol.trim().equals("") && SUB_PROTOCOLS.contains(clientSubProtocol)) {
                servletUpgradeResponse.setAcceptedSubProtocol(clientSubProtocol);
            }
        }
    }

    private void dispatchServerResponse(final StubWebSocketServerResponse serverResponse) {
        final long delay = serverResponse.getDelay();
        if (serverResponse.getPolicy() == ONCE || serverResponse.getPolicy() == DISCONNECT) {
            scheduledExecutorService.schedule(() -> {

                if (serverResponse.getMessageType() == TEXT) {
                    this.remote.sendStringByFuture(serverResponse.getBodyAsString());
                } else {
                    this.remote.sendBytesByFuture(ByteBuffer.wrap(serverResponse.getBodyAsBytes()));
                }

            }, delay, TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == PUSH) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                if (serverResponse.getMessageType() == TEXT) {
                    this.remote.sendStringByFuture(serverResponse.getBodyAsString());
                } else {
                    this.remote.sendBytesByFuture(ByteBuffer.wrap(serverResponse.getBodyAsBytes()));
                }
            }, delay, delay, TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == DISCONNECT) {
            scheduledExecutorService.schedule(() -> {
                this.session.close(StatusCode.NORMAL, NORMAL_CLOSE_REASON);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}

