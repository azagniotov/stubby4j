package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.SSLv3;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_0;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_1;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;

public class StubsPortalTlsProtocolTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);

    private static final String REQUEST_URL = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");

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

        final URL jsonContentUrl = StubsPortalTlsProtocolTests.class.getResource("/json/response/json_response_1.json");
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
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithSslVersion_SSLv3() throws Exception {
        makeRequestAndAssert(buildHttpClient(SSLv3));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_0() throws Exception {
        makeRequestAndAssert(buildHttpClient(TLS_v1_0));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_1() throws Exception {
        makeRequestAndAssert(buildHttpClient(TLS_v1_1));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_2() throws Exception {
        makeRequestAndAssert(buildHttpClient(TLS_v1_2));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_3() throws Exception {
        makeRequestAndAssert(buildHttpClient(TLS_v1_3));
    }

    private CloseableHttpClient buildHttpClient(final String tlsVersion) throws Exception {

        System.out.println("Running tests using TLS version: " + tlsVersion);

        // https://www.baeldung.com/httpclient-ssl
        final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        final SSLContext sslContext = SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setEnabledProtocols(new String[]{tlsVersion});
        System.out.println("SSLEngine [client] enabled protocols: ");
        System.out.println(new HashSet<>(Arrays.asList(engine.getEnabledProtocols())));

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{tlsVersion},
                null,
                new NoopHostnameVerifier());

        final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslSocketFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig
                        .custom()
                        .setSocketTimeout(45000)
                        .setConnectTimeout(45000)
                        .build())
                .setSSLContext(sslContext)
                .setSSLSocketFactory(sslSocketFactory)
                .setConnectionManager(new BasicHttpClientConnectionManager(socketFactoryRegistry))
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .setConnectionTimeToLive(-1, TimeUnit.MILLISECONDS)
                .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                .disableRedirectHandling()

                // In ProxyConfigWithStubsTest.shouldReturnProxiedRequestResponse_WhenStubsWereNotMatched():
                //
                // I had to set this header to avoid "Not in GZIP format java.util.zip.ZipException: Not in GZIP format" error:
                // The 'null' overrides the default value "gzip", also I had to .disableContentCompression() on WEB_CLIENT
                .disableContentCompression()
                .disableAutomaticRetries()
                .build();
    }

    private void makeRequestAndAssert(CloseableHttpClient httpClient) throws IOException {
        try (final CloseableHttpResponse response = httpClient.execute(new HttpGet(REQUEST_URL))) {

            final HttpEntity responseEntity = response.getEntity();

            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat(expectedContent).isEqualTo(EntityUtils.toString(responseEntity));
            assertThat(responseEntity.getContentType().getValue()).contains(HEADER_APPLICATION_JSON);
        }
    }
}
