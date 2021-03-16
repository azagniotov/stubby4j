package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

public class StubsAdminPortalsTest {

    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsAdminPortalsTest.class.getResource("/yaml/main-test-stubs.yaml");
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

    @Test
    public void should_UpdateStubsDataAndGetNewResource_WhenSuccessfulValidPostMade_ToAdminPortalRoot() throws Exception {

        final String yamlToUpdate = new YamlBuilder()
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
        assertThat(preDeletionResponseHeaders.containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID)).isTrue();
        assertThat(preDeletionResponseHeaders.getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID)).isEqualTo("1");

        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, String.format("%s%s", ADMIN_URL, "/0"));
        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();
        assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
        assertThat(deleteResponseContent).isEqualTo("Stub request index#0 deleted successfully");

        final HttpRequest postDeletionStubGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse postDeletionStubGetResponse = postDeletionStubGetRequest.execute();
        final HttpHeaders postDeletionResponseHeaders = postDeletionStubGetResponse.getHeaders();
        assertThat(postDeletionResponseHeaders.containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID)).isTrue();
        assertThat(postDeletionResponseHeaders.getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID)).isEqualTo("0");
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

        final String resourceID = response.getHeaders().getFirstHeaderStringValue(Common.HEADER_X_STUBBY_RESOURCE_ID);
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

    @Test
    public void should_UpdateStubbedResponseBody_WhenRequestUrlStringRemainsUnchanged() throws Exception {

        // Warm up the cache in StubRepository by making a request to '/this/stub/should/always/be/second/in/this/file'
        final String originalStubbedUrl = "/this/stub/should/always/be/second/in/this/file";
        final String stubsRequestUrl = String.format("%s%s", STUBS_URL, originalStubbedUrl);
        final HttpRequest firstHttpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse firstHttpGetResponse = firstHttpGetRequest.execute();
        final String firstResponseContent = firstHttpGetResponse.parseAsString().trim();

        // Sanity check 1st request to Stubs to verify what the original stubbed response body contains for the above URL
        assertThat(HttpStatus.OK_200).isEqualTo(firstHttpGetResponse.getStatusCode());
        assertThat(firstResponseContent).contains("OK");

        // Getting the original stub content by making a GET request to Admin portal using the stub index
        final String requestUrl = String.format("%s%s", ADMIN_URL, "/1"); // 2nd stub in stubs.yaml
        final HttpRequest adminHttpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        final HttpResponse adminHttpGetResponse = adminHttpGetRequest.execute();
        final String adminResponseContent = adminHttpGetResponse.parseAsString().trim();

        // Sanity check to verify what the original stubbed response body contains for the stub Admin portal gave us
        assertThat(HttpStatus.OK_200).isEqualTo(adminHttpGetResponse.getStatusCode());
        assertThat(adminResponseContent).contains("request");
        assertThat(adminResponseContent).contains("url: " + originalStubbedUrl);
        assertThat(adminResponseContent).contains("response");
        assertThat(adminResponseContent).contains("OK");
        assertThat(adminResponseContent).contains("content-type: application/json");

        // Building a YAML to update the response body for the above URL
        final String yamlToUpdate = new YamlBuilder()
                .newStubbedRequest()
                .withUrl(originalStubbedUrl)
                .withMethodGet()
                .withMethodPost()
                .withMethodPut()
                .newStubbedResponse()
                .withHeaderContentType("application/json")
                .withLiteralBody("OK Updated!")
                .withStatus("200")
                .build();

        // Making a stub update request
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, yamlToUpdate);
        final HttpResponse adminHttpPutResponse = httpPutRequest.execute();
        final String adminPutResponseContent = adminHttpPutResponse.parseAsString().trim();
        final String adminPutResponseLocationHeader = adminHttpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(adminHttpGetResponse.getStatusCode());
        assertThat(adminPutResponseLocationHeader).isEqualTo(originalStubbedUrl);
        assertThat(adminPutResponseContent).isEqualTo("Stub request index#1 updated successfully");

        // Sanity check 2nd request to Stubs to verify the updated stubbed response body for the above URL
        final HttpRequest secondHttpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
        final HttpResponse secondHttpGetResponse = secondHttpGetRequest.execute();
        final String secondResponseContent = secondHttpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(secondHttpGetResponse.getStatusCode());
        assertThat(secondResponseContent).contains("OK Updated!");
    }

    @Test
    public void should_UpdateStubbedResponseBody_WhenRequestUrlRegexRemainsUnchanged() throws Exception {

        // Warm up the cache in StubRepository by making a request to a stub with URL regex: '^/resources/asn/.*$'
        final String originalStubbedUrl = "^/resources/asn/.*$";
        final List<String> assertingRequests = new LinkedList<String>() {{
            add("/resources/asn/1");
            add("/resources/asn/2");
            add("/resources/asn/3");
            add("/resources/asn/eew97we9");
        }};

        // Sanity check 1st requests to Stubs to verify what the stubbed response body contains for the above URLs
        for (final String assertingRequest : assertingRequests) {

            final String firstRequestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            final HttpRequest firstRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, firstRequestUrl);
            final HttpResponse firstResponse = firstRequest.execute();
            final String firstResponseContent = firstResponse.parseAsString().trim();

            assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat("{\"status\": \"ASN found!\"}").isEqualTo(firstResponseContent);
        }

        // Getting the original stub content by making a GET request to Admin portal using the stub index
        final String stubAdminRequestUrl = String.format("%s%s", ADMIN_URL, "/0"); // 1st stub in stubs.yaml
        final HttpRequest adminHttpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubAdminRequestUrl);
        final HttpResponse adminHttpGetResponse = adminHttpGetRequest.execute();
        final String adminResponseContent = adminHttpGetResponse.parseAsString().trim();

        // Sanity check to verify what the original stubbed response body contains for the stub Admin portal gave us
        assertThat(HttpStatus.OK_200).isEqualTo(adminHttpGetResponse.getStatusCode());
        assertThat(adminResponseContent).contains("request");
        assertThat(adminResponseContent).contains("url: " + originalStubbedUrl);
        assertThat(adminResponseContent).contains("response");
        assertThat(adminResponseContent).contains("{\"status\": \"ASN found!\"}");
        assertThat(adminResponseContent).contains("content-type: application/json");

        // Building a YAML to update the response body for the above URL
        final String yamlToUpdate = new YamlBuilder()
                .newStubbedRequest()
                .withUrl(originalStubbedUrl)
                .withMethodGet()
                .withMethodPost()
                .withMethodPut()
                .newStubbedResponse()
                .withHeaderContentType("application/json")
                .withFoldedBody("{\"status\": \"ASN found AND UPDATED!\"}")
                .withStatus("200")
                .build();

        // Making a stub update request
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, stubAdminRequestUrl, yamlToUpdate);
        final HttpResponse adminHttpPutResponse = httpPutRequest.execute();
        final String adminPutResponseContent = adminHttpPutResponse.parseAsString().trim();
        final String adminPutResponseLocationHeader = adminHttpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(adminHttpGetResponse.getStatusCode());
        assertThat(adminPutResponseLocationHeader).isEqualTo(originalStubbedUrl);
        assertThat(adminPutResponseContent).isEqualTo("Stub request index#0 updated successfully");

        // Sanity check 2nd requests to Stubs to verify the updated stubbed response body for the above URLs
        for (final String assertingRequest : assertingRequests) {

            final String secondRequestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            final HttpRequest secondRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, secondRequestUrl);
            final HttpResponse secondResponse = secondRequest.execute();
            final String secondResponseContent = secondResponse.parseAsString().trim();

            assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat("{\"status\": \"ASN found AND UPDATED!\"}").isEqualTo(secondResponseContent);
        }
    }
}