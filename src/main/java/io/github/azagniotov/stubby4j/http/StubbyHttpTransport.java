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

package io.github.azagniotov.stubby4j.http;

import static io.github.azagniotov.stubby4j.common.Common.POSTING_METHODS;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.SSL_SOCKET_FACTORY;
import static io.github.azagniotov.stubby4j.utils.StringUtils.charsetUTF8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.inputStreamToString;
import static java.lang.String.valueOf;
import static java.util.Map.Entry;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_ENCODING;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_LANGUAGE;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_LENGTH;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubbyHttpTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubbyHttpTransport.class);

    private static final Set<String> SUPPORTED_METHODS = new HashSet<String>() {
        {
            add(HttpMethod.GET.asString());
            add(HttpMethod.HEAD.asString());
            add(HttpMethod.TRACE.asString());
            add(HttpMethod.OPTIONS.asString());
            add(HttpMethod.DELETE.asString());
            add(HttpMethod.POST.asString());
            add(HttpMethod.PUT.asString());
            add(HttpMethodExtended.PATCH.asString());
        }
    };

    public StubbyHttpTransport() {}

    public StubbyResponse httpRequestFromStub(final StubRequest request, final String recordingSource)
            throws Exception {
        final String method = request.getMethod().get(0);
        if (!ANSITerminal.isMute()) {
            final String logMessage = String.format(
                    "[%s] -> Making %s HTTP request from stub metadata to: [%s]",
                    ConsoleUtils.getLocalDateTime(), method, recordingSource);
            ANSITerminal.incoming(logMessage);
        }
        LOGGER.debug("Making {} HTTP request from stub metadata to: [{}].", method, recordingSource);
        return request(
                method,
                recordingSource,
                request.getPostBody(),
                request.getHeaders(),
                StringUtils.calculateStringLength(request.getPostBody()));
    }

    public StubbyResponse request(
            final String method,
            final String fullUrl,
            final String post,
            final Map<String, String> headers,
            final int postLength)
            throws Exception {

        if (!SUPPORTED_METHODS.contains(method)) {
            throw new UnsupportedOperationException(
                    String.format("HTTP method '%s' not supported when contacting stubby4j", method));
        }

        final HttpURLConnection connection = getHttpURLConnection(fullUrl);

        connection.setRequestMethod(method);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        setRequestHeaders(connection, headers, postLength);

        if (POSTING_METHODS.contains(method)) {
            writePost(connection, post);
        }

        return buildStubbyResponse(connection);
    }

    private HttpURLConnection getHttpURLConnection(final String fullUrl) throws Exception {
        final URL url = new URL(fullUrl);
        if (url.getProtocol().equalsIgnoreCase(HttpScheme.HTTPS.asString())) {
            final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            // As part of this SSL socket factory, providing a way for the web client to validate stubby4j self-signed
            // certificate, as well as making requests to the outside world using the default trust store.
            // The requests outside are made as part of StubRepository class behavior when proxying requests
            httpsURLConnection.setSSLSocketFactory(SSL_SOCKET_FACTORY);

            return httpsURLConnection;
        } else {
            return (HttpURLConnection) url.openConnection();
        }
    }

    private StubbyResponse buildStubbyResponse(final HttpURLConnection connection) throws IOException {
        try {
            connection.connect();
            final int responseCode = connection.getResponseCode();
            final Map<String, List<String>> responseHeaders = new HashMap<>(connection.getHeaderFields());
            if (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.CREATED_201) {
                try (final InputStream inputStream = connection.getInputStream()) {
                    final String responseContent = inputStreamToString(inputStream);

                    return new StubbyResponse(responseCode, responseContent, responseHeaders);
                }
            }
            return new StubbyResponse(responseCode, connection.getResponseMessage(), responseHeaders);
        } finally {
            connection.disconnect();
        }
    }

    private void setRequestHeaders(
            final HttpURLConnection connection, final Map<String, String> headers, final int postLength) {
        connection.setRequestProperty("User-Agent", StringUtils.constructUserAgentName());
        final String requestMethod = connection.getRequestMethod();
        if (POSTING_METHODS.contains(StringUtils.toUpper(requestMethod))) {
            connection.setDoOutput(true);
            connection.setRequestProperty(CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            connection.setRequestProperty(CONTENT_LANGUAGE.asString(), "en-US");
            connection.setRequestProperty(CONTENT_ENCODING.asString(), StringUtils.UTF_8);

            connection.setRequestProperty(CONTENT_LENGTH.asString(), valueOf(postLength));
            if (postLength > 0) {
                connection.setFixedLengthStreamingMode(postLength);
            } else {
                connection.setChunkedStreamingMode(0);
            }
        }

        for (final Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void writePost(final HttpURLConnection connection, final String post) throws IOException {
        try (final OutputStreamWriter outputStreamWriter =
                        new OutputStreamWriter(connection.getOutputStream(), charsetUTF8());
                final BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            bufferedWriter.write(post);
        }
    }
}
