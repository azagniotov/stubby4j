package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.builders.yaml.YAMLBuilder;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

public class StubsAdminPortalsTest {

    private static final int STUBS_PORT = 5792;
    private static final int STUBS_SSL_PORT = 5793;
    private static final int ADMIN_PORT = 5799;

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsAdminPortalsTest.class.getResource("/yaml/stubs.yaml");
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
        assertThat(adminPortalResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    public void should_UpdateStubsDataAndGetNewResource_WhenSuccessfulValidPostMade_ToAdminPortalRoot() throws Exception {

        final String yamlToUpdate = new YAMLBuilder()
                .newStubbedRequest()
                .withUrl("^/new/resource/.*$")
                .withMethodGet()
                .withQuery("someKey", "someValue")
                .newStubbedResponse()
                .withHeaderContentType("application/xml")
                .withLiteralBody("OK")
                .withStatus("200")
                .build();

        final String adminRequestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, adminRequestUrl, yamlToUpdate);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(statusMessage).isEqualTo("Created");
        assertThat(responseMessage).contains("Configuration created successfully");

        final String stubsRequestUrl = String.format("%s%s", STUBS_URL, "/new/resource/account?someKey=someValue");
        final HttpRequest stubsGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse stubsGetResponse = stubsGetRequest.execute();

        final String stubsGetResponseContentTypeHeader = stubsGetResponse.getContentType();
        final String stubsGetResponseContent = stubsGetResponse.parseAsString().trim();

        assertThat(stubsGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(stubsGetResponseContent).isEqualTo("OK");
        assertThat(stubsGetResponseContentTypeHeader).contains("application/xml");
    }

    @Test
    public void should_AdjustResourceIdHeaderAccordingly_WhenSuccessfulDeleteMade() throws Exception {

        final String stubsRequestUrl = String.format("%s%s", STUBS_URL, "/this/stub/should/always/be/second/in/this/file");
        final HttpRequest stubsGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse preDeletionStubGetResponse = stubsGetRequest.execute();
        final HttpHeaders preDeletionResponseHeaders = preDeletionStubGetResponse.getHeaders();
        assertThat(preDeletionResponseHeaders.containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isTrue();
        assertThat(preDeletionResponseHeaders.getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo("1");

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, String.format("%s%s", ADMIN_URL, "/0"));
        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();
        assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
        assertThat(deleteResponseContent).isEqualTo("Stub request index#0 deleted successfully");

        final HttpRequest postDeletionStubGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse postDeletionStubGetResponse = postDeletionStubGetRequest.execute();
        final HttpHeaders postDeletionResponseHeaders = postDeletionStubGetResponse.getHeaders();
        assertThat(postDeletionResponseHeaders.containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isTrue();
        assertThat(postDeletionResponseHeaders.getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo("0");
    }

    @Test
    public void should_ReturnAjaxRequestContent_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/request/post");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains(postContent);
    }

    @Test
    public void should_ReturnAjaxResponseContent_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/response/body");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains(responseContent);
    }

    @Test
    public void should_ReturnAjaxSequencedResponseContent_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/sequenced/responses/infile");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/response/1/file");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains("Still going strong!");
    }

    @Test
    public void should_ReturnAjaxHttpLifecycleYAMLResponse_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/httplifecycle/completeYAML");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains(
                "- request:" + BR +
                        "    method: POST" + BR +
                        "    url: /invoice/new" + BR +
                        "    headers:" + BR +
                        "      content-type: " + Common.HEADER_APPLICATION_JSON + BR +
                        "    post: |" + BR +
                        "      {\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}" + BR +
                        "  response:" + BR +
                        "    headers:" + BR +
                        "      content-type: " + Common.HEADER_APPLICATION_JSON + BR +
                        "      pragma: no-cache" + BR +
                        "    status: 201" + BR +
                        "    body: |" + BR +
                        "      {\"id\": \"456\", \"status\": \"created\"}");
    }

    @Test
    public void should_ReturnAjaxStubRequestYAMLResponse_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/httplifecycle/requestAsYAML");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains(
                "request:" + BR +
                        "  method: POST" + BR +
                        "  url: /invoice/new" + BR +
                        "  headers:" + BR +
                        "    content-type: application/json" + BR +
                        "  post: |" + BR +
                        "    {\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}");
    }

    @Test
    public void should_ReturnAjaxStubResponseYAMLResponse_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/httplifecycle/responseAsYAML");
        final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

        final HttpResponse ajaxResponse = ajaxRequest.execute();
        assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(ajaxResponse.parseAsString().trim()).contains(
                "response:" + BR +
                        "  headers:" + BR +
                        "    content-type: application/json" + BR +
                        "    pragma: no-cache" + BR +
                        "  status: 201" + BR +
                        "  body: |" + BR +
                        "    {\"id\": \"456\", \"status\": \"created\"}");
    }
}