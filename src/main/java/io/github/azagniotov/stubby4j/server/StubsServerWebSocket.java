package io.github.azagniotov.stubby4j.server;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

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
    public void onWebSocketText(String message) {
        if (message.toLowerCase(Locale.US).contains("bye")) {
            this.session.close(StatusCode.NORMAL, "Thanks");
        }

        System.out.printf("Received by server: %s%n", message);

        if (message.trim().equalsIgnoreCase("/item/uri?param=value")) {
            this.remote.sendStringByFuture("Hello from Server");
        }
    }

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
        if (this.session != null && this.session.getPolicy().getBehavior().equals(WebSocketBehavior.SERVER)) {
            this.remote.sendStringByFuture("Socket closed!");
        }
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException {
        System.out.println("Awaiting closure from remote");
    }
}
