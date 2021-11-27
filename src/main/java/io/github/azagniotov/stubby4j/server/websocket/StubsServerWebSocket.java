package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@WebSocket
public class StubsServerWebSocket {

    private final static Set<String> SUB_PROTOCOLS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("chat", "echo")));

    private final StubWebSocketConfig stubWebSocketConfig;
    private volatile Session session;
    private RemoteEndpoint remote;
    private volatile boolean webSocketCreationFailed = false;

    public StubsServerWebSocket(final StubWebSocketConfig stubWebSocketConfig) {
        this.stubWebSocketConfig = stubWebSocketConfig;
    }

    @OnWebSocketConnect
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        if (!webSocketCreationFailed) {
            this.remote = this.session.getRemote();
            this.remote.sendStringByFuture("Server connection OK!");
        } else {
            // Reject the session and close the connection
            this.session.close(StatusCode.PROTOCOL, "Unexpected sub-protocol");
        }
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(byte[] array, int offset, int length) {
        final String message = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(array, offset, length)).toString();
        if (message.toLowerCase(Locale.US).trim().contains("bye")) {
            this.remote.sendStringByFuture("Client requested to close socket via binary message!");
            this.session.close(StatusCode.NORMAL, "Thanks");
        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(final String message) {
        if (message.toLowerCase(Locale.US).trim().contains("bye")) {
            this.remote.sendStringByFuture("Client requested to close socket!");
            this.session.close(StatusCode.NORMAL, "Thanks");
        }

        System.out.printf("Received by server: %s%n", message);

        if (message.trim().equalsIgnoreCase("/item/uri?param=value")) {
            this.remote.sendStringByFuture("Item URI with query string param");
        } else if (message.trim().equalsIgnoreCase("/items")) {
            this.remote.sendStringByFuture("All items");
        }
    }

    @OnWebSocketClose
    public void onWebSocketClose(final int statusCode, final String reason) {
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
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
            } else {
                // Mark as failed in order to close connection on onWebSocketConnect
                webSocketCreationFailed = true;
            }
        }
    }
}

