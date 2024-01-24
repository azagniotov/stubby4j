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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

/**
 * @author: Alexander Zagniotov
 * Created: 4/28/13 8:53 PM
 */
public final class HttpUtils {

    private static final HttpRequestFactory WEB_CLIENT;

    static {
        /**
         * @see ApacheHttpTransport#newDefaultHttpClient()
         */
        final HttpClient apacheHttpClient = HttpClientBuilder.create()
                .useSystemProperties()
                // .setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory())
                .setSSLHostnameVerifier((hostname, session) -> true)
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .setConnectionTimeToLive(-1, TimeUnit.MILLISECONDS)
                .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .build();

        WEB_CLIENT = new ApacheHttpTransport(apacheHttpClient, false).createRequestFactory(request -> {
            request.setThrowExceptionOnExecuteError(false);
            request.setReadTimeout(45000);
            request.setConnectTimeout(45000);
        });
    }

    private HttpUtils() {}

    public static HttpRequest constructHttpRequest(final String method, final String targetUrl) throws IOException {

        return WEB_CLIENT.buildRequest(method, new GenericUrl(targetUrl), null);
    }

    public static HttpRequest constructHttpRequest(final String method, final String targetUrl, final String content)
            throws IOException {

        return WEB_CLIENT.buildRequest(
                method, new GenericUrl(targetUrl), new ByteArrayContent(null, StringUtils.getBytesUtf8(content)));
    }
}
