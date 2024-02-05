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

package io.github.azagniotov.stubby4j;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.HttpClientUtils.jettyHttpClientOnHttp11WithClientSsl;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.websocket.StubsServerWebSocket.EMPTY_BYTE_BUFFER;
import static org.junit.Assert.assertThrows;

import io.github.azagniotov.stubby4j.annotations.PotentiallyFlaky;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// There are jetty specific tests that I want to run last, hence the MethodSorters.NAME_ASCENDING,
// since one of the tests will close the server websocket and stubby4j will have to be restarted.
// Normally, it is a bad practice to have some interdependency in tests and should be avoided.
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubsPortalHttp11WebSocketOverTlsTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String WEBSOCKET_SSL_ROOT_PATH_URL = String.format("wss://localhost:%s/ws", STUBS_SSL_PORT);

    private static final URI REQUEST_URL_HELLO_1 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/1"));
    private static final URI REQUEST_URL_HELLO_2 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/2"));
    private static final URI REQUEST_URL_HELLO_3 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/3"));
    private static final URI REQUEST_URL_HELLO_4 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/4"));
    private static final URI REQUEST_URL_HELLO_5 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/5"));
    private static final URI REQUEST_URL_HELLO_6 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/6"));
    private static final URI REQUEST_URL_HELLO_7 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/7"));
    private static final URI NON_STUBBED_REQUEST_URL =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/blah"));

    private static WebSocketClient client;

    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalHttp11WebSocketOverTlsTests.class.getResource(
                "/yaml/main-test-stubs-with-web-socket-config.yaml");
        assert url != null;

        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        // Do not pass in flag: "--enable_tls_with_alpn_and_http_2"
        //
        // WebSocket Bootstrap from HTTP/2 (RFC8441) not supported in Jetty 9.x
        // https://github.com/eclipse/jetty.project/blob/f86a719bce89844337e4f2bde68e8e147095ed80/jetty-websocket/websocket-server/src/main/java/org/eclipse/jetty/websocket/server/WebSocketServerFactory.java#L585
        //
        // Also, they added WebSocket Bootstrap from HTTP/2 (RFC8441) only in Jetty 10.x.x:
        // https://github.com/eclipse/jetty.project/pull/3740
        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, JettyFactory.DEFAULT_HOST, url.getFile(), "");

        client = new WebSocketClient(jettyHttpClientOnHttp11WithClientSsl(TLS_v1_2));
        client.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        client.stop();
        STUBBY_CLIENT.stopJetty();
    }

    private static InputStream readResourceAsInputStream(final String testResource) throws IOException {
        final URL url = StubsPortalHttp11WebSocketOverTlsTests.class.getResource(testResource);
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
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

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
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();
    }

    @Test
    public void serverOnOpen_SendsExpected_BinaryMessage() throws Exception {
        // The socket that receives server events
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_7);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_7, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final byte[] expectedBytes = StringUtils.getBytesUtf8("E.T., call home");

        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnOpen_SendsExpected_BinaryMessageFromFile() throws Exception {
        // The socket that receives server events
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_3);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_3, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream binaryDataInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[binaryDataInputStream.available()];
        binaryDataInputStream.read(expectedBytes);

        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnOpen_RespondsWithExpected_ContinuousPing() throws Exception {
        // The socket that receives server events
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(5);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_6);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_6, clientUpgradeRequest);
        sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(5);

        final byte[] emptyArray = EMPTY_BYTE_BUFFER.array();
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(emptyArray);
        assertThat(socket.receivedOnMessageBytes.get(1)).isEqualTo(emptyArray);
        assertThat(socket.receivedOnMessageBytes.get(2)).isEqualTo(emptyArray);
        assertThat(socket.receivedOnMessageBytes.get(3)).isEqualTo(emptyArray);
        assertThat(socket.receivedOnMessageBytes.get(4)).isEqualTo(emptyArray);
    }

    @Test
    public void serverOnMessage_ReactsToClient_RealBinaryMessage_AndRespondsWithExpected_BinaryMessage_OnceOnly()
            throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

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
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_ReactsToClient_TextBinaryMessage_AndRespondsWithExpected_BinaryMessage_OnceOnly()
            throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_4);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_4, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        final URL resource =
                StubsPortalHttp11WebSocketOverTlsTests.class.getResource("/json/response/json_response_6.json");
        final Path path = Paths.get(resource.toURI());
        final byte[] payloadBytes = FileUtils.fileToBytes(path.toFile());

        session.getRemote().sendBytes(ByteBuffer.wrap(payloadBytes));

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/binary/hello-world.pdf");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_TextMessage_OnceOnly() throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(2);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("hello");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(2);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();
        assertThat(socket.receivedOnMessageText.contains("bye-bye")).isTrue();
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_TextMessage_ContinuousPush() throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(5);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("do push");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(5);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();
        socket.receivedOnMessageText.remove("You have been successfully connected");

        assertThat(socket.receivedOnMessageText.get(0)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(1)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(2)).isEqualTo("pushing");
        assertThat(socket.receivedOnMessageText.get(3)).isEqualTo("pushing");
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_OnceOnly() throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(2);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_2);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_2, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("JSON");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream binaryDataInputStream = readResourceAsInputStream("/json/response/json_response_1.json");
        final String expectedResponseData = StringUtils.inputStreamToString(binaryDataInputStream);
        final String actualResponseData = StringUtils.newStringUtf8(socket.receivedOnMessageBytes.get(0));

        assertThat(expectedResponseData).isEqualTo(actualResponseData);
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_ContinuousPush() throws Exception {
        // The socket that receives server events
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(5);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_2);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_2, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("push PDF");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();
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
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(3);
        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_1);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_1, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("disconnect with a message");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(2);
        assertThat(socket.receivedOnMessageText.contains("You have been successfully connected"))
                .isTrue();
        assertThat(socket.receivedOnMessageText.contains("bon-voyage")).isTrue();

        assertThat(socket.receivedOnCloseText.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseText.contains("bye")).isTrue();

        assertThat(socket.receivedOnCloseStatus.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseStatus.contains(StatusCode.NORMAL)).isTrue();
    }

    @Test
    public void serverOnMessage_RespondsWithExpected_BinaryMessage_FragmentedFrames() throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_5);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_5, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendString("send-fragmentation-pls");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        final InputStream expectedBytesInputStream = readResourceAsInputStream("/json/response/json_response_1.json");
        final byte[] expectedBytes = new byte[expectedBytesInputStream.available()];
        expectedBytesInputStream.read(expectedBytes);
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(expectedBytes);
    }

    @Test
    public void serverShouldThrow_WhenConnectingClient_RequestedWrongUrl() throws Exception {
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);
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
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);
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
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);
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

    @Test
    public void webSocketProtocol_jettySanityCheck_jettyRespondsWithExpected_PongMessage() throws Exception {
        // This test makes sure that stubby4j did not mess up default Jetty
        // behavior that conforms to the RFC of the Web Socket protocol:
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.3

        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_5);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_5, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendPing(ByteBuffer.wrap(new byte[0]));

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        // Checking that stubby4j web sockets behavior conforms to:
        // A Pong frame sent in response to a Ping frame
        // must have identical "Application data" as found
        // the message body of the Ping frame being replied to
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.3
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo(EMPTY_BYTE_BUFFER.array());
    }

    @Test
    public void webSocketProtocol_jettySanityCheck_jettyRespondsWithExpected_PongWithDataMessage() throws Exception {
        // This test makes sure that stubby4j did not mess up default Jetty
        // behavior that conforms to the RFC of the Web Socket protocol:
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.3

        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_5);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_5, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.getRemote().sendPing(ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8)));

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        // Checking that stubby4j web sockets behavior conforms to:
        // A Pong frame sent in response to a Ping frame
        // must have identical "Application data" as found
        // the message body of the Ping frame being replied to
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.3
        assertThat(socket.receivedOnMessageBytes.get(0)).isEqualTo("ping".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @PotentiallyFlaky(
            "This test can potentially can affect other tests because it closes the socket, something weird is happening under the hood, investigate later...")
    public void webSocketProtocol_jettySanityCheck_jettyShouldDisconnect_WhenClientClosesSocket() throws Exception {
        // This test makes sure that stubby4j did not mess up default Jetty
        // behavior that conforms to the RFC of the Web Socket protocol:
        // https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.3

        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_5);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_5, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        session.close(StatusCode.SHUTDOWN, "Bye!");

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnCloseStatus.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseStatus.get(0)).isEqualTo(StatusCode.SHUTDOWN);

        assertThat(socket.receivedOnCloseText.size()).isEqualTo(1);
        assertThat(socket.receivedOnCloseText.get(0)).isEqualTo("Bye!");
    }
}
