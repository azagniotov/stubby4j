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
import static io.github.azagniotov.stubby4j.HttpClientUtils.jettyClientSslContextFactory;
import static io.github.azagniotov.stubby4j.HttpClientUtils.jettyHttpClientOnHttp20WithClientSsl;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;
import static java.util.Arrays.asList;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StubsPortalHttp20OverTlsWithAlpnProtocolTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

    private static String stubsData;
    private static String expectedContent;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalHttp20OverTlsWithAlpnProtocolTests.class.getResource("/yaml/main-test-stubs.yaml");
        assert url != null;

        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(
                STUBS_PORT,
                STUBS_SSL_PORT,
                ADMIN_PORT,
                JettyFactory.DEFAULT_HOST,
                url.getFile(),
                "--enable_tls_with_alpn_and_http_2");

        final URL jsonContentUrl =
                StubsPortalHttp20OverTlsWithAlpnProtocolTests.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    @Before
    public void beforeEach() throws Exception {
        // Admin portal is not running on HTTP/2 via the TLS with ALPN
        final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
        assertThat(adminPortalResponse.statusCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @After
    public void afterEach() {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void shouldReturnExpectedResponseWhenHttp2GetRequestMadeOnOverTlsWithAlpn_TlsVersion_1_2() throws Exception {
        makeRequestAndAssert(TLS_v1_2);
    }

    @Test
    public void shouldReturnExpectedResponseWhenHttp2GetRequestMadeOnOverTlsWithAlpn_TlsVersion_1_3() throws Exception {
        // The following is a bad practice: conditionally running this test only if 'TLSv1.3' is supported by the JDK
        if (new HashSet<>(asList(SslUtils.enabledProtocols())).contains(TLS_v1_3)) {
            makeRequestAndAssert(TLS_v1_3);
        } else {
            assertThat(true).isTrue();
        }
    }

    @Test
    public void shouldReturnExpectedResponseUsingLowLevelHttp2ClientApiOverTlsWithAlpn_TlsVersion_1_3()
            throws Exception {
        final SslContextFactory sslContextFactory = jettyClientSslContextFactory(TLS_v1_3);

        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.addBean(sslContextFactory);
        http2Client.start();

        final String host = "localhost";
        final HttpURI httpURI =
                new HttpURI("https://" + host + ":" + STUBS_SSL_PORT + "/invoice?status=active&type=full");

        final FuturePromise<Session> sessionPromise = new FuturePromise<>();
        http2Client.connect(
                sslContextFactory,
                new InetSocketAddress(host, STUBS_SSL_PORT),
                new ServerSessionListener.Adapter(),
                sessionPromise);
        final Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        final HttpFields requestFields = new HttpFields();
        requestFields.put("User-Agent", http2Client.getClass().getName() + "/" + Jetty.VERSION);

        final MetaData.Request metaData =
                new MetaData.Request(HttpMethod.GET.asString(), httpURI, HttpVersion.HTTP_2, requestFields);
        final HeadersFrame headersFrame = new HeadersFrame(metaData, null, true);

        // A Phaser may be used instead of a CountDownLatch to control a one-shot action serving a
        // variable number of parties. The typical idiom is for the method setting this up to first
        // register, then start the actions, then deregister
        final Phaser phaser = new Phaser(2);

        final CompletableFuture<String> byteBufferCompletableFuture = new CompletableFuture<>();
        session.newStream(headersFrame, new Promise.Adapter<>(), new Stream.Listener.Adapter() {
            @Override
            public void onHeaders(final Stream stream, final HeadersFrame frame) {

                assertThat(stream.getId() > 0).isTrue();
                assertThat(stream.getId()).isEqualTo(frame.getStreamId());
                assertThat(frame.getMetaData().isResponse()).isTrue();

                final MetaData.Response response = (MetaData.Response) frame.getMetaData();
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

                if (frame.isEndStream()) {
                    phaser.arrive();
                }
            }

            @Override
            public void onData(final Stream stream, final DataFrame frame, final Callback callback) {

                if (!frame.isEndStream()) {
                    // Get the content buffer.
                    final ByteBuffer dataBuffer = frame.getData();
                    assertThat(ByteBuffer.wrap(expectedContent.getBytes(StandardCharsets.UTF_8)))
                            .isEqualTo(dataBuffer);

                    // Consume the buffer, here - as an example - just log it.
                    final CharBuffer decodedData = StandardCharsets.UTF_8.decode(dataBuffer);
                    System.out.println("Consuming buffer: " + decodedData);

                    byteBufferCompletableFuture.complete(decodedData.toString());
                }

                callback.succeeded();
                if (frame.isEndStream()) {
                    phaser.arrive();
                }
            }

            @Override
            public Stream.Listener onPush(final Stream stream, final PushPromiseFrame frame) {
                phaser.register();
                return this;
            }
        });

        phaser.awaitAdvanceInterruptibly(phaser.arrive(), 5, TimeUnit.SECONDS);

        http2Client.stop();

        assertThat(byteBufferCompletableFuture.isDone()).isTrue();
        assertThat(expectedContent).isEqualTo(byteBufferCompletableFuture.get());
    }

    private void makeRequestAndAssert(final String tlsProtocol) throws Exception {
        final HttpClient httpClient = jettyHttpClientOnHttp20WithClientSsl(tlsProtocol);
        httpClient.start();

        ContentResponse response = httpClient
                .newRequest("localhost", STUBS_SSL_PORT)
                .path("/invoice?status=active&type=full")
                .method(HttpMethod.GET)
                .scheme(HttpScheme.HTTPS.asString())
                // .timeout(5, TimeUnit.SECONDS)
                .send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.getContentAsString());
    }
}
