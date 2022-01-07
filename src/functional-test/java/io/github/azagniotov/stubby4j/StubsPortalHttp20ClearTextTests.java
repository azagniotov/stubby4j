package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.utils.StringUtils;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.HttpClientUtils.jettyHttpClientOnHttp20;

public class StubsPortalHttp20ClearTextTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

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

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, JettyFactory.DEFAULT_HOST, url.getFile(), "--enable_tls_with_alpn_and_http_2");

        final URL jsonContentUrl = StubsPortalHttp20ClearTextTests.class.getResource("/json/response/json_response_1.json");
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
    public void shouldReturnExpectedResponseWhenGetRequestMadeOver_Http2_Cleartext() throws Exception {
        makeRequestAndAssert();
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeUsingLowLevelApiOver_Http2_Cleartext() throws Exception {
        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.start();

        final String host = "localhost";
        final HttpURI.Immutable httpURI =
                HttpURI.build()
                        .uri("http://" + host + ":" + STUBS_PORT + "/invoice?status=active&type=full")
                        .asImmutable();

        final FuturePromise<Session> sessionPromise = new FuturePromise<>();
        http2Client.connect(new InetSocketAddress(host, STUBS_PORT), new ServerSessionListener.Adapter(), sessionPromise);
        final Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        final HttpFields.Immutable requestFields =
                HttpFields.build()
                        .add("User-Agent", http2Client.getClass().getName() + "/" + Jetty.VERSION)
                        .asImmutable();

        final MetaData.Request metaData = new MetaData.Request(HttpMethod.GET.asString(), httpURI, HttpVersion.HTTP_2, requestFields);
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
                    assertThat(ByteBuffer.wrap(expectedContent.getBytes(StandardCharsets.UTF_8))).isEqualTo(dataBuffer);

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

    private void makeRequestAndAssert() throws Exception {
        final HttpClient httpClient = jettyHttpClientOnHttp20();
        httpClient.start();

        ContentResponse response = httpClient.newRequest("localhost", STUBS_PORT)
                .path("/invoice?status=active&type=full")
                .method(HttpMethod.GET)
                .scheme(HttpScheme.HTTP.asString())
                //.timeout(5, TimeUnit.SECONDS)
                .send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.getContentAsString());
    }
}
