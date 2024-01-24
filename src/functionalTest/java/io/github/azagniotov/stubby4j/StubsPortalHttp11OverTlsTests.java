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

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.HttpClientUtils.buildHttpClient;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.SSLv3;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_0;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_1;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_2;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;
import static java.util.Arrays.asList;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StubsPortalHttp11OverTlsTests {

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);

    private static final String REQUEST_URL = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");

    private static String stubsData;
    private static String expectedContent;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalHttp11OverTlsTests.class.getResource("/yaml/main-test-stubs.yaml");
        assert url != null;

        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());

        final URL jsonContentUrl =
                StubsPortalHttp11OverTlsTests.class.getResource("/json/response/json_response_1.json");
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
        // The following is a bad practice: conditionally running this test only if 'TLSv1.3' is supported by the JDK
        if (new HashSet<>(asList(SslUtils.enabledProtocols())).contains(TLS_v1_3)) {
            makeRequestAndAssert(buildHttpClient(TLS_v1_3));
        } else {
            assertThat(true).isTrue();
        }
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
