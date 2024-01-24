/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.server.websocket;

import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketMessageType.TEXT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.DISCONNECT;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.FRAGMENTATION;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.ONCE;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.PING;
import static io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponsePolicy.PUSH;
import static io.github.azagniotov.stubby4j.utils.CollectionUtils.chunkifyByteArrayAndQueue;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.getLocalDateTime;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeMethodCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketClientRequest;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketOnMessageLifeCycle;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketServerResponse;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class StubsServerWebSocket {

    public static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(new byte[0]);
    private static final Logger LOGGER = LoggerFactory.getLogger(StubsServerWebSocket.class);
    private static final String NORMAL_CLOSE_REASON = "bye";
    private static final int FRAGMENTATION_FRAMES = 100;

    private final StubWebSocketConfig stubWebSocketConfig;
    private final ScheduledExecutorService scheduledExecutorService;

    private volatile Session session;
    private RemoteEndpoint remote;

    public StubsServerWebSocket(
            final StubWebSocketConfig stubWebSocketConfig, final ScheduledExecutorService scheduledExecutorService) {
        this.stubWebSocketConfig = stubWebSocketConfig;
        this.scheduledExecutorService = scheduledExecutorService;
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
            this.remote.sendStringByFuture(String.format("404 Not Found: client sent [%s]", incoming));
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
        final String logMessage = String.format(
                "[%s] <= %s %s\n",
                getLocalDateTime(),
                statusCode,
                String.format("Socket closed by client: [%s] %s", statusCode, reason.trim()));

        ANSITerminal.ok(logMessage);
        LOGGER.info(logMessage);
    }

    @OnWebSocketError
    @GeneratedCodeMethodCoverageExclusion
    public void onWebSocketError(Throwable cause) {
        final String logMessage = String.format("[%s] <= %s %s\n", getLocalDateTime(), 500, cause.getMessage());

        ANSITerminal.error(logMessage);
        LOGGER.error(logMessage);
    }

    private void dispatchServerResponse(final StubWebSocketServerResponse serverResponse) {
        final long delay = serverResponse.getDelay();
        if (serverResponse.getPolicy() == ONCE || serverResponse.getPolicy() == DISCONNECT) {

            scheduledExecutorService.schedule(
                    () -> {
                        if (serverResponse.getMessageType() == TEXT) {
                            // Send response in a UTF-8 text form as a whole
                            this.remote.sendStringByFuture(serverResponse.getBodyAsString());
                        } else {
                            // Send response in a binary form as a whole blob
                            this.remote.sendBytesByFuture(ByteBuffer.wrap(serverResponse.getBodyAsBytes()));
                        }
                    },
                    delay,
                    TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == FRAGMENTATION) {
            final BlockingQueue<ByteBuffer> queue =
                    chunkifyByteArrayAndQueue(serverResponse.getBodyAsBytes(), FRAGMENTATION_FRAMES);
            scheduledExecutorService.schedule(
                    () -> {
                        while (!queue.isEmpty()) {
                            try {
                                final ByteBuffer byteBufferChunk = queue.poll();
                                if (byteBufferChunk != null) {
                                    final boolean isLast = queue.isEmpty();
                                    // Send response in a binary form as sequential fragmented frames one after another
                                    // in
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
                    },
                    delay,
                    TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == PUSH) {
            // Send response to the client in periodic pushes one after another. The content will be sent as a whole
            scheduledExecutorService.scheduleAtFixedRate(
                    () -> {
                        if (serverResponse.getMessageType() == TEXT) {
                            this.remote.sendStringByFuture(serverResponse.getBodyAsString());
                        } else {
                            this.remote.sendBytesByFuture(ByteBuffer.wrap(serverResponse.getBodyAsBytes()));
                        }
                    },
                    delay,
                    delay,
                    TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == PING) {
            // Send Ping (without application data) to the connected
            // client upon on-open or on-message config in periodic manner.
            // WebSocket Ping spec: https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.2
            scheduledExecutorService.scheduleAtFixedRate(
                    () -> {
                        try {
                            this.remote.sendPing(EMPTY_BYTE_BUFFER);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    },
                    delay,
                    delay,
                    TimeUnit.MILLISECONDS);
        }

        if (serverResponse.getPolicy() == DISCONNECT) {
            scheduledExecutorService.schedule(
                    () -> {
                        this.session.close(StatusCode.NORMAL, NORMAL_CLOSE_REASON);
                    },
                    delay,
                    TimeUnit.MILLISECONDS);
        }
    }
}
