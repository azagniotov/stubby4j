package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import java.security.KeyStore;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.SSLv3;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_0;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_1;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;
import static java.util.Arrays.asList;

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
        makeRequestAndAssert(buildHttpClientWithTrustSelfSignedStrategy(SSLv3));

        makeRequestAndAssert(buildHttpClientWithRemoteCertificateLoaded(SSLv3));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_0() throws Exception {
        makeRequestAndAssert(buildHttpClientWithTrustSelfSignedStrategy(TLS_v1_0));

        makeRequestAndAssert(buildHttpClientWithRemoteCertificateLoaded(TLS_v1_0));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_1() throws Exception {
        makeRequestAndAssert(buildHttpClientWithTrustSelfSignedStrategy(TLS_v1_1));

        makeRequestAndAssert(buildHttpClientWithRemoteCertificateLoaded(TLS_v1_1));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_2() throws Exception {
        makeRequestAndAssert(buildHttpClientWithTrustSelfSignedStrategy(TLS_v1_2));

        makeRequestAndAssert(buildHttpClientWithRemoteCertificateLoaded(TLS_v1_2));
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_3() throws Exception {
        // The following is a bad practice: conditionally running this test only if 'TLSv1.3' is supported by the JDK
        if (new HashSet<>(asList(SslUtils.enabledProtocols())).contains(TLS_v1_3)) {
            makeRequestAndAssert(buildHttpClientWithTrustSelfSignedStrategy(TLS_v1_3));

            makeRequestAndAssert(buildHttpClientWithRemoteCertificateLoaded(TLS_v1_3));
        } else {
            assertThat(true).isTrue();
        }
    }

    private CloseableHttpClient buildHttpClientWithTrustSelfSignedStrategy(final String tlsVersion) throws Exception {
        final SSLContext sslContext = buildSSLContextWithTrustSelfSignedStrategy(tlsVersion);

        return buildHttpClient(tlsVersion, sslContext);
    }

    private CloseableHttpClient buildHttpClientWithRemoteCertificateLoaded(final String tlsVersion) throws Exception {
        final SSLContext sslContext = buildSSLContextWithRemoteCertificateLoaded(tlsVersion);

        return buildHttpClient(tlsVersion, sslContext);
    }

    private CloseableHttpClient buildHttpClient(final String tlsVersion, final SSLContext sslContext) throws Exception {

        System.out.println("Running tests using TLS version: " + tlsVersion);

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setEnabledProtocols(new String[]{tlsVersion});
        System.out.println("SSLEngine [client] enabled protocols: ");
        System.out.println(new HashSet<>(asList(engine.getEnabledProtocols())));

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new DefaultHostnameVerifier());

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

    private SSLContext buildSSLContextWithTrustSelfSignedStrategy(final String tlsVersion) throws Exception {
        return SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
                .build();
    }

    private SSLContext buildSSLContextWithRemoteCertificateLoaded(final String tlsVersion) throws Exception {
        //
        // 1. Download and save the remote self-signed certificate from the stubby4j server with TLS at localhost:7443
        //    This opens an SSL connection to the specified hostname and port and prints the SSL certificate.
        // ---------------------------------------------------------------------------------
        // $ echo quit | openssl s_client -showcerts -servername localhost -connect "localhost":7443 > FILE_NAME.pem
        //
        //
        // 2. Optionally, you can perform verification using cURL. Note: the -k (or --insecure) option is NOT used
        // ---------------------------------------------------------------------------------
        // $ curl -X GET --cacert FILE_NAME.pem  --tls-max 1.1  https://localhost:7443/hello -v
        //
        //
        // 3. Finally, load the saved self-signed certificate to a keystore
        // ---------------------------------------------------------------------------------
        // $ keytool -import -trustcacerts -alias stubby4j -file FILE_NAME.pem -keystore FILE_NAME.jks
        //
        //
        // 4. Load the generated FILE_NAME.jks file into the trust store of your client
        // ---------------------------------------------------------------------------------
        final KeyStore trustStore = KeyStore.getInstance("jks");
        trustStore.load(
                getClass().getResourceAsStream("/ssl/openssl.extracted.stubby4j.remote.jks"),
                "stubby4j".toCharArray()); // this is the password entered during the 'keytool -import ... ' command

        return SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(trustStore, null)
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
