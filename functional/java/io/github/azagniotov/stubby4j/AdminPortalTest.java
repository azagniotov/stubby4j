package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.builder.yaml.YAMLBuilder;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;

public class AdminPortalTest {

    private static final int STUBS_PORT = 5992;
    private static final int STUBS_SSL_PORT = 5993;
    private static final int ADMIN_PORT = 5999;

    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = AdminPortalTest.class.getResource("/yaml/stubs.yaml");
        final InputStream stubsDatanputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDatanputStream);
        stubsDatanputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());
    }

    @Before
    public void beforeEach() throws Exception {
        final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
        assertThat(adminPortalResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    @Test
    public void should_ReturnMethodNotImplemented_WhenSuccessfulOptionsMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.OPTIONS, requestUrl);

        final HttpResponse httpResponse = httpPutRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
        assertThat(statusMessage).isEqualTo("Not Implemented");
        assertThat(responseMessage).isEqualTo("Method OPTIONS is not implemented on URI /");
    }

    @Test
    public void should_ReturnMethodNotImplemented_WhenSuccessfulHeadMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.HEAD, requestUrl);

        final HttpResponse httpResponse = httpPutRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
        assertThat(statusMessage).isEqualTo("Not Implemented");
        assertThat(responseMessage).isEqualTo("");
    }

    @Test
    public void should_ReturnMethodNotImplemented_WhenSuccessfulTraceMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.TRACE, requestUrl);

        final HttpResponse httpResponse = httpPutRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
        assertThat(statusMessage).isEqualTo("Not Implemented");
        assertThat(responseMessage).isEqualTo("Method TRACE is not implemented on URI /");
    }

    @Test
    public void shouldMakeSuccessfulGetRequestToStatusPage() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/status");
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String responseContentAsString = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("/pdf/hello-world");
        assertThat(responseContentAsString).contains("STATUS");
        assertThat(responseContentAsString).contains("/uri/with/single/sequenced/response");
    }

    @Test
    public void should_ReturnAllStubbedRequestsAsYAML_WhenSuccessfulGetMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/");
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String responseContentAsString = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("request");
        assertThat(responseContentAsString).contains("url: ^/resources/asn/");
        assertThat(responseContentAsString).contains("content-disposition: attachment; filename=hello-world.pdf");
        assertThat(responseContentAsString).contains("file: ../json/response.json");
        assertThat(responseContentAsString).contains("url: /uri/with/single/sequenced/response");
    }

    @Test
    public void should_ReturnSingleStubbedRequestAsYAML_WhenSuccessfulGetMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String responseContentAsString = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("request");
        assertThat(responseContentAsString).contains("url: ^/resources/asn/");
        assertThat(responseContentAsString).contains("response");
        assertThat(responseContentAsString).contains("content-type: application/json");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulGetMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

        final int invalidIndex = 88888888;
        final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();

        final String expectedMessage = String.format("Stub request index#%s does not exist, cannot display", invalidIndex);

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        assertThat(statusMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulPutMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

        final HttpResponse httpResponse = httpPutRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
        assertThat(statusMessage).isEqualTo("Method Not Allowed");
        assertThat(responseMessage).isEqualTo("Method PUT is not allowed on URI /");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulEmptyPutMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s/1", ADMIN_URL);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        assertThat(statusMessage).isEqualTo("PUT request on URI /1 was empty");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulPutMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

        final int invalidIndex = 88888888;
        final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();

        final String expectedMessage = String.format("Stub request index#%s does not exist, cannot update", invalidIndex);

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        assertThat(statusMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void should_UpdateStubbedRequest_WhenSuccessfulPutMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("url: ^/resources/asn/");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/json");

        final String yamlToUpdate = new YAMLBuilder()
                .newStubbedRequest()
                .withUrl("^/resources/something/new")
                .withMethodGet()
                .withQuery("someKey", "someValue")
                .newStubbedResponse()
                .withHeaderContentType("application/xml")
                .withLiteralBody("OK")
                .withStatus("201")
                .build();

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, yamlToUpdate);

        final HttpResponse httpPutResponse = httpPutRequest.execute();
        final String putResponseContent = httpPutResponse.parseAsString().trim();
        final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(putResponseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
        assertThat(putResponseContent).isEqualTo("Stub request index#0 updated successfully");


        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("query");
        assertThat(getResponseContent).contains("url: ^/resources/something/new");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/xml");

        assertThat(getResponseContent).doesNotContain("url: ^/resources/asn/");
        assertThat(getResponseContent).doesNotContain("content-type: application/json");
    }

    @Test
    public void should_UpdateStubbedRequest_WithJsonRequest_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("url: ^/resources/asn/");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/json");

        final URL url = AdminPortalTest.class.getResource("/json/update.request.without.escaped.slashes.json");
        final InputStream jsonInputStream = url.openStream();
        final String jsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, jsonToUpdate);

        final HttpResponse httpPutResponse = httpPutRequest.execute();
        final String putResponseContent = httpPutResponse.parseAsString().trim();
        final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(putResponseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
        assertThat(putResponseContent).isEqualTo("Stub request index#0 updated successfully");


        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("query");
        assertThat(getResponseContent).contains("url: ^/resources/something/new");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/xml");

        assertThat(getResponseContent).doesNotContain("url: ^/resources/asn/");
        assertThat(getResponseContent).doesNotContain("content-type: application/json");
    }

    @Test
    public void should_UpdateStubbedRequest_WithEnquotedForwardSlashesEscapedJsonRequest_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("url: ^/resources/asn/");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/json");

        final URL url = AdminPortalTest.class.getResource("/json/update.request.with.enquoted.escaped.forward.slashes.json");
        final InputStream jsonInputStream = url.openStream();
        final String escapedJsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, escapedJsonToUpdate);

        final HttpResponse httpPutResponse = httpPutRequest.execute();
        final String putResponseContent = httpPutResponse.parseAsString().trim();
        final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(putResponseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
        assertThat(putResponseContent).isEqualTo("Stub request index#0 updated successfully");


        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("query");
        assertThat(getResponseContent).contains("url: ^/resources/something/new");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/xml");

        assertThat(getResponseContent).doesNotContain("url: ^/resources/asn/");
        assertThat(getResponseContent).doesNotContain("content-type: application/json");
    }

    @Test
    public void should_UpdateStubbedRequest_WithForwardSlashesEscapedJsonRequest_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("url: ^/resources/asn/");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/json");

        final URL url = AdminPortalTest.class.getResource("/json/update.request.with.escaped.forward.slashes.json");
        final InputStream jsonInputStream = url.openStream();
        final String escapedJsonToUpdate = StringUtils.inputStreamToString(jsonInputStream);

        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, escapedJsonToUpdate);

        final HttpResponse httpPutResponse = httpPutRequest.execute();
        final String putResponseContent = httpPutResponse.parseAsString().trim();
        final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(putResponseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
        assertThat(putResponseContent).isEqualTo("Stub request index#0 updated successfully");


        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("query");
        assertThat(getResponseContent).contains("url: ^/resources/something/new");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/xml");

        assertThat(getResponseContent).doesNotContain("url: ^/resources/asn/");
        assertThat(getResponseContent).doesNotContain("content-type: application/json");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulDeleteMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpResponse = httpPutRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
        assertThat(statusMessage).isEqualTo("Method Not Allowed");
        assertThat(responseMessage).isEqualTo("Method DELETE is not allowed on URI /");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulDeleteMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

        final int invalidIndex = 88888888;
        final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();

        final String expectedMessage = String.format("Stub request index#%s does not exist, cannot delete", invalidIndex);

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        assertThat(statusMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void should_DeleteStubbedRequest_WhenSuccessfulDeleteMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/2");
        HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse httpGetResponse = httpGetRequest.execute();
        String getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).contains("request");
        assertThat(getResponseContent).contains("url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}");
        assertThat(getResponseContent).contains("response");
        assertThat(getResponseContent).contains("content-type: application/json");


        final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

        final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
        final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
        assertThat(deleteResponseContent).isEqualTo("Stub request index#2 deleted successfully");

        httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        httpGetResponse = httpGetRequest.execute();
        getResponseContent = httpGetResponse.parseAsString().trim();

        assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
        assertThat(getResponseContent).doesNotContain("url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}");
    }


    @Test
    public void should_ReturnExpectedError_WhenSuccessfulPostMade_ToAdminPortalRootWithAnIndexURI() throws Exception {

        final int invalidIndex = 5;
        final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
        assertThat(statusMessage).isEqualTo("Method Not Allowed");
        assertThat(responseMessage).isEqualTo("Method POST is not allowed on URI /5");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulEmptyPostMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
        assertThat(statusMessage).isEqualTo("POST request on URI / was empty");
    }

    @Test
    public void should_ReturnExpectedError_WhenSuccessfulInvalidPostMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, "unparseable rubbish post content");

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();

        final String expectedMessage = "Problem handling request in Admin handler: java.io.IOException: Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML";

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        assertThat(statusMessage).isEqualTo(expectedMessage);
        assertThat(responseMessage).contains(expectedMessage);
    }

    @Test
    public void should_UpdateStubsData_WhenSuccessfulValidPostMade_ToAdminPortalRoot() throws Exception {

        final String yamlToUpdate = new YAMLBuilder()
                .newStubbedRequest()
                .withUrl("^/resources/something/new")
                .withMethodGet()
                .withQuery("someKey", "someValue")
                .newStubbedResponse()
                .withHeaderContentType("application/xml")
                .withLiteralBody("OK")
                .withStatus("201")
                .build();

        final String requestUrl = String.format("%s/", ADMIN_URL);
        final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, yamlToUpdate);

        final HttpResponse httpResponse = httpPuttRequest.execute();
        final String statusMessage = httpResponse.getStatusMessage().trim();
        final String responseMessage = httpResponse.parseAsString().trim();
        final String responseLocationHeader = httpResponse.getHeaders().getLocation();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(responseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
        assertThat(statusMessage).isEqualTo("Created");
        assertThat(responseMessage).contains("Configuration created successfully");
    }
}