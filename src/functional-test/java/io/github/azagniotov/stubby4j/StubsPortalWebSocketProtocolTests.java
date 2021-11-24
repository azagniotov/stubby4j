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
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);

    private static final String REQUEST_URL = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");

    private static final URI WEBSOCKET_SSL_EVENTS_URL = URI.create("wss://localhost:" + STUBS_SSL_PORT + "/ws/events/1?snack=cashews&amount=handful&brand=off");
    private static final URI WEBSOCKET_EVENTS_URL = URI.create("ws://localhost:" + STUBS_PORT + "/ws/events/1");

    private static String stubsData;
    private static String expectedContent;

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
        expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());
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
        sslContextFactory.setExcludeCipherSuites(); // echo.websocket.org use WEAK cipher suites

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
        WebSocketClient client = new WebSocketClient(getHttpClientWithSsl());
        client.getPolicy().setIdleTimeout(90000);

        try {
            client.start();
            // The socket that receives events
            StubsClientWebSocket socket = new StubsClientWebSocket();
            // Attempt Connect
            Future<Session> fut = client.connect(socket, WEBSOCKET_SSL_EVENTS_URL);
            // Wait for Connect
            Session session = fut.get(500, TimeUnit.MILLISECONDS);
            assertThat(session).isNotNull();
            assertThat(session.isOpen()).isTrue();
            assertThat(session).isNotNull();
            assertThat(session.getUpgradeRequest()).isNotNull();
            assertThat(session.getUpgradeResponse()).isNotNull();

            final UpgradeRequest upgradeRequest = session.getUpgradeRequest();
            final Map<String, List<String>> parameterMap = upgradeRequest.getParameterMap();

            assertThat(upgradeRequest.getProtocolVersion()).isEqualTo("13");
            assertThat(upgradeRequest.getMethod()).isEqualTo(HttpMethod.GET.asString());
            assertThat(upgradeRequest.getRequestURI()).isEqualTo(WEBSOCKET_SSL_EVENTS_URL);
            assertThat(parameterMap.get("snack")).isEqualTo(Collections.singletonList("cashews"));
            assertThat(parameterMap.get("amount")).isEqualTo(Collections.singletonList("handful"));
            assertThat(parameterMap.get("brand")).isEqualTo(Collections.singletonList("off"));
            assertThat(parameterMap.get("cost")).isNull();

            assertThat(session.getUpgradeResponse().getStatusCode()).isEqualTo(101);
            assertThat(session.getUpgradeResponse().getStatusReason()).isEqualTo("Switching Protocols");

            // Send a message
            session.getRemote().sendString("/item/uri?param=value");

            // Wait for other size to close
            socket.awaitClosure();

            // Close session
            // session.close();

        } finally {
            client.stop();
        }

    }

    @WebSocket
    public static class StubsClientWebSocket {

        private volatile Session session;
        private RemoteEndpoint remote;

        @OnWebSocketConnect
        public void onWebSocketConnect(final Session session) {
            this.session = session;
            this.remote = this.session.getRemote();

            this.remote.sendStringByFuture("Client connection OK!");
        }

        @OnWebSocketMessage
        public void onWebSocketText(final String message) {
            System.out.printf("Received by client: %s%n", message);
        }

        @OnWebSocketClose
        public void onWebSocketClose(int statusCode, String reason) {
            System.out.println("Socket Closed: [" + statusCode + "] " + reason);
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable cause) {
            cause.printStackTrace(System.err);
        }

        public void awaitClosure() throws InterruptedException {
            System.out.println("Awaiting closure from remote");
        }
    }
}
