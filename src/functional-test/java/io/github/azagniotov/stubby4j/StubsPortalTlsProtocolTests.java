package io.github.azagniotov.stubby4j;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.SslUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.server.SslUtils.TLS_v1;
import static io.github.azagniotov.stubby4j.server.SslUtils.TLS_v1_1;
import static io.github.azagniotov.stubby4j.server.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.SslUtils.TLS_v1_3;

public class StubsPortalTlsProtocolTests {

    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);
    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalTest.class.getResource("/yaml/main-test-stubs.yaml");
        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());
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
    public void afterEach() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_0() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpRequestFactory httpClient = buildHttpClient(TLS_v1);
        final HttpResponse response = httpClient.buildRequest(HttpMethods.GET, new GenericUrl(requestUrl), null).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_1() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpRequestFactory httpClient = buildHttpClient(TLS_v1_1);
        final HttpResponse response = httpClient.buildRequest(HttpMethods.GET, new GenericUrl(requestUrl), null).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_2() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpRequestFactory httpClient = buildHttpClient(TLS_v1_2);
        final HttpResponse response = httpClient.buildRequest(HttpMethods.GET, new GenericUrl(requestUrl), null).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void shouldReturnExpectedResponseWhenGetRequestMadeOverSslWithTlsVersion_1_3() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpRequestFactory httpClient = buildHttpClient(TLS_v1_3);
        final HttpResponse response = httpClient.buildRequest(HttpMethods.GET, new GenericUrl(requestUrl), null).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    /**
     * @see ApacheHttpTransport#newDefaultHttpClient()
     */
    private HttpRequestFactory buildHttpClient(final String tlsVersion) throws Exception {

        System.out.println("Running tests using TLS version: " + tlsVersion);

        // https://www.baeldung.com/httpclient-ssl
        final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        final SSLContext sslContext = SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(null, acceptingTrustStrategy).build();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setEnabledProtocols(new String[]{TLS_v1, TLS_v1_1, TLS_v1_2, TLS_v1_3});

        System.out.println("SSLEngine [client] enabled protocols: ");
        System.out.println(new HashSet<>(Arrays.asList(engine.getEnabledProtocols())));

        final HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{tlsVersion}, /* "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" */
                null,
                allowAllHosts);

        final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslSocketFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        final HttpClient apacheHttpClient = HttpClientBuilder.create()
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

        return new ApacheHttpTransport(apacheHttpClient, false).createRequestFactory(request -> {
            request.setThrowExceptionOnExecuteError(false);
            request.setReadTimeout(45000);
            request.setConnectTimeout(45000);
        });
    }
}
