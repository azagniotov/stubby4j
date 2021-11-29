package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.HttpClientUtils.jettyHttpClientWithClientSsl;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static org.junit.Assert.assertThrows;

public class StubsPortalWebSocketProtocolTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String WEBSOCKET_SSL_ROOT_PATH_URL = String.format("wss://localhost:%s/ws", STUBS_SSL_PORT);

    private static final URI REQUEST_URL_HELLO_1 = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/1"));
    private static final URI REQUEST_URL_HELLO_2 = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/2"));
    private static final URI REQUEST_URL_HELLO_3 = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/3"));
    private static final URI REQUEST_URL_HELLO_4 = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/4"));
    private static final URI REQUEST_URL_HELLO_5 = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/5"));
    private static final URI NON_STUBBED_REQUEST_URL = URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/blah"));

    private static WebSocketClient client;

    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalWebSocketProtocolTests.class.getResource("/yaml/main-test-stubs-with-web-socket-config.yaml");
        assert url != null;

        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());

        client = new WebSocketClient(jettyHttpClientWithClientSsl(TLS_v1_2));
        client.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        client.stop();
        STUBBY_CLIENT.stopJetty();
    }

    private static InputStream readResourceAsInputStream(final String testResource) throws IOException {
        final URL url = StubsPortalWebSocketProtocolTests.class.getResource(testResource);
        assert url != null;

        return url.openStream();
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
    public void webSocketSessionShouldHaveValidState() throws Exception {
        // The socket that receives server events
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        assertThat(session).isNotNull();
        assertThat(session.isOpen()).isTrue();
        assertThat(session.isSecure()).isTrue();
        assertThat(session.getPolicy().getBehavior()).isEqualTo(WebSocketBehavior.CLIENT);

        assertThat(session.getUpgradeResponse().getAcceptedSubProtocol()).isEqualTo("mamba,echo");
    }

    @Test
    public void serverOnOpen_SendsExpected_TextMessage() throws Exception {
        // The socket that receives server events
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();
    }

    @Test
    public void serverOnOpen_SendsExpected_BinaryMessage() throws Exception {
        // The socket that receives server events
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_3);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_3, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream binaryDataInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[binaryDataInputStream.available()];
        binaryDataInputStream.read(expectedBytes);

        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_ReactsToClient_RealBinaryMessage_AndRespondsWithExpected_BinaryMessage_OnceOnly() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_4);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_4, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        final InputStream payloadBytesInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] payloadBytes = new byte[payloadBytesInputStream.available()];
        payloadBytesInputStream.read(payloadBytes);

        session.getRemote().sendBytes(ByteBuffer.wrap(payloadBytes));

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_ReactsToClient_TextBinaryMessage_AndRespondsWithExpected_BinaryMessage_OnceOnly() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_4);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_4, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        final URL resource = StubsPortalWebSocketProtocolTests.class.getResource("/json/response/json_response_6.json");
        final Path path = Paths.get(resource.toURI());
        final byte[] payloadBytes = FileUtils.fileToBytes(path.toFile());

        session.getRemote().sendBytes(ByteBuffer.wrap(payloadBytes));

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_TextMessage_OnceOnly() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(2);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("hello");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(2);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();
        assertThat(socket.receivedOnMessageText.contains("bye-bye")).isTrue();
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_TextMessage_ContinuousPush() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(5);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("do push");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(5);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();
        socket.receivedOnMessageText.remove("You have been successfully connected");

        assertThat(socket.receivedOnMessageText.get(0)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(1)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(2)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(3)).isEqualTo("pushing");
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_OnceOnly() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(2);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_2);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_2, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("JSON");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream binaryDataInputStream = readResourceAsInputStream("/json/response/json_response_1.json");
        final String expectedResponseData = StringUtils.inputStreamToString(binaryDataInputStream);
        final String actualResponseData = StringUtils.newStringUtf8(socket.receivedOnMessageBytes.get(0));

        assertThat(expectedResponseData).isEqualTo(actualResponseData);
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_ContinuousPush() throws Exception {
        // The socket that receives server events
        final StubsClientWebSocket socket = new StubsClientWebSocket(5);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_2);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_2, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("push PDF");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(4);

        final InputStream binaryDataInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[binaryDataInputStream.available()];
        binaryDataInputStream.read(expectedBytes);

        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(1)).isEqualTo(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(2)).isEqualTo(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(3)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_TextMessage_OnceOnly_And_Disconnects() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(3);
        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("disconnect with a message");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(2);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected")).isTrue();
        assertThat(socket.receivedOnMessageText.contains("bon-voyage")).isTrue();

        assertThat(socket.receivedOnCloseText.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseText.contains("bye")).isTrue();

        assertThat(socket.receivedOnCloseStatus.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseStatus.contains(StatusCode.NORMAL)).isTrue();
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_PartialFrames() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_5);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_5, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("send-partial-pls");

        // Wait for client to get all the messages from the server
        assertThat(socket.countDownLatch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/json/response/json_response_1.json");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverShouldThrow_WhenConnectingClient_RequestedWrongUrl() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);
        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(NON_STUBBED_REQUEST_URL);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, NON_STUBBED_REQUEST_URL, clientUpgradeRequest);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            sessionFuture.get(1000, TimeUnit.MILLISECONDS);
        });

        String expectedMessage = "Failed to upgrade to websocket: Unexpected HTTP Response Status Code: 404 Not Found";
        String actualMessage = exception.getCause().getMessage();

        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    public void serverWithSubProtocolsShouldThrow_WhenConnectingClient_RequestedWrongSubProtocol() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);
        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("non-existent-among-stubbed-ones");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            sessionFuture.get(500, TimeUnit.MILLISECONDS);
        });

        String expectedMessage = "Failed to upgrade to websocket: Unexpected HTTP Response Status Code: 403 Forbidden";
        String actualMessage = exception.getCause().getMessage();

        assertThat(actualMessage).contains(expectedMessage);
    }

    @Test
    public void serverWithSubProtocolsShouldThrow_WhenConnectingClient_RequestedNoSubProtocol() throws Exception {
        final StubsClientWebSocket socket = new StubsClientWebSocket(1);
        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);

        // Not calling the following
        // clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);

        Exception exception = assertThrows(ExecutionException.class, () -> {
            sessionFuture.get(500, TimeUnit.MILLISECONDS);
        });

        String expectedMessage = "Failed to upgrade to websocket: Unexpected HTTP Response Status Code: 403 Forbidden";
        String actualMessage = exception.getCause().getMessage();

        assertThat(actualMessage).contains(expectedMessage);
    }

    @WebSocket
    public static class StubsClientWebSocket {

        private final List<String> receivedOnMessageText;
        private final List<byte[]> receivedOnMessageBytes;

        private final List<Integer> receivedOnCloseStatus;
        private final List<String> receivedOnCloseText;

        private final CountDownLatch countDownLatch;

        public StubsClientWebSocket(final int numberOfExpectedMessages) {
            this.countDownLatch = new CountDownLatch(numberOfExpectedMessages);
            this.receivedOnMessageText = new LinkedList<>();
            this.receivedOnMessageBytes = new LinkedList<>();
            this.receivedOnCloseText = new LinkedList<>();
            this.receivedOnCloseStatus = new LinkedList<>();
        }

        @OnWebSocketConnect
        public void onWebSocketConnect(final Session session) {

        }

        @OnWebSocketMessage
        public void onWebSocketBinary(final byte[] array, final int offset, final int length) {
            receivedOnMessageBytes.add(array);
            countDownLatch.countDown();
        }

        @OnWebSocketMessage
        public void onWebSocketText(final String message) {
            System.out.printf("Received by client: %s%n", message);
            receivedOnMessageText.add(message.trim());
            countDownLatch.countDown();
        }

        @OnWebSocketClose
        public void onWebSocketClose(int statusCode, String reason) {
            System.out.printf("Socket closed by server: %s%n %s%n", statusCode, reason);
            receivedOnCloseText.add(reason.trim());
            receivedOnCloseStatus.add(statusCode);
            countDownLatch.countDown();
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable cause) {

        }
    }
}
