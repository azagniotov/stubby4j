package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketClientRequest;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketOnMessageLifeCycle;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponse;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType.TEXT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.DISCONNECT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.ONCE;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.FRAGMENTATION;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.PUSH;
import static io.github.azagniotov.stubby4j.utils.CollectionUtils.chunkifyByteArrayAndQueue;

@WebSocket
public class StubsServerWebSocket {

    private static final String NORMAL_CLOSE_REASON = "bye";
    private static final int FRAGMENTATION_FRAMES = 100;

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

        if (stubWebSocketConfig.getOnOpenServerResponse() != null) {
            dispatchServerResponse(stubWebSocketConfig.getOnOpenServerResponse());
        }
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(final byte[] incoming, final int offset, final int length) {
        final ServletUpgradeRequest upgradeRequest = (ServletUpgradeRequest) this.session.getUpgradeRequest();
        ConsoleUtils.logIncomingWebSocketTextRequest(upgradeRequest, "binary payload");

        boolean found = false;
        final List<StubWebSocketOnMessageLifeCycle> onMessage = stubWebSocketConfig.getOnMessage();
        for (final StubWebSocketOnMessageLifeCycle lifeCycle : onMessage) {
            final StubWebSocketClientRequest clientRequest = lifeCycle.getClientRequest();
            final StubWebSocketServerResponse serverResponse = lifeCycle.getServerResponse();

            if (Arrays.equals(clientRequest.getBodyAsBytes(), incoming)) {
                found = true;
                dispatchServerResponse(serverResponse);
                break;
            }
        }

        if (!found) {
            this.remote.sendStringByFuture(String.format("404 Not Found: client request %s", incoming));
        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(final String message) {
        final ServletUpgradeRequest upgradeRequest = (ServletUpgradeRequest) this.session.getUpgradeRequest();
        ConsoleUtils.logIncomingWebSocketTextRequest(upgradeRequest, message);

        boolean found = false;
        final List<StubWebSocketOnMessageLifeCycle> onMessage = stubWebSocketConfig.getOnMessage();
        for (final StubWebSocketOnMessageLifeCycle lifeCycle : onMessage) {
            final StubWebSocketClientRequest clientRequest = lifeCycle.getClientRequest();
            final StubWebSocketServerResponse serverResponse = lifeCycle.getServerResponse();

            if (clientRequest.getBodyAsString().equals(message.trim())) {
                found = true;
                dispatchServerResponse(serverResponse);
                break;
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

    private void dispatchServerResponse(final StubWebSocketServerResponse serverResponse) {
        final long delay = serverResponse.getDelay();
        if (serverResponse.getPolicy() == ONCE || serverResponse.getPolicy() == DISCONNECT) {

            scheduledExecutorService.schedule(() -> {
                if (serverResponse.getMessageType() == TEXT) {
                    // Send response in a UTF-8 text form as a whole
                    this.remote.sendStringByFuture(serverResponse.getBodyAsString());
                } else {
                    // Send response in a binary form as a whole blob
                    this.remote.sendBytesByFuture(ByteBuffer.wrap(serverResponse.getBodyAsBytes()));
                }

            }, delay, TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == FRAGMENTATION) {
            final BlockingQueue<ByteBuffer> queue = chunkifyByteArrayAndQueue(serverResponse.getBodyAsBytes(), FRAGMENTATION_FRAMES);
            scheduledExecutorService.schedule(() -> {
                while (!queue.isEmpty()) {
                    try {
                        final ByteBuffer byteBufferChunk = queue.poll();
                        if (byteBufferChunk != null) {
                            final boolean isLast = queue.isEmpty();
                            // Send response in a binary form as sequential fragmented frames one after another in
                            // a blocking manner. This must be a blocking call, i.e.: we cannot send each chunk
                            // in an async manner using a Future, as this can produce un-deterministic behavior.
                            this.remote.sendPartialBytes(byteBufferChunk, isLast);
                            Thread.sleep(delay);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, delay, TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == PUSH) {
            // Send response to the client in periodic pushes one after another. The content will be sent as a whole
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

