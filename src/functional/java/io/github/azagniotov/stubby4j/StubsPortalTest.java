package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class StubsPortalTest {

    private static final int STUBS_PORT = 5892;
    private static final int STUBS_SSL_PORT = 5893;
    private static final int ADMIN_PORT = 5899;

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);
    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalTest.class.getResource("/yaml/stubs.yaml");
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

    @After
    public void afterEach() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }


    @Test
    public void shouldMatchRequest_WhenStubbedUrlRegexBeginsWith_ButGoodAssertionSent() throws Exception {

        //^/resources/asn/.*$

        final List<String> assertingRequests = new LinkedList<String>() {{
            add("/resources/asn/");
            add("/resources/asn/123");
            add("/resources/asn/eew97we9");
        }};

        for (final String assertingRequest : assertingRequests) {

            String requestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
            HttpResponse response = request.execute();
            String responseContent = response.parseAsString().trim();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat("{\"status\": \"ASN found!\"}").isEqualTo(responseContent);
        }
    }

    @Test
    public void shouldMatchRequest_WhenStubbedUrlRegexified_ButGoodAssertionSent() throws Exception {

        //^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$

        final List<String> assertingRequests = new LinkedList<String>() {{
            add("/abc-efg/12/KM/jhgjkhg234234l2");
            add("/abc-efg/12/KM/23423");
            add("/aaa-aaa/00/AA/qwerty");
        }};

        for (final String assertingRequest : assertingRequests) {

            String requestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
            HttpResponse response = request.execute();
            String responseContent = response.parseAsString().trim();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat("{\"status\": \"The regex works!\"}").isEqualTo(responseContent);
        }
    }

    @Test
    public void shouldMatchRequest_WhenStubbedUrlRegexifiedAndNoStubbedQueryParams() throws Exception {

        // ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}

        final List<String> assertingRequests = new LinkedList<String>() {{
            add("/abc-efg/12/KM/jhgjkhg234234l2?paramOne=valueOne&paramTwo=valueTwo");
            add("/abc-efg/12/KM/23423?paramOne=aaaBLaH&paramTwo=QWERTYUI");
            add("/aaa-aaa/00/AA/qwerty?paramOne=BLAH&paramTwo=Two");
        }};

        for (final String assertingRequest : assertingRequests) {

            String requestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
            HttpResponse response = request.execute();
            String responseContent = response.parseAsString().trim();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat("{\"status\": \"The regex works!\"}").isEqualTo(responseContent);
        }
    }

    @Test
    public void shouldNotMatchRequest_WhenStubbedUrlRegexified_ButBadAssertionSent() throws Exception {

        //^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$

        final List<String> assertingRequests = new LinkedList<String>() {{
            add("/abca-efg/12/KM/jhgjkhg234234l2");
            add("/abcefg/12/KM/23423");
            add("/aaa-aaa/00/Af/qwerty");
            add("/aaa-aaa/00/AA/qwerTy");
            add("/aaa-aaa/009/AA/qwerty");
            add("/AAA-AAA/00/AA/qwerty");
        }};

        for (final String assertingRequest : assertingRequests) {

            String requestUrl = String.format("%s%s", STUBS_URL, assertingRequest);
            HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
            HttpResponse response = request.execute();
            String responseContent = response.parseAsString().trim();

            final String errorMessage = String.format("(404) Nothing found for GET request at URI %s", assertingRequest);
            assertThat(responseContent).contains(errorMessage);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamsAreAnArrayWithEscapedSingleQuoteElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote?type_name=user&client_id=id&client_secret=secret&attributes=[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with single quote\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamValueWithEscapedPlus() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.spaces.within?key=stalin%2B%2B%2Band%2B%2Btruman%2Bare%2B%2B%2Bbest%2B%2B%2Bbuddies");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with spaces within values\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamValueWithEscapedPlusWithEscapedSingleQuoteElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote.spaces.within?key=%5B%27stalin%2B%2B%2Band%2B%2B%2Btruman%27,%27are%2B%2B%2Bbest%2B%2B%2Bfriends%27%5D");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with single quote and spaces within\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamValueWithRawPlusWithEscapedSingleQuoteElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote.spaces.within?key=%5B%27stalin+and+++truman%27,%27are+best++friends%27%5D");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with single quote and spaces within\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamValueWithEmptySpacesWithEscapedSingleQuoteElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote.spaces.within?key=%5B%27stalin and   truman%27,%27are best   friends%27%5D");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with single quote and spaces within\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamValueWithEscapedSpacesWithEscapedSingleQuoteElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote.spaces.within?key=%5B%27stalin%20and%20%20truman%27,%27are%20best%20%20friends%27%5D");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world with single quote and spaces within\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenUrlAndQueryParamsAreRegexified() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/feeds/paymentz?start-index=12&max-records=500");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("Got response").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenUrlMatchingAndFilePathIsRegexified() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/regex-fileserver/file.html");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).contains("Fileserver in stubby4j is working.");
    }

    @Test
    public void should_NotMatchRequest_WhenUrlNotMatchingAndFilePathIsRegexified() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/regex-fileserver/123file.html");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_FailRequest_WhenUrlMatchingButFilePathDoesNotExist() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/regex-fileserver/filexxx.html");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamsAreAnArrayWithEscapedQuotedElements() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find?type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenQueryParamsAreAnArray() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.again?type_name=user&client_id=id&client_secret=secret&attributes=[id,uuid,created,lastUpdated,displayName,email,givenName,familyName]");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"status\": \"hello world\"}").isEqualTo(responseContent);
    }

    @Test
    public void should_ReactToPostRequest_WithoutPost_AndPostNotSupplied() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new/no/post");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void should_ReactToPostRequest_WithoutPost_AndPostSupplied() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new/no/post");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void should_FindPostContentsEqual_WhenJsonContentOrderIrrelevant() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/complex/json/tree");

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/graph.2.json");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("OK");
    }

    @Test
    public void should_FindPostContentsNotEqual_WhenJsonParseExceptionThrown() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/complex/json/tree");
        final String content = "{this is an : invalid JSON string]";

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(httpHeaders);

        assertThat(request.execute().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_FindPostContentsEqual_WhenXmlContentOrderIrrelevant() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/complex/xml/tree");

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/xml/graph.2.xml");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_XML);
        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("OK");
    }

    @Test
    public void should_ReturnPDF_WhenGetRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/pdf/hello-world");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.getHeaders()).containsKey("content-type");
        assertThat(response.getHeaders().getContentType()).contains("application/pdf");
        assertThat(response.getHeaders()).containsKey("content-disposition");
    }

    @Test
    public void should_ReturnAllProducts_WhenGetRequestMade() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice?status=active&type=full");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        final String contentTypeHeader = response.getContentType();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(responseContent);
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryString() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice?status=active");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for GET request at URI /invoice?status=active");
    }

    @Test
    public void should_ReturnAllProducts_WhenGetRequestMadeOverSsl() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryStringOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for GET request at URI /invoice?status=active");

    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/123");
        final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\": \"123\", \"status\": \"updated\"}").isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/123");
        final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\": \"123\", \"status\": \"updated\"}").isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeWithWrongPost() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/123");
        final String content = "{\"wrong\": \"post\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for PUT request at URI /invoice/123");
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeWithWrongPostOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/123");
        final String content = "{\"wrong\": \"post\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for PUT request at URI /invoice/123");
    }

    @Test
    public void should_CreateNewProduct_WhenPostRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"id\": \"456\", \"status\": \"created\"}").isEqualTo(responseContentAsString);
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_CreateNewProduct_WhenPostRequestMadeOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(Common.HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"id\": \"456\", \"status\": \"created\"}").isEqualTo(responseContentAsString);
        assertThat(contentTypeHeader).contains(Common.HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_FailedToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSet() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType("application/wrong");

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for POST request at URI /invoice/new");
    }

    @Test
    public void should_FailedToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSetOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType("application/wrong");

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContentAsString).contains("(404) Nothing found for POST request at URI /invoice/new");
    }

    @Test
    public void should_MakeSuccessfulRequest_AndReturnSingleSequencedResponse() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/single/sequenced/response");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        HttpResponse firstSequenceResponse = request.execute();
        String firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("Still going strong!");

        firstSequenceResponse = request.execute();
        firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("Still going strong!");
    }

    @Test
    public void should_MakeSuccessfulRequest_AndReturnMultipleSequencedResponses() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/sequenced/responses");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        HttpResponse firstSequenceResponse = request.execute();
        String firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("OK");

        final HttpResponse secondSequenceResponse = request.execute();
        final String secondResponseContent = secondSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(secondSequenceResponse.getStatusCode());
        assertThat(secondResponseContent).isEqualTo("Still going strong!");

        final HttpResponse thridSequenceResponse = request.execute();
        final String thirdResponseContent = thridSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(thridSequenceResponse.getStatusCode());
        assertThat(thirdResponseContent).isEqualTo("OMFG!!!");

        firstSequenceResponse = request.execute();
        firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("OK");
    }

    @Test
    public void should_MakeSuccessfulRequest_AndReturnMultipleSequencedResponses_FromFile() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/sequenced/responses/infile");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        HttpResponse firstSequenceResponse = request.execute();
        String firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("OK");

        final HttpResponse secondSequenceResponse = request.execute();
        final String secondResponseContent = secondSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(secondSequenceResponse.getStatusCode());
        assertThat(secondResponseContent).isEqualTo("Still going strong!");

        final HttpResponse thirdSequenceResponse = request.execute();
        final String thirdResponseContent = thirdSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(thirdSequenceResponse.getStatusCode());
        assertThat(thirdResponseContent).isEqualTo("OMFG!!!");

        firstSequenceResponse = request.execute();
        firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEqualTo("OK");
    }

    @Test
    public void should_MakeSuccessfulRequest_AndReturnMultipleSequencedResponses_FromFile_WithBadUrls() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/sequenced/responses/infile/withbadurls");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        HttpResponse firstSequenceResponse = request.execute();
        String firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEmpty();

        final HttpResponse secondSequenceResponse = request.execute();
        final String secondResponseContent = secondSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(secondSequenceResponse.getStatusCode());
        assertThat(secondResponseContent).isEqualTo("Still going strong!");

        final HttpResponse thirdSequenceResponse = request.execute();
        final String thirdResponseContent = thirdSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(thirdSequenceResponse.getStatusCode());
        assertThat(thirdResponseContent).isEmpty();

        firstSequenceResponse = request.execute();
        firstResponseContent = firstSequenceResponse.parseAsString().trim();

        assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequenceResponse.getStatusCode());
        assertThat(firstResponseContent).isEmpty();
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenPostRegexMatchingPostWithLineChars() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/post/regex");
        final String content =
                "Here's the story of a lovely lady," + FileUtils.BR +
                        "Who was bringing up three very lovely girls." + FileUtils.BR +
                        "All of them had hair of gold, like their mother," + FileUtils.BR +
                        "The youngest one in curls." + FileUtils.BR +
                        "Here's the story, of a man named Brady," + FileUtils.BR +
                        "Who was busy with three boys of his own." + FileUtils.BR +
                        "They were four men, living all together," + FileUtils.BR +
                        "Yet they were all alone.";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("OK");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenJsonRegexMatchesPostJson() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/post-body-as-json");
        final String content = "{\"userId\":\"19\",\"requestId\":\"12345\",\"transactionDate\":\"98765\",\"transactionTime\":\"11111\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"requestId\": \"12345\", \"transactionDate\": \"98765\", \"transactionTime\": \"11111\"}");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenJsonRegexMatchesPostComplexJson() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/post-body-as-json-2");
        final String content = "{\"objects\": [{\"key\": \"value\"}, {\"key\": \"value\"}, {\"key\": {\"key\": \"12345\"}}]}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"internalKey\": \"12345\"}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_ReturnExpectedResourceIdHeader_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/this/stub/should/always/be/second/in/this/file");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(Common.HEADER_APPLICATION_JSON)).isTrue();
        assertThat(headers.containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isTrue();
        final List<String> headerValues = (List<String>) headers.get(StubResponse.STUBBY_RESOURCE_ID_HEADER);
        assertThat(headerValues.get(0)).isEqualTo("1");
    }

    @Test
    public void should_ReturnReplacedTokenizedResponse_WhenCapturingGroupsEqualToNumberOfTokens() throws Exception {
        String requestUrl = String.format("%s%s", STUBS_URL, "/resources/invoices/12345/category/milk");
        HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse response = request.execute();
        String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned invoice number# 12345 in category 'milk'");


        requestUrl = String.format("%s%s", STUBS_URL, "/resources/invoices/88888/category/army");
        request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        response = request.execute();
        responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned invoice number# 88888 in category 'army'");
    }

    @Test
    public void should_ReturnReplacedTokenizedResponse_WhenUsingExternalFile() throws Exception {
        String requestUrl = String.format("%s%s", STUBS_URL, "/account/12345/category/milk?date=Saturday");
        HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        HttpResponse response = request.execute();
        String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned invoice number# 12345 in category 'milk' on the date 'Saturday'");


        requestUrl = String.format("%s%s", STUBS_URL, "/account/88888/category/army?date=NOW");
        request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        response = request.execute();
        responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned invoice number# 88888 in category 'army' on the date 'NOW'");
    }

    @Test
    public void should_ReturnReplacedTokenizedResponse_WhenCapturingGroupsNotEqualsToNumberOfTokens() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/resources/invoices/22222");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned invoice number# 22222 in category '<%url.2%>'");
    }

    @Test
    public void should_ReturnReplacedTokenizedResponse_WhenFullMatchCapturingGroupsAreUsed() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/no/explicit/groups/22222?param=ABC");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("custom-header", "XYZ");
        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned content with URL /no/explicit/groups/22222, query param ABC and custom-header XYZ");
    }

    @Test
    public void should_ReturnReplacedTokenizedResponse_WhenCapturingGroupHasSubgroups() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/groups/with/sub/groups/abc-123");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        final HttpResponse response = request.execute();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Returned content with URL /groups/with/sub/groups/abc-123, parent group abc-123 and two sub-groups abc & 123");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenGetRequestMadeWithNoEqualSignInQueryStringParam() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/empty.param?type_name&client_secret=secret");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("EMPTY WORKS");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenGetRequestMadeWithNoEqualSignInSingleQueryStringParam() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/empty.single.param?type_name");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("EMPTY SINGLE WORKS");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenGetRequestMadeWithNoQueryStringParamValue() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/empty.param?type_name=&client_secret=secret");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains("EMPTY WORKS");
    }

    @Test
    public void should_ReturnExpectedRecordedResponse_FromAnotherValidUrl() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/feed/1?language=chinese&greeting=nihao");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains("application/xml")).isTrue();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).contains("<payment><invoiceTypeLookupCode>STANDARD</invoiceTypeLookupCode></payment>");
    }

    @Test
    public void should_NotReturnExpectedRecordedResponse_FromValidUrl_WhenQueryValueNotCorrect() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/feed/2?language=russian&greeting=nihao");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains("application/xml")).isTrue();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).contains("(404) Nothing found for GET request at URI /recordable/feed/2?greeting=nihao&language=russian");
    }

    @Test
    public void should_ReturnExpectedRecordedResponse_OnSubsequentCallToValidUrl() throws Exception {

        ANSITerminal.muteConsole(false);

        final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        final PrintStream oldPrintStream = System.out;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        final String requestUrl = String.format("%s%s", STUBS_URL, "/feed/1?language=chinese&greeting=nihao");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(Common.HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final int LIMIT = 5;
        for (int idx = 1; idx <= LIMIT; idx++) {
            final HttpResponse actualResponse = request.execute();
            final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

            String firstCallResponseContent = actualResponse.parseAsString().trim();
            assertThat(firstCallResponseContent).contains("<payment><invoiceTypeLookupCode>STANDARD</invoiceTypeLookupCode></payment>");
            // Make sure we only hitting recordabe source once
            assertThat(actualConsoleOutput).containsOnlyOnce("Recording HTTP response using");

            if (idx == LIMIT) {
                System.setOut(oldPrintStream);
                System.out.println(actualConsoleOutput);
            }
        }
    }

    /**
     * This test really has value when there is an active connection to the Internet
     */
    @Test
    @Ignore
    @CoberturaIgnore
    public void should_ReturnExpectedRecordedResponse_FromGoogle() throws Exception {

        ANSITerminal.muteConsole(false);

        final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        final PrintStream oldPrintStream = System.out;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        final String requestUrl = String.format("%s%s", STUBS_URL, "/maps/api/geocode/json?sensor=false&address=1600+Amphitheatre+Parkway,+Mountain+View,+CA");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final int LIMIT = 1;
        for (int idx = 1; idx <= LIMIT; idx++) {
            final HttpResponse response = request.execute();
            final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();
            if (idx == 1) {
                if (actualConsoleOutput.contains("Exception")) {
                    System.setOut(oldPrintStream);
                    System.out.println(actualConsoleOutput);
                    // If we are here, it means we do not have active Internet connection (or something else has happened)
                    // and we could not hit Google in that case, there is no point to causing this test to fail if the user
                    // running this test without having the ability to access the Internet.
                    break;
                }
            }

            final HttpHeaders headers = response.getHeaders();
            assertThat(headers.getContentType().contains(Common.HEADER_APPLICATION_JSON)).isTrue();

            String responseContent = response.parseAsString().trim();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
            assertThat(responseContent).contains("results");
            assertThat(responseContent).contains("address_components");
            assertThat(responseContent).contains("formatted_address");

            // Make sure we only hitting recordabe source once
            assertThat(actualConsoleOutput).containsOnlyOnce("Recording HTTP response using");

            if (idx == LIMIT) {
                System.setOut(oldPrintStream);
                System.out.println(actualConsoleOutput);
            }
        }
    }
}