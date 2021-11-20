package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;
import static java.util.Arrays.asList;

public class StubsPortalTlsWithAlpnHttp2ProtocolTests {

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

        final URL jsonContentUrl = StubsPortalTlsWithAlpnHttp2ProtocolTests.class.getResource("/json/response/json_response_1.json");
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

    private void makeRequestAndAssert(final String tlsProtocol) throws Exception {
        final SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setProtocol(tlsProtocol);
        sslContextFactory.setTrustStore(SslUtils.SELF_SIGNED_CERTIFICATE_TRUST_STORE);

        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.addBean(sslContextFactory);

        final HttpClientTransportOverHTTP2 transport = new HttpClientTransportOverHTTP2(http2Client);
        transport.setUseALPN(true);

        final HttpClient httpClient = new HttpClient(transport, sslContextFactory);
        httpClient.start();

        ContentResponse response = httpClient.newRequest("localhost", STUBS_SSL_PORT)
                .path("/invoice?status=active&type=full")
                .method(HttpMethod.GET)
                .scheme(HttpScheme.HTTPS.asString())
                .timeout(5, TimeUnit.SECONDS)
                .send();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.getContentAsString());
    }
}
