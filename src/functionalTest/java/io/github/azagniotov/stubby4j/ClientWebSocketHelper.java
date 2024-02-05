/*
 * Copyright (c) 2021-2024 Alexander Zagniotov
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

package io.github.azagniotov.stubby4j;

import static com.google.common.truth.Truth.assertThat;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.frames.PingFrame;
import org.eclipse.jetty.websocket.common.frames.PongFrame;

/**
 * A test helper class that plays a role of web socket client for the functional tests.
 *<p>
 * The class is annotated with {@link org.eclipse.jetty.websocket.api.annotations.WebSocket}
 */
@WebSocket
public class ClientWebSocketHelper {

    // Prefer this constructor with zero core threads for a shared pool, to avoid blocking JVM exit
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(0);

    private static final Random RANDOM = new Random();

    @VisibleForTesting
    final List<String> receivedOnMessageText;

    @VisibleForTesting
    final List<byte[]> receivedOnMessageBytes;

    @VisibleForTesting
    final List<Integer> receivedOnCloseStatus;

    @VisibleForTesting
    final List<String> receivedOnCloseText;

    private CountDownLatch countDownLatch;

    public ClientWebSocketHelper() {
        this.receivedOnMessageText = new LinkedList<>();
        this.receivedOnMessageBytes = new LinkedList<>();
        this.receivedOnCloseText = new LinkedList<>();
        this.receivedOnCloseStatus = new LinkedList<>();
    }

    void initLatch(final int numberOfExpectedMessages) {
        this.countDownLatch = new CountDownLatch(numberOfExpectedMessages);
        this.receivedOnMessageText.clear();
        this.receivedOnMessageBytes.clear();
        this.receivedOnCloseText.clear();
        this.receivedOnCloseStatus.clear();
    }

    @OnWebSocketConnect
    public void onWebSocketConnect(final Session session) {}

    @OnWebSocketFrame
    public void onOnWebSocketFrame(final Frame frame) throws IOException {
        if (frame instanceof PingFrame) {
            final PingFrame pingFrame = (PingFrame) frame;
            final ByteBuffer pingPayload = pingFrame.getPayload();
            receivedOnMessageBytes.add(pingPayload.array());
            countDownLatch.countDown();
        }

        if (frame instanceof PongFrame) {
            final PongFrame pongFrame = (PongFrame) frame;
            final ByteBuffer pongPayload = pongFrame.getPayload();

            if (!pongPayload.hasArray()) {
                byte[] to = new byte[pongPayload.remaining()];
                pongPayload.slice().get(to);
                receivedOnMessageBytes.add(to);
            } else {
                receivedOnMessageBytes.add(pongPayload.array());
            }
            countDownLatch.countDown();
        }
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(final byte[] array, final int offset, final int length) {
        System.out.printf("[Binary payload] received by client: %s%n", StringUtils.newStringUtf8(array));
        receivedOnMessageBytes.add(array);
        countDownLatch.countDown();
    }

    @OnWebSocketMessage
    public void onWebSocketText(final String message) {
        System.out.printf("[Text payload] received by client: %s%n", message);
        receivedOnMessageText.add(message.trim());
        countDownLatch.countDown();
    }

    @OnWebSocketClose
    public void onWebSocketClose(final int statusCode, final String reason) {
        System.out.printf("Socket closed by server: %s %s%n", statusCode, reason);
        receivedOnCloseText.add(reason.trim());
        receivedOnCloseStatus.add(statusCode);
        countDownLatch.countDown();
    }

    @OnWebSocketError
    public void onWebSocketError(final Throwable cause) {
        System.err.printf("Socket error: %s", cause.getMessage());
    }

    public void awaitCountDownLatchWithAssertion() throws InterruptedException {
        assertThat(this.countDownLatch.await(3, TimeUnit.SECONDS)).isTrue();
    }

    public void scheduleDelayedSend(final Session session, final String clientRequestText, long delay) {
        final Runnable runnable = () -> {
            try {
                session.getRemote().sendString(clientRequestText);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        SCHEDULED_EXECUTOR_SERVICE.schedule(
                runnable,
                // Generate a random int between 50 (incl.) and 300 (incl.)
                RANDOM.nextInt(301 - 50) + 50,
                TimeUnit.MILLISECONDS);
    }

    public void scheduleRandomDelayedSend(final Session session, final String clientRequestText) {
        scheduleDelayedSend(session, clientRequestText, RANDOM.nextInt(301 - 50) + 50);
    }

    public void normalClose(final Session session) {
        session.close(new CloseStatus(1000, "=> Bye bye from client!"));
    }
}
