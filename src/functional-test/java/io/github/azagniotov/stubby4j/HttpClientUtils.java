package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.ProxySelector;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static io.github.azagniotov.stubby4j.HttpClientUtils.SslContextFlavor.SELF_SIGNED_CERTIFICATE_TRUST_STRATEGY;
import static java.util.Arrays.asList;

public final class HttpClientUtils {

    private HttpClientUtils() {

    }

    static CloseableHttpClient buildHttpClient(final String tlsVersion, final SslContextFlavor flavor) throws Exception {
        if (flavor == SELF_SIGNED_CERTIFICATE_TRUST_STRATEGY) {
            return buildHttpClient(tlsVersion, buildSSLContextWithTrustSelfSignedStrategy(tlsVersion));
        } else {
            return buildHttpClient(tlsVersion, buildSSLContextWithRemoteCertificateLoaded(tlsVersion));
        }
    }

    private static CloseableHttpClient buildHttpClient(final String tlsVersion, final SSLContext sslContext) throws Exception {

        System.out.println("Running tests using TLS version: " + tlsVersion);

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setEnabledProtocols(new String[]{tlsVersion});
        System.out.println("SSLEngine [client] enabled protocols: ");
        System.out.println(new HashSet<>(asList(engine.getEnabledProtocols())));

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new DefaultHostnameVerifier());

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig
                        .custom()
                        .setSocketTimeout(45000)
                        .setConnectTimeout(45000)
                        .build())
                // When .useSystemProperties(), the FakeX509TrustManager gets exercised
                //.useSystemProperties()
                .setSSLSocketFactory(sslSocketFactory)
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

    static SSLContext buildSSLContextWithTrustSelfSignedStrategy(final String tlsVersion) throws Exception {
        return SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
                .build();
    }

    static SSLContext buildSSLContextWithRemoteCertificateLoaded(final String tlsVersion) throws Exception {
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
        // 4. Load the generated FILE_NAME.jks file into the trust store of your client client by creating a KeyStore
        // ---------------------------------------------------------------------------------
        return SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(SslUtils.STUBBY_SELF_SIGNED_TRUST_STORE, null)
                .build();
    }

    enum SslContextFlavor {
        SERVER_SELF_SIGNED_CERTIFICATE_LOADED,
        SELF_SIGNED_CERTIFICATE_TRUST_STRATEGY
    }
}
