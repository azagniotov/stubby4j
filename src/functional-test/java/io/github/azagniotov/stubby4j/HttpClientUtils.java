package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.ProxySelector;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public final class HttpClientUtils {

    private HttpClientUtils() {

    }

    static CloseableHttpClient buildHttpClient(final String tlsVersion) throws Exception {
        return buildHttpClient(tlsVersion, buildSSLContextWithRemoteCertificateLoaded(tlsVersion));
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
        // 4. Load the generated FILE_NAME.jks file into the trust store of SSLContext, which then can be
        //    used to create an SSL socket factory for your web client. The STUBBY_SELF_SIGNED_TRUST_STORE
        //    was created using the following code:
        //    https://github.com/azagniotov/stubby4j/blob/737f1f16650ce78a9a63f8f3e23c60ba2769cdb4/src/main/java/io/github/azagniotov/stubby4j/server/ssl/SslUtils.java#L168-L172
        // ---------------------------------------------------------------------------------
        return SSLContexts.custom()
                .setProtocol(tlsVersion)
                .loadTrustMaterial(SslUtils.SELF_SIGNED_CERTIFICATE_TRUST_STORE, null)
                .build();
    }
}
