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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.server.SslUtils.ALL_TLS_VERSIONS;
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
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLEngine engine = sslContext.createSSLEngine();
        final String[] supportedProtocols = sslContext.getSupportedSSLParameters().getProtocols();
        Set<String> enabledProtocols = new LinkedHashSet<>(Arrays.asList(supportedProtocols));
        enabledProtocols.addAll(Arrays.asList(ALL_TLS_VERSIONS));
        // https://aws.amazon.com/blogs/opensource/tls-1-0-1-1-changes-in-openjdk-and-amazon-corretto/
        // https://support.azul.com/hc/en-us/articles/360061143191-TLSv1-v1-1-No-longer-works-after-upgrade-No-appropriate-protocol-error
        engine.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
        System.out.println("SSLEngine [client] enabled protocols: ");
        System.out.println(new HashSet<>(Arrays.asList(engine.getEnabledProtocols())));


        final LinkedHashSet<String> supportedCipherSuites = new LinkedHashSet<>(Arrays.asList(SslUtils.includedCipherSuites()));
        Arrays.asList(new String[]{
                "TLS_CHACHA20_POLY1305_SHA256",
                "TLS_AES_128_CCM_8_SHA256",
                "TLS_AES_128_CCM_SHA256"
        }).forEach(supportedCipherSuites::remove);

        supportedCipherSuites.addAll(
                Arrays.asList(
                        "TLS_RSA_WITH_AES_128_CBC_SHA256",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                        "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
                        "SSL_DH_anon_WITH_RC4_128_MD5",
                        "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                        "SSL_RSA_WITH_RC4_128_MD5",
                        "SSL_RSA_WITH_RC4_128_SHA",
                        "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                        "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                        "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
                        "TLS_ECDH_RSA_WITH_RC4_128_SHA",
                        "TLS_ECDH_anon_WITH_RC4_128_SHA",
                        "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
                        "TLS_KRB5_WITH_RC4_128_MD5",
                        "TLS_KRB5_WITH_RC4_128_SHA"));

        supportedCipherSuites.addAll(
                Arrays.asList(

                        // *_CHACHA20_POLY1305 are 3x to 4x faster than existing cipher suites.
                        //   http://googleonlinesecurity.blogspot.com/2014/04/speeding-up-and-strengthening-https.html
                        // Use them if available. Normative names can be found at (TLS spec depends on IPSec spec):
                        //   http://tools.ietf.org/html/draft-nir-ipsecme-chacha20-poly1305-01
                        //   http://tools.ietf.org/html/draft-mavrogiannopoulos-chacha-tls-02
//                "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305",
//                "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305",
//                "TLS_ECDHE_ECDSA_WITH_CHACHA20_SHA",
//                "TLS_ECDHE_RSA_WITH_CHACHA20_SHA",
//
//                "TLS_DHE_RSA_WITH_CHACHA20_POLY1305",
//                "TLS_RSA_WITH_CHACHA20_POLY1305",
//                "TLS_DHE_RSA_WITH_CHACHA20_SHA",
//                "TLS_RSA_WITH_CHACHA20_SHA",

                        // Done with bleeding edge, back to TLS v1.2 and below
                        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",

                        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                        "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
                        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                        "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",

                        // TLS v1.0 (with some SSLv3 interop)
                        //"TLS_DHE_RSA_WITH_AES_256_CBC_SHA384",
                        "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",

                        "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                        "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                        //"SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA",
                        //"SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA",

                        // RSA key transport sucks, but they are needed as a fallback.
                        // For example, microsoft.com fails under all versions of TLS
                        // if they are not included. If only TLS 1.0 is available at
                        // the client, then google.com will fail too. TLS v1.3 is
                        // trying to deprecate them, so it will be interesteng to see
                        // what happens.
                        "TLS_RSA_WITH_AES_256_CBC_SHA256",
                        "TLS_RSA_WITH_AES_256_CBC_SHA",
                        "TLS_RSA_WITH_AES_128_CBC_SHA256",
                        "TLS_RSA_WITH_AES_128_CBC_SHA"
                ));

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{tlsVersion}, /* "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3" */
                null,
                new NoopHostnameVerifier());

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
