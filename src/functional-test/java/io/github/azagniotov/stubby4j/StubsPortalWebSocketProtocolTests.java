package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.StubsJettyNativeWebSocket;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;

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

    private static final URI WEBSOCKET_SSL_EVENTS_URL = URI.create("wss://localhost:" + STUBS_SSL_PORT + "/ws/events/1");
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
        try {
            try {
                client.start();
                // The socket that receives events
                StubsJettyNativeWebSocket socket = new StubsJettyNativeWebSocket();
                // Attempt Connect
                Future<Session> fut = client.connect(socket, WEBSOCKET_SSL_EVENTS_URL);
                // Wait for Connect
                Session session = fut.get();

                // Send a message
                session.getRemote().sendString("Hello from Client");

                // Send another message
                session.getRemote().sendString("Goodbye from Client");

                // Wait for other size to close
                socket.awaitClosure();

                // Close session
                session.close();
            } finally {
                client.stop();
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    private static HttpClient getHttpClientWithSsl() {

        final SslContextFactory sslContextFactory = new SslContextFactory.Client();

        sslContextFactory.setEndpointIdentificationAlgorithm("HTTPS");
        sslContextFactory.setProtocol(TLS_v1_3);
        sslContextFactory.setTrustStore(SslUtils.SELF_SIGNED_CERTIFICATE_TRUST_STORE);
        sslContextFactory.setExcludeCipherSuites(); // echo.websocket.org use WEAK cipher suites

        return new HttpClient(sslContextFactory);
    }
}
