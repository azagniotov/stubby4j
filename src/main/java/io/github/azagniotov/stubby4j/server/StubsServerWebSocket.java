package io.github.azagniotov.stubby4j.server;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@WebSocket
public class StubsServerWebSocket {

    private volatile Session session;
    private RemoteEndpoint remote;

    @OnWebSocketConnect
    public void onWebSocketConnect(final Session session) {
        this.session = session;
        this.remote = this.session.getRemote();

        this.remote.sendStringByFuture("Server connection OK!");
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
}
