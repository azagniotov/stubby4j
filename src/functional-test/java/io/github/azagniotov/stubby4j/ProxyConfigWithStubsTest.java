package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.annotations.PotentiallyFlaky;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlBuilder;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_PROXY_CONFIG;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_PROXY_RESPONSE;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

public class ProxyConfigWithStubsTest {

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

        final URL url = ProxyConfigWithStubsTest.class.getResource("/yaml/main-test-stubs-with-proxy-config.yaml");
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
    public void should_ReturnCompleteYAMLConfig_WhenSuccessfulGetMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/");
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String responseContentAsString = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains(
                "- proxy-config:\n" +
                        "    description: this is a catch-all proxy config\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com"
        );
    }

    @Test
    @PotentiallyFlaky("This test sending the request over the wire to https://jsonplaceholder.typicode.com")
    public void shouldReturnProxiedResponseUsingDefaultProxyConfig_WhenStubsWereNotMatched_PotentiallyFlaky() throws Exception {

        // https://jsonplaceholder.typicode.com/todos/1
        final String targetUriPath = "/todos/1";

        // Stub with URL '/todos/1' does not exist, so the request will be proxied
        final String requestUrl = String.format("%s%s", STUBS_URL, targetUriPath);
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders httpHeaders = new HttpHeaders();

        // I had to set this header to avoid "Not in GZIP format java.util.zip.ZipException: Not in GZIP format" error:
        // The 'null' overrides the default value "gzip", also I had to .disableContentCompression() on WEB_CLIENT
        httpHeaders.setAcceptEncoding(null);
        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);

        assertThat(response.getHeaders().containsKey(HEADER_X_STUBBY_PROXY_RESPONSE)).isTrue();

        final String responseContent = response.parseAsString().trim();
        assertThat(responseContent).isEqualTo(
                "{" + BR +
                        "  \"userId\": 1," + BR +
                        "  \"id\": 1," + BR +
                        "  \"title\": \"delectus aut autem\"," + BR +
                        "  \"completed\": false" + BR +
                        "}");
    }

    @Test
    @PotentiallyFlaky("This test sending the request over the wire to https://jsonplaceholder.typicode.com")
    public void shouldReturnProxiedResponseUsingSpecificProxyConfig_WhenStubsWereNotMatched_PotentiallyFlaky() throws Exception {

        // https://jsonplaceholder.typicode.com/todos/1
        final String targetUriPath = "/todos/1";

        // Stub with URL '/todos/1' does not exist, so the request will be proxied
        final String requestUrl = String.format("%s%s", STUBS_URL, targetUriPath);
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders httpHeaders = new HttpHeaders();
        // I had to set this header to avoid "Not in GZIP format java.util.zip.ZipException: Not in GZIP format" error:
        // The 'null' overrides the default value "gzip", also I had to .disableContentCompression() on WEB_CLIENT
        httpHeaders.setAcceptEncoding(null);

        // The 'some-unique-name' is actually set in 'proxy-config' object defined in resources/yaml/include-proxy-config.yaml
        httpHeaders.set(HEADER_X_STUBBY_PROXY_CONFIG, "some-unique-name");
        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);

        assertThat(response.getHeaders().containsKey(HEADER_X_STUBBY_PROXY_RESPONSE)).isTrue();

        final String responseContent = response.parseAsString().trim();
        assertThat(responseContent).isEqualTo(
                "{" + BR +
                        "  \"userId\": 1," + BR +
                        "  \"id\": 1," + BR +
                        "  \"title\": \"delectus aut autem\"," + BR +
                        "  \"completed\": false" + BR +
                        "}");
    }

    @Test
    public void should_UpdateStubbedProxyConfig_WithJsonRequest_ByValidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/some-unique-name");

        ///////////////////////////////////////////////////////////////////////////////////////
        // 1st sanity check: verifying the original endpoint URL
        ///////////////////////////////////////////////////////////////////////////////////////
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).isEqualTo(
                "- proxy-config:\n" +
                        "    uuid: some-unique-name\n" +
                        "    strategy: additive\n" +
                        "    headers:\n" +
                        "      x-original-stubby4j-custom-header: custom/value\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com");


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: updating by UUID
        ///////////////////////////////////////////////////////////////////////////////////////
        final URL url = AdminPortalTest.class.getResource("/json/request/json_payload_11.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, jsonToUpdate);

        final HttpResponse httpPutResponse = httpPutRequest.execute();
        final String putResponseContent = httpPutResponse.parseAsString().trim();
        final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.CREATED_201).isEqualTo(httpPutResponse.getStatusCode());
        assertThat(putResponseLocationHeader).isEqualTo("https://UPDATED.com");
        assertThat(putResponseContent).isEqualTo("Proxy config uuid#some-unique-name updated successfully");

        ///////////////////////////////////////////////////////////////////////////////////////
        // 2nd sanity check: verifying the updated endpoint URL
        ///////////////////////////////////////////////////////////////////////////////////////
        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).isEqualTo(
                "- proxy-config:\n" +
                        "    uuid: some-unique-name\n" +
                        "    strategy: additive\n" +
                        "    headers:\n" +
                        "      x-custom-header: custom/value\n" +
                        "    properties:\n" +
                        "      endpoint: https://UPDATED.com");
    }

    @Test
    public void should_UpdateStubbedDefaultProxyConfig_WithYamlRequest_ByValidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/default");

        ///////////////////////////////////////////////////////////////////////////////////////
        // 1st sanity check: verifying the state of the stubbed 'default' proxy config by UUID
        ///////////////////////////////////////////////////////////////////////////////////////
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).isEqualTo(
                "- proxy-config:\n" +
                        "    description: this is a catch-all proxy config\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com");

        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: updating the 'default' proxy config by UUID
        ///////////////////////////////////////////////////////////////////////////////////////
        final String yamlPayload = new YamlBuilder()
                .newStubbedProxyConfig()
                .withDescription("this is a catch-all proxy config that was updated")
                .withProxyStrategyAsIs()
                .withPropertyEndpoint("https://google.com")
                .withProperty("uniqueKey", "arbitraryValue")
                .toString();

        final StubbyClient stubbyClient = new StubbyClient();

        final StubbyResponse stubbyResponse = stubbyClient.updateStubbedData(requestUrl, HttpMethod.PUT, yamlPayload);
        assertThat(HttpStatus.CREATED_201).isEqualTo(stubbyResponse.statusCode());

        final String putResponseContent = stubbyResponse.body();
        assertThat(putResponseContent).isEqualTo("Proxy config uuid#default updated successfully");

        ///////////////////////////////////////////////////////////////////////////////////////
        // 2nd sanity check: verifying the state of the updated 'default' proxy config by UUID
        ///////////////////////////////////////////////////////////////////////////////////////
        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).isEqualTo(
                "- proxy-config:\n" +
                        "    description: this is a catch-all proxy config that was updated\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://google.com\n" +
                        "      uniqueKey: arbitraryValue");
    }

    @Test
    public void should_Return400_WhenRequestingWrongURIPathForUpdatingProxyConfig() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/WRONGproxy-config/some-unique-name-two");
        final URL url = AdminPortalTest.class.getResource("/json/request/json_payload_11.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, jsonToUpdate);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenUpdatingProxyConfig_ByInvalidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/this-uuid-does-not-exist");
        final URL url = AdminPortalTest.class.getResource("/json/request/json_payload_11.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, jsonToUpdate);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenUpdatingProxyConfig_WithEmptyPayload() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/some-unique-name-two");
        final String jsonToUpdate = "";

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, jsonToUpdate);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_CreateWholeYAMLConfig_WithJsonRequest() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);

        ///////////////////////////////////////////////////////////////////////////////////////
        // 1st sanity check: verifying the original endpoint URL
        ///////////////////////////////////////////////////////////////////////////////////////
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl + "proxy-config/default");
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).isEqualTo(
                "- proxy-config:\n" +
                        "    description: this is a catch-all proxy config\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com");


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: Creating a new YAML config by POST request
        ///////////////////////////////////////////////////////////////////////////////////////
        final URL url = AdminPortalTest.class.getResource("/json/request/json_payload_12.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToCreate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, jsonToCreate);

        final HttpResponse httpPostResponse = httpPutRequest.execute();
        final String postResponseContent = httpPostResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(httpPostResponse.getStatusCode());
        assertThat(postResponseContent).isEqualTo("Configuration created successfully");

        ///////////////////////////////////////////////////////////////////////////////////////
        // 2nd sanity check: verifying the updated endpoint URL
        ///////////////////////////////////////////////////////////////////////////////////////
        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains(
                "- proxy-config:\n" +
                        "    description: this would be the default proxy config\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://google.com");
    }

    @Test
    public void should_Return400_WhenCreatingWholeConfig_WithEmptyPayload() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final String jsonToCreate = "";

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, jsonToCreate);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenCreatingWholeProxyConfig_WithDuplicateUUIDs() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);

        final URL url = AdminPortalTest.class.getResource("/json/request/json_payload_13.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToCreate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, jsonToCreate);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_DeleteStubbedProxyConfig_WhenSuccessfulDeleteMade_ToAdminPortalRootWithValidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/some-unique-name-two");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
        assertThat(deleteResponseContent).isEqualTo("Proxy config uuid#some-unique-name-two deleted successfully");
    }

    @Test
    public void should_Return400_WhenDeletingProxyConfig_ByInvalidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/this-uuid-does-not-exist");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenDeletingDefaultProxyConfig() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/default");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenRequestingWrongURIPathForDeletingProxyConfig() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/WRONGproxy-config/some-unique-name-two");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenDisplayingProxyConfig_ByInvalidUuid() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/proxy-config/this-uuid-does-not-exist");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void should_Return400_WhenRequestingWrongURIPathForDisplayingProxyConfig() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/WRONGproxy-config/some-unique-name-two");
        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        assertThat(httpDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }
}
