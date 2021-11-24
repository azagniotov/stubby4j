package io.github.azagniotov.stubby4j.server;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class StubsJettyNativeWebSocket extends WebSocketAdapter {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<>());

    private CountDownLatch closureLatch = new CountDownLatch(1);

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
//        System.out.println("Socket Connected: " + sess);
//        System.out.println("");
//        System.out.println("UpgradeRequest: " + sess.getUpgradeRequest().getClass().getSimpleName());
//        System.out.println("getRequestURI: " + sess.getUpgradeRequest().getRequestURI());
//        System.out.println("getParameterMap: " + sess.getUpgradeRequest().getParameterMap());
//        System.out.println("getHttpVersion: " + sess.getUpgradeRequest().getHttpVersion());
//        System.out.println("getMethod: " + sess.getUpgradeRequest().getMethod());
//        System.out.println("getQueryString: " + sess.getUpgradeRequest().getQueryString());
//        System.out.println("getQueryString: " + sess.getUpgradeRequest().getHeaders());

        if (getSession().getPolicy().getBehavior().equals(WebSocketBehavior.SERVER)) {
            getSession().getRemote().sendStringByFuture("Connection OK!");
        }
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);

        if (message.toLowerCase(Locale.US).contains("bye")) {
            getSession().close(StatusCode.NORMAL, "Thanks");
        }

        if (getSession().getPolicy().getBehavior().equals(WebSocketBehavior.SERVER)) {
            if (message.trim().equalsIgnoreCase("/item/uri?param=value")) {
                getSession().getRemote().sendBytesByFuture(ByteBuffer.wrap("Hello from Server".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
        if (getSession() != null && getSession().getPolicy().getBehavior().equals(WebSocketBehavior.SERVER)) {
            getSession().getRemote().sendStringByFuture("Socket closed!");
        }
        closureLatch.countDown();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException {
        System.out.println("Awaiting closure from remote");
        closureLatch.await();
    }
}
