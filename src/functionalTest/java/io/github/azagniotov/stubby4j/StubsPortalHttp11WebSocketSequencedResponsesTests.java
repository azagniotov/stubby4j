/*
 * Copyright (c) 2024 Alexander Zagniotov
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

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
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
public class StubsPortalHttp11WebSocketSequencedResponsesTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String WEBSOCKET_SSL_ROOT_PATH_URL = String.format("wss://localhost:%s/ws", STUBS_SSL_PORT);

    private static final URI REQUEST_URL_HELLO_8 =
            URI.create(String.format("%s%s", WEBSOCKET_SSL_ROOT_PATH_URL, "/demo/hello/8"));

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
    public void serverOnMessageUponReconnectsRequests_RespondsWithExpected_SequencedResponses_InLoop()
            throws Exception {
        final String clientRequestText = "Hey, server, give me fruits";

        // ###########################################################################
        //       RE-CONNECTING ON THE SAME URI AND EXPECTING THE 1st RESPONSE
        // ###########################################################################
        ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(1);

        // Asking for the 1st time
        session.getRemote().sendString(clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(0);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);

        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(0));
        socket.normalClose(session);

        // ###########################################################################
        //       RE-CONNECTING ON THE SAME URI AND EXPECTING THE 2nd RESPONSE
        // ###########################################################################
        socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(1);

        // Asking for the 2nd time
        session.getRemote().sendString(clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(0);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);

        assertThat("fruit-1").isEqualTo(socket.receivedOnMessageText.get(0));
        socket.normalClose(session);

        // ###########################################################################
        //       RE-CONNECTING ON THE SAME URI AND EXPECTING THE 3rd RESPONSE
        // ###########################################################################
        socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(1);

        // Asking for the 3rd time
        session.getRemote().sendString(clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageText.size()).isEqualTo(0);
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);

        assertExpectedBinaryResponse(socket, "fruit-2a,fruit-2b,fruit-2c,fruit-2d,fruit-2e");
        socket.normalClose(session);

        // ###########################################################################
        //       RE-CONNECTING ON THE SAME URI AND EXPECTING THE 4th RESPONSE
        // ###########################################################################
        socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(1);

        // Asking for the 4th time
        session.getRemote().sendString(clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        // Nothing in the binary response now
        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(0);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);

        assertThat("fruit-3").isEqualTo(socket.receivedOnMessageText.get(0));
        socket.normalClose(session);

        // ###########################################################################
        //       RE-CONNECTING ON THE SAME URI AND EXPECTING THE 5th RESPONSE,
        //         I.E.: EXPECTING THE 1st RESPONSE AGAIN - THE LOOP RESTARTED
        // ###########################################################################
        socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(1);

        // Asking for the 5th time
        session.getRemote().sendString(clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(0);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);

        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(0));
        socket.normalClose(session);
    }

    @Test
    public void serverOnMessageWithoutDelayedRequests_OnTheSameConnection_RespondsWith_SequencedResponses_InLoop()
            throws Exception {
        final String clientRequestText = "Hey, server, give me fruits";
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(5);

        // Asking for the 1st time
        socket.scheduleDelayedSend(session, clientRequestText, 0);
        // Asking for the 2nd time
        socket.scheduleDelayedSend(session, clientRequestText, 0);
        // Asking for the 3rd time
        socket.scheduleDelayedSend(session, clientRequestText, 0);
        // Asking for the 4th time
        socket.scheduleDelayedSend(session, clientRequestText, 0);
        // Asking for the 5th time (the sequence loop has restarted, we should get the 1st response)
        socket.scheduleDelayedSend(session, clientRequestText, 0);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(4);

        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(0));
        assertThat("fruit-1").isEqualTo(socket.receivedOnMessageText.get(1));
        assertExpectedBinaryResponse(socket, "fruit-2a,fruit-2b,fruit-2c,fruit-2d,fruit-2e");
        assertThat("fruit-3").isEqualTo(socket.receivedOnMessageText.get(2));
        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(3));

        socket.normalClose(session);
    }

    @Test
    public void serverOnMessageWithSeriallyDelayedRequests_OnTheSameConnection_RespondsWith_SequencedResponses_InLoop()
            throws Exception {
        final String clientRequestText = "Hey, server, give me fruits";
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(5);

        // Asking for the 1st time
        socket.scheduleDelayedSend(session, clientRequestText, 10L);
        // Asking for the 2nd time
        socket.scheduleDelayedSend(session, clientRequestText, 20L);
        // Asking for the 3rd time
        socket.scheduleDelayedSend(session, clientRequestText, 30L);
        // Asking for the 4th time
        socket.scheduleDelayedSend(session, clientRequestText, 40L);
        // Asking for the 5th time (the sequence loop has restarted, we should get the 1st response)
        socket.scheduleDelayedSend(session, clientRequestText, 50L);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(4);

        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(0));
        assertThat("fruit-1").isEqualTo(socket.receivedOnMessageText.get(1));
        assertExpectedBinaryResponse(socket, "fruit-2a,fruit-2b,fruit-2c,fruit-2d,fruit-2e");
        assertThat("fruit-3").isEqualTo(socket.receivedOnMessageText.get(2));
        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(3));

        socket.normalClose(session);
    }

    @Test
    public void serverOnMessageWithRandomlyDelayedRequests_OnTheSameConnection_RespondsWith_SequencedResponses_InLoop()
            throws Exception {
        final String clientRequestText = "Hey, server, give me fruits";
        final ClientWebSocketHelper socket = new ClientWebSocketHelper();
        socket.initLatch(1);

        final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        clientUpgradeRequest.setRequestURI(REQUEST_URL_HELLO_8);
        clientUpgradeRequest.setLocalEndpoint(socket);
        clientUpgradeRequest.setSubProtocols("echo", "mamba");

        final Future<Session> sessionFuture = client.connect(socket, REQUEST_URL_HELLO_8, clientUpgradeRequest);
        final Session session = sessionFuture.get(500, TimeUnit.MILLISECONDS);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(1);
        assertThat("You have been successfully connected").isEqualTo(socket.receivedOnMessageText.get(0));

        socket.initLatch(5);

        // Asking for the 1st time
        socket.scheduleRandomDelayedSend(session, clientRequestText);
        // Asking for the 2nd time
        socket.scheduleRandomDelayedSend(session, clientRequestText);
        // Asking for the 3rd time
        socket.scheduleRandomDelayedSend(session, clientRequestText);
        // Asking for the 4th time
        socket.scheduleRandomDelayedSend(session, clientRequestText);
        // Asking for the 5th time (the sequence loop has restarted, we should get the 1st response)
        socket.scheduleRandomDelayedSend(session, clientRequestText);

        // Wait for client to get all the messages from the server
        socket.awaitCountDownLatchWithAssertion();

        assertThat(socket.receivedOnMessageBytes.size()).isEqualTo(1);
        assertThat(socket.receivedOnMessageText.size()).isEqualTo(4);

        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(0));
        assertThat("fruit-1").isEqualTo(socket.receivedOnMessageText.get(1));
        assertExpectedBinaryResponse(socket, "fruit-2a,fruit-2b,fruit-2c,fruit-2d,fruit-2e");
        assertThat("fruit-3").isEqualTo(socket.receivedOnMessageText.get(2));
        assertThat("fruit-0").isEqualTo(socket.receivedOnMessageText.get(3));

        socket.normalClose(session);
    }

    private static void assertExpectedBinaryResponse(final ClientWebSocketHelper socket, final String expected) throws IOException {
        final InputStream binaryDataInputStream = readResourceAsInputStream("/json/response/json_response_7.json");
        final String expectedResponseData = StringUtils.inputStreamToString(binaryDataInputStream);
        assertThat(expectedResponseData).isEqualTo(expected);

        final String actualResponseData = StringUtils.newStringUtf8(socket.receivedOnMessageBytes.get(0));
        assertThat(expectedResponseData).isEqualTo(actualResponseData);
    }
}
