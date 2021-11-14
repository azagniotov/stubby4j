package io.github.azagniotov.stubby4j;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;


final class HttpUtils {

    private static final HttpRequestFactory WEB_CLIENT;
    private static final SSLContext TRUST_SELF_SIGNED_STRATEGY_SSL_CONTEXT;

    static {
        try {
            TRUST_SELF_SIGNED_STRATEGY_SSL_CONTEXT = buildSSLContextWithTrustSelfSignedStrategy();
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
        /**
         * @see ApacheHttpTransport#newDefaultHttpClient()
         */
        final HttpClient apacheHttpClient = HttpClientBuilder.create()
                // When .useSystemProperties(), the FakeX509TrustManager gets exercised
                //.useSystemProperties()
                .setSSLContext(TRUST_SELF_SIGNED_STRATEGY_SSL_CONTEXT)
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

        WEB_CLIENT = new ApacheHttpTransport(apacheHttpClient, false).createRequestFactory(request -> {
            request.setThrowExceptionOnExecuteError(false);
            request.setReadTimeout(45000);
            request.setConnectTimeout(45000);
        });
    }

    private HttpUtils() {

    }

    private static SSLContext buildSSLContextWithTrustSelfSignedStrategy() throws Exception {
        return SSLContexts.custom()
                .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
                .build();
    }

    static HttpRequest constructHttpRequest(final String method, final String targetUrl) throws IOException {
        return WEB_CLIENT.buildRequest(method,
                new GenericUrl(targetUrl),
                null);
    }

    static HttpRequest constructHttpRequest(final String method, final String targetUrl, final String content) throws IOException {
        return WEB_CLIENT.buildRequest(method,
                new GenericUrl(targetUrl),
                new ByteArrayContent(null, StringUtils.getBytesUtf8(content)));
    }
}
