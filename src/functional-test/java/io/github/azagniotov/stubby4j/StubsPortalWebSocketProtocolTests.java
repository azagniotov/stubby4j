package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;

public class StubsPortalWebSocketProtocolTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final URI WEBSOCKET_SSL_EVENTS_URL = URI.create("wss://localhost:" + STUBS_SSL_PORT + "/ws/events/1?snack=cashews&amount=handful&brand=off");

    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalTest.class.getResource("/yaml/main-test-stubs.yaml");
        assert url != null;

        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());

        final URL jsonContentUrl = StubsPortalWebSocketProtocolTests.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    private static HttpClient getHttpClientWithSsl() {

        final SslContextFactory sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        sslContextFactory.setProtocol(TLS_v1_3);
        sslContextFactory.setTrustStore(SslUtils.SELF_SIGNED_CERTIFICATE_TRUST_STORE);

        return new HttpClient(sslContextFactory);
    }

    @Before
    public void beforeEach() throws Exception {
        final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
        assertThat(adminPortalResponse.statusCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @After
    public void afterEach() {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void nativeJettyWebSocketsOnSsl() throws Exception {
        final WebSocketClient client = new WebSocketClient(getHttpClientWithSsl());
        final Set<String> receivedMessages = new HashSet<>();
        final CountDownLatch countDownLatch = new CountDownLatch(4);

        try {
            client.start();
            // The socket that receives server events
            final StubsClientWebSocket socket = new StubsClientWebSocket(receivedMessages, countDownLatch, new StubsClientWebSocketDelegate() {
                @Override
                public void onWebSocketConnect(final Session session) {

                }

                @Override
                public void onWebSocketText(final String message) {
                    System.out.printf("Received by client: %s%n", message);
                    receivedMessages.add(message.trim());
                    countDownLatch.countDown();
                }

                @Override
                public void onWebSocketClose(int statusCode, String reason) {

                }

                @Override
                public void onWebSocketError(final Throwable cause) {

                }
            });

            final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
            clientUpgradeRequest.setRequestURI(WEBSOCKET_SSL_EVENTS_URL);
            clientUpgradeRequest.setLocalEndpoint(socket);
            clientUpgradeRequest.setSubProtocols("chat");

            // Attempt Connect
            final Future<Session> sessionFuture = client.connect(socket, WEBSOCKET_SSL_EVENTS_URL, clientUpgradeRequest);

            // Wait for Connect
            final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);
            assertThat(session).isNotNull();
            assertThat(session.isOpen()).isTrue();
            assertThat(session.isSecure()).isTrue();
            assertThat(session.getPolicy().getBehavior()).isEqualTo(WebSocketBehavior.CLIENT);

            final UpgradeRequest upgradeRequest = session.getUpgradeRequest();
            assertThat(upgradeRequest).isNotNull();
            assertThat(upgradeRequest.getUserPrincipal()).isNull();
            assertThat(upgradeRequest.getProtocolVersion()).isEqualTo("13");
            assertThat(upgradeRequest.getMethod()).isEqualTo(HttpMethod.GET.asString());
            assertThat(upgradeRequest.getRequestURI()).isEqualTo(WEBSOCKET_SSL_EVENTS_URL);

            final Map<String, List<String>> parameterMap = upgradeRequest.getParameterMap();
            assertThat(parameterMap.get("snack")).isEqualTo(Collections.singletonList("cashews"));
            assertThat(parameterMap.get("amount")).isEqualTo(Collections.singletonList("handful"));
            assertThat(parameterMap.get("brand")).isEqualTo(Collections.singletonList("off"));
            assertThat(parameterMap.get("cost")).isNull();

            final UpgradeResponse upgradeResponse = session.getUpgradeResponse();
            assertThat(upgradeResponse).isNotNull();
            assertThat(upgradeResponse.getStatusCode()).isEqualTo(101);
            assertThat(upgradeResponse.getStatusReason()).isEqualTo("Switching Protocols");
            assertThat(upgradeResponse.getAcceptedSubProtocol()).isEqualTo("chat");

            // Send messages
            session.getRemote().sendString("/item/uri?param=value");
            session.getRemote().sendString("/items");
            session.getRemote().sendBytes(ByteBuffer.wrap("bye".getBytes(StandardCharsets.UTF_8)));

            // Wait for client to get all the messages from teh server
            assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedMessages.size()).isEqualTo(4);
            assertThat(receivedMessages.contains("Server connection OK!")).isTrue();
            assertThat(receivedMessages.contains("Item URI with query string param")).isTrue();
            assertThat(receivedMessages.contains("All items")).isTrue();
            assertThat(receivedMessages.contains("Client requested to close socket via binary message!")).isTrue();

            // Close session
            if (session.isOpen()) {
                session.close(new CloseStatus(StatusCode.NORMAL, "Thanks again"));
            }

        } finally {
            client.stop();
        }
    }

    @Test
    public void webSocketConnectionRejectWhenSubProtocolNotAccepted() throws Exception {
        final WebSocketClient client = new WebSocketClient(getHttpClientWithSsl());
        final Set<String> receivedMessages = new HashSet<>();
        final List<Integer> receivedStatusCodes = new ArrayList<>();
        final CountDownLatch errorCountDownLatch = new CountDownLatch(1);

        try {
            client.start();
            // The socket that receives server events
            final StubsClientWebSocket socket = new StubsClientWebSocket(receivedMessages, errorCountDownLatch, new StubsClientWebSocketDelegate() {
                @Override
                public void onWebSocketConnect(Session session) {

                }

                @Override
                public void onWebSocketText(String message) {

                }

                @Override
                public void onWebSocketClose(int statusCode, String reason) {
                    System.out.printf("Received by client: %s%n %s%n", statusCode, reason);
                    receivedMessages.add(reason.trim());
                    receivedStatusCodes.add(statusCode);
                    errorCountDownLatch.countDown();
                }

                @Override
                public void onWebSocketError(Throwable cause) {

                }
            });

            final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
            clientUpgradeRequest.setRequestURI(WEBSOCKET_SSL_EVENTS_URL);
            clientUpgradeRequest.setLocalEndpoint(socket);
            clientUpgradeRequest.setSubProtocols("totally-unexpected-protocol");

            // Attempt Connect
            final Future<Session> sessionFuture = client.connect(socket, WEBSOCKET_SSL_EVENTS_URL, clientUpgradeRequest);
            // Wait for Connect
            final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);
            assertThat(session).isNotNull();
            assertThat(session.isOpen()).isTrue();
            assertThat(session.isSecure()).isTrue();
            assertThat(session.getPolicy().getBehavior()).isEqualTo(WebSocketBehavior.CLIENT);

            // Wait for client to get all the messages from teh server
            assertThat(errorCountDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedMessages.size()).isEqualTo(1);
            assertThat(receivedMessages.contains("Unexpected sub-protocol")).isTrue();
            assertThat(receivedStatusCodes.size()).isEqualTo(1);
            assertThat(receivedStatusCodes.get(0)).isEqualTo(StatusCode.PROTOCOL);

        } finally {
            client.stop();
        }
    }

    public interface StubsClientWebSocketDelegate {
        void onWebSocketConnect(final Session session);

        void onWebSocketText(final String message);

        void onWebSocketClose(int statusCode, final String reason);

        void onWebSocketError(final Throwable cause);
    }

    @WebSocket
    public static class StubsClientWebSocket {

        private final Set<String> receivedMessages;
        private final CountDownLatch countDownLatch;
        private final StubsClientWebSocketDelegate webSocketDelegate;

        public StubsClientWebSocket(final Set<String> receivedMessages,
                                    final CountDownLatch countDownLatch,
                                    final StubsClientWebSocketDelegate webSocketDelegate) {
            this.receivedMessages = receivedMessages;
            this.countDownLatch = countDownLatch;
            this.webSocketDelegate = webSocketDelegate;
        }

        @OnWebSocketConnect
        public void onWebSocketConnect(final Session session) {
            webSocketDelegate.onWebSocketConnect(session);
        }

        @OnWebSocketMessage
        public void onWebSocketText(final String message) {
            webSocketDelegate.onWebSocketText(message);
        }

        @OnWebSocketClose
        public void onWebSocketClose(int statusCode, String reason) {
            webSocketDelegate.onWebSocketClose(statusCode, reason);
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable cause) {
            webSocketDelegate.onWebSocketError(cause);
        }
    }
}
