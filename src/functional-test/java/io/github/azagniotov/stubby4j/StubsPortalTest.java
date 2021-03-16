package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.HttpMethodExtended;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedArrayList;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_XML;

public class StubsPortalTest {

    // This port needs to be hardcoded due to tests for the:
    // 1. recordable response behavior (the `response` `body` key value starts with http://..... )
    // 2. redirect response behavior (the `location` header is set on the `response` )
    private static final int STUBS_PORT = 5892;
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

        final URL url = StubsPortalTest.class.getResource("/yaml/main-test-stubs.yaml");
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

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/request/json_payload_4.json");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);
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
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(httpHeaders);

        assertThat(request.execute().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_FindPostContentsEqual_WhenXmlContentOrderIrrelevant() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/complex/xml/tree");

        final URL xmlContentResource = StubsPortalTest.class.getResource("/xml/request/xml_payload_2.xml");
        assertThat(xmlContentResource).isNotNull();
        final String content = StringUtils.inputStreamToString(xmlContentResource.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);
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

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice?status=active&type=full");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        final String contentTypeHeader = response.getContentType();
        final String responseContent = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(responseContent);
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryString() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice?status=active");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_ReturnAllProducts_WhenGetRequestMadeOverSsl() throws Exception {

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_1.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active&type=full");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(expectedContent).isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryStringOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active");
        final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/123");
        final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\": \"123\", \"status\": \"updated\"}").isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/123");
        final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\": \"123\", \"status\": \"updated\"}").isEqualTo(response.parseAsString().trim());
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeWithWrongPost() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/123");
        final String content = "{\"wrong\": \"post\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_UpdateProduct_WhenPutRequestMadeWithWrongPostOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/123");
        final String content = "{\"wrong\": \"post\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void should_CreateNewProduct_WhenPostRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"id\": \"456\", \"status\": \"created\"}").isEqualTo(responseContentAsString);
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
    }

    @Test
    public void should_CreateNewProduct_WhenPostRequestMadeOverSsl() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice/new");
        final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final String contentTypeHeader = response.getContentType();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"id\": \"456\", \"status\": \"created\"}").isEqualTo(responseContentAsString);
        assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
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
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"requestId\": \"12345\", \"transactionDate\": \"98765\", \"transactionTime\": \"11111\"}");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenJsonRegexMatchesComplexJsonPost() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/post-body-as-json-2");
        final String content = "{\"objects\": [{\"key\": \"value\"}, {\"key\": \"value\"}, {\"key\": {\"key\": \"12345\"}}]}";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"internalKey\": \"12345\"}");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenStubbedValidJsonMatchesComplexValidJsonPost() throws Exception {

        final URL dataPostUrl = StubsPortalTest.class.getResource("/json/request/json_payload_9.json");
        final String content = StringUtils.inputStreamToString(dataPostUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_URL, "/jsonapi-json-object-comparison");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"status\": \"OK\"}");
    }

    @Test
    public void should_MakeSuccessfulRequest_WhenStubbedJsonRegexMatchesComplexValidJsonPost() throws Exception {

        final URL dataPostUrl = StubsPortalTest.class.getResource("/json/request/json_payload_1.json");
        final String content = StringUtils.inputStreamToString(dataPostUrl.openStream());

        final String requestUrl = String.format("%s%s", STUBS_URL, "/jsonapi-json-regex");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(response.parseAsString().trim()).isEqualTo("{\"people#id\": \"9\"}");
    }

    @Test
    public void should_ReturnExpectedResourceIdHeader_WhenSuccessfulRequestMade() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/this/stub/should/always/be/second/in/this/file");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();
        assertThat(headers.containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID)).isTrue();

        final List<String> headerValues = asCheckedArrayList(headers.get(Common.HEADER_X_STUBBY_RESOURCE_ID), String.class);

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
    public void shouldReturnReplacedValueInLocationHeaderWhenQueryParamHasDynamicToken() throws Exception {

        expectedException.expect(UnknownHostException.class);
        expectedException.expectMessage("hostDoesNotExist123.com");

        final String requestUrl = String.format("%s%s", STUBS_URL, "/v8/identity/authorize?redirect_uri=https://hostDoesNotExist123.com/app/very/cool");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        request.execute();
    }

    @Test
    public void should_MakeSuccessfulRedirectRequest_WhenLocationHeaderAndStatusSet() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/item/redirect/source");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(Common.HEADER_APPLICATION_JSON)).isTrue();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("{\"response\" : \"content\"}");
    }

    @Test
    public void should_NotMakeRedirectRequest_WhenLocationHeaderButStatusNot30x() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/item/redirect/source/with/wrong/status");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        // No redirect, only empty response because the stubbed status code is not 30x
        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEmpty();
    }

    @Test
    public void should_MakeSuccessfulRequest_When404NotFoundResponseStubbedWithBody() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/returns-not-found-response-with-body");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
        final HttpResponse response = request.execute();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
        assertThat(responseContent).isEqualTo("This response with body was actually not found");
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
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).contains("<payment><invoiceTypeLookupCode>STANDARD</invoiceTypeLookupCode></payment>");
    }

    @Test
    public void should_ReturnExpectedRecordedResponseUsingActualQuery_FromAnotherValidUrl() throws Exception {
        final String requestUrl = String.format("%s%s", STUBS_URL, "/feed/3?language=chinese&greeting=12345");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(Common.HEADER_APPLICATION_JSON)).isTrue();

        String responseContent = response.parseAsString().trim();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).contains("OK");
        assertThat(responseContent).contains("actual query params when recording");
    }

    @Test
    public void should_NotReturnExpectedRecordedResponse_FromValidUrl_WhenQueryValueIncorrect() throws Exception {

        // The '/recordable/feed/2' stub expects query param 'language' to have a value 'chinese'
        final String requestUrl = String.format("%s%s", STUBS_URL, "/feed/2?language=russian&greeting=nihao");
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final HttpResponse response = request.execute();
        final String responseContent = response.parseAsString().trim();

        final HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContent).isEqualTo("Not Found");
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
        requestHeaders.setContentType(HEADER_APPLICATION_JSON);
        request.setHeaders(requestHeaders);

        final int LIMIT = 5;
        for (int idx = 1; idx <= LIMIT; idx++) {
            final HttpResponse actualResponse = request.execute();
            final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

            String firstCallResponseContent = actualResponse.parseAsString().trim();
            assertThat(firstCallResponseContent).contains("<payment><invoiceTypeLookupCode>STANDARD</invoiceTypeLookupCode></payment>");
            // Make sure we only hitting recordable source once
            assertThat(actualConsoleOutput).contains("HTTP request from stub metadata to");

            if (idx == LIMIT) {
                System.setOut(oldPrintStream);
                System.out.println(actualConsoleOutput);
            }
        }
    }

    @Test
    public void stubby4jIssue29() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/29");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#1 which matches rule_2 ONLY, this will cache stub for rule_2 by the above requestUrl
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final String contentOne = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><idex:type xmlns:idex=\"http://idex.bbc.co.uk/v1\"><idex:authority>ALEX-1</idex:authority><idex:name>ALEX-2</idex:name><idex:startsWith>ALEX-3</idex:startsWith></idex:type>";

        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentOne);
        requestOne.setHeaders(httpHeaders);

        final HttpResponse responseOne = requestOne.execute();
        final HttpHeaders headersOne = responseOne.getHeaders();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headersOne.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentOne = responseOne.parseAsString().trim();

        final URL xmlActualContentResourceOne = StubsPortalTest.class.getResource("/xml/response/xml_response_2.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContentOne = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(responseContentOne).isEqualTo(expectedResponseContentOne);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#2 which matches rule_1 AND rule_2. But, in this case,
        // we are expecting rule_1 as a response, because the rule_1 is defined earlier than rule_2
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final URL xmlContentResourceTwo = StubsPortalTest.class.getResource("/xml/request/xml_payload_3.xml");
        assertThat(xmlContentResourceTwo).isNotNull();
        final String contentTwo = StringUtils.inputStreamToString(xmlContentResourceTwo.openStream());

        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentTwo);
        requestTwo.setHeaders(httpHeaders);

        final HttpResponse responseTwo = requestTwo.execute();
        final HttpHeaders headersTwo = responseTwo.getHeaders();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headersTwo.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentTwo = responseTwo.parseAsString().trim();

        final URL xmlActualContentResourceTwo = StubsPortalTest.class.getResource("/xml/response/xml_response_1.xml");
        assertThat(xmlActualContentResourceTwo).isNotNull();
        final String expectedResponseContentTwo = StringUtils.inputStreamToString(xmlActualContentResourceTwo.openStream());

        assertThat(responseContentTwo).isEqualTo(expectedResponseContentTwo);
    }

    @Test
    public void stubby4jIssue29_VanillaRegex() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/29/vanilla/regex");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        // The '?' in <?xml are escaped in YAML as these are regex characters
        final String contentOne = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><idex:type xmlns:idex=\"http://idex.bbc.co.uk/v1\"><idex:authority>ALEX-1</idex:authority><idex:name>ALEX-2</idex:name><idex:startsWith>ALEX-3</idex:startsWith></idex:type>";

        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentOne);
        requestOne.setHeaders(httpHeaders);

        final HttpResponse responseOne = requestOne.execute();
        final HttpHeaders headersOne = responseOne.getHeaders();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headersOne.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentOne = responseOne.parseAsString().trim();

        final URL xmlActualContentResourceOne = StubsPortalTest.class.getResource("/xml/response/xml_response_2.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContentOne = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(responseContentOne).isEqualTo(expectedResponseContentOne);
    }

    @Test
    public void stubby4jIssue29_VanillaComplexRegex() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/29/vanilla/regex");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        // Note:
        // 1. the '?' in <?xml are escaped as these are regex characters
        // 2. ths '[' in <![CDATA[(.*)]]> are escaped as these are regex characters
        final URL jsonContentUrl = StubsPortalTest.class.getResource("/xml/request/xml_payload_5.xml");
        assertThat(jsonContentUrl).isNotNull();
        final String contentOne = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentOne);
        requestOne.setHeaders(httpHeaders);

        final HttpResponse responseOne = requestOne.execute();
        final HttpHeaders headersOne = responseOne.getHeaders();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headersOne.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentOne = responseOne.parseAsString().trim();

        final URL xmlActualContentResourceOne = StubsPortalTest.class.getResource("/xml/response/xml_response_4.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContentOne = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(responseContentOne).isEqualTo(expectedResponseContentOne);
    }

    @Test
    public void stubby4jIssue93_WithBody() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93");

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/request/json_payload_10.json");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethodExtended.PATCH.asString(), requestUrl, content);

        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();
    }

    @Test
    public void stubby4jIssue93_WithoutBody() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93");

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethodExtended.PATCH.asString(), requestUrl);
        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();

        final String responseContent = response.parseAsString().trim();

        final URL jsonContentUrl = StubsPortalTest.class.getResource("/json/response/json_response_6.json");
        assertThat(jsonContentUrl).isNotNull();
        final String expectedResponseContent = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        assertThat(responseContent).isEqualTo(expectedResponseContent);
    }

    @Test
    public void stubby4jIssue93_ExtraTest() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93/SAME/address");

        final String contentOne = "{\"type\": \"MOBILE\"}";
        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, contentOne);

        final HttpResponse responseOne = requestOne.execute();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        assertThat("{\"type\": \"BAD_REQUEST\"}").isEqualTo(responseOne.parseAsString().trim());


        final String contentTwo = "{\"type\": \"HOME\"}";
        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, contentTwo);

        final HttpResponse responseTwo = requestTwo.execute();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("OK").isEqualTo(responseTwo.parseAsString().trim());
    }

    @Test
    public void stubby4jIssue171() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/171");

        final String content = "[{\"application\":{\"Address\":[{\"addressId\":8,\"customerId\":1,\"orderItemId\":3,\"addressType\":\"STMT\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"},{\"addressId\":7,\"customerId\":1,\"addressType\":\"CMAL\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"},{\"addressId\":6,\"customerId\":1,\"addressType\":\"CRES\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"}],\"AppCustRelationship\":[{\"customerId\":1,\"applicationId\":2,\"relationshipType\":\"POW\"}],\"Application\":[{\"applicationId\":2,\"applicationSigned\":false,\"applicationSource\":\"sola\",\"applicationSourceCode\":\"QMV\",\"applicationSourceCountry\":\"AU\",\"applicationVersion\":1,\"basketId\":1,\"bsb\":\"\",\"channel\":\"I\",\"createdDate\":\"2021-01-21T09:57:03+11:00\",\"currencyCode\":\"AUD\",\"modifiedBy\":\"Apply User\",\"orderId\":1000000003}],\"CRNRequest\":[{\"regId\":5,\"customerId\":1,\"orderItemId\":3,\"accessLevel\":\"Full\",\"createNewCRN\":true,\"customerClass\":\"CNE\"}],\"Contact\":[{\"contactId\":4,\"customerId\":1,\"contactType\":\"E\",\"email\":\"test@anz.com\"}],\"CustOrderItemRelationship\":[{\"customerId\":1,\"orderItemId\":3,\"custAcctRelationship\":\"SOL\",\"orderItemFlag\":\"N\"}],\"Customer\":[{\"customerId\":1,\"acceptMarketing\":true,\"AUTaxResidentOnly\":false,\"customerType\":\"IND\",\"depositCustomerType\":\"STD\",\"existingCustomer\":false,\"fullName\":\"Failure Test\",\"ipAddress\":\"\",\"privacyConsent\":true,\"verificationStatus\":\"Verified\"}],\"Deposit\":[{\"orderItemId\":3,\"ATOType\":\"I\",\"numberOfSignatories\":1,\"transactionAccountType\":\"IND\"}],\"Individual\":[{\"customerId\":1,\"dateOfBirth\":\"1981-01-01T00:00:00Z\",\"firstName\":\"Failure\",\"gender\":\"M\",\"lastName\":\"Test\",\"middleName\":\"\",\"title\":\"mr\"}],\"OrderItem\":[{\"orderItemId\":3,\"capProductCode\":\"DDA\",\"capSubProductCode\":\"ED\",\"eStatementEmail\":\"test@anz.com\",\"natureOfProduct\":\"primary\"}]}}]";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();
        final String responseContentAsString = response.parseAsString().trim();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("MATCHED!").isEqualTo(responseContentAsString);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();
    }

    @Test
    public void stubby4jIssue171_WithWrongPayload() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/171");

        // Stubbed RegEx expects JSON key "natureOfProduct" to be present, but it is missing from the POSTed payload
        final String content = "[{\"application\":{\"Address\":[{\"addressId\":8,\"customerId\":1,\"orderItemId\":3,\"addressType\":\"STMT\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"},{\"addressId\":7,\"customerId\":1,\"addressType\":\"CMAL\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"},{\"addressId\":6,\"customerId\":1,\"addressType\":\"CRES\",\"city\":\"DOCKLANDS\",\"country\":\"AU\",\"postcode\":\"3008\",\"state\":\"VIC\",\"streetName\":\"Collins\",\"streetNumber\":\"691\",\"streetType\":\"ST\"}],\"AppCustRelationship\":[{\"customerId\":1,\"applicationId\":2,\"relationshipType\":\"POW\"}],\"Application\":[{\"applicationId\":2,\"applicationSigned\":false,\"applicationSource\":\"sola\",\"applicationSourceCode\":\"QMV\",\"applicationSourceCountry\":\"AU\",\"applicationVersion\":1,\"basketId\":1,\"bsb\":\"\",\"channel\":\"I\",\"createdDate\":\"2021-01-21T09:57:03+11:00\",\"currencyCode\":\"AUD\",\"modifiedBy\":\"Apply User\",\"orderId\":1000000003}],\"CRNRequest\":[{\"regId\":5,\"customerId\":1,\"orderItemId\":3,\"accessLevel\":\"Full\",\"createNewCRN\":true,\"customerClass\":\"CNE\"}],\"Contact\":[{\"contactId\":4,\"customerId\":1,\"contactType\":\"E\",\"email\":\"test@anz.com\"}],\"CustOrderItemRelationship\":[{\"customerId\":1,\"orderItemId\":3,\"custAcctRelationship\":\"SOL\",\"orderItemFlag\":\"N\"}],\"Customer\":[{\"customerId\":1,\"acceptMarketing\":true,\"AUTaxResidentOnly\":false,\"customerType\":\"IND\",\"depositCustomerType\":\"STD\",\"existingCustomer\":false,\"fullName\":\"Failure Test\",\"ipAddress\":\"\",\"privacyConsent\":true,\"verificationStatus\":\"Verified\"}],\"Deposit\":[{\"orderItemId\":3,\"ATOType\":\"I\",\"numberOfSignatories\":1,\"transactionAccountType\":\"IND\"}],\"Individual\":[{\"customerId\":1,\"dateOfBirth\":\"1981-01-01T00:00:00Z\",\"firstName\":\"Failure\",\"gender\":\"M\",\"lastName\":\"Test\",\"middleName\":\"\",\"title\":\"mr\"}],\"OrderItem\":[{\"orderItemId\":3,\"capProductCode\":\"DDA\",\"capSubProductCode\":\"ED\",\"eStatementEmail\":\"test@anz.com\"}]}}]";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        request.setHeaders(httpHeaders);

        final HttpResponse response = request.execute();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
    }

    @Test
    public void stubby4jIssue170() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/170");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#1 which matches rule_3 ONLY, this will cache stub for rule_3 by the above requestUrl
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final String contentOne = "{\"rule\":\"rule_3\",\"request_id\":\"rule_3_request_id\"}";
        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentOne);

        requestOne.setHeaders(httpHeaders);
        final HttpResponse responseOne = requestOne.execute();
        final String responseOneContentAsString = responseOne.parseAsString().trim();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("rule_3").isEqualTo(responseOneContentAsString);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#2 which matches rule_1 AND rule_3. But, in this case,
        // we are expecting rule_1 as a response, because the rule_1 is defined earlier than rule_3
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final String contentTwo = "{\"rule\":\"rule_1\",\"request_id\":\"rule_1_request_id\"}";
        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentTwo);

        requestTwo.setHeaders(httpHeaders);
        final HttpResponse responseTwo = requestTwo.execute();
        final String responseTwoContentAsString = responseTwo.parseAsString().trim();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("rule_1").isEqualTo(responseTwoContentAsString);
    }
}
