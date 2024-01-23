package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.http.HttpMethodExtended;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
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
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_XML;

public class StubsPortalRaisedIssueTests {

    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalRaisedIssueTests.class.getResource("/yaml/main-test-stubs.yaml");
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

        final URL xmlActualContentResourceOne = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_2.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContentOne = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(responseContentOne).isEqualTo(expectedResponseContentOne);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#2 which matches rule_1 AND rule_2. But, in this case,
        // we are expecting rule_1 as a response, because the rule_1 is defined earlier than rule_2
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final URL xmlContentResourceTwo = StubsPortalRaisedIssueTests.class.getResource("/xml/request/xml_payload_3.xml");
        assertThat(xmlContentResourceTwo).isNotNull();
        final String contentTwo = StringUtils.inputStreamToString(xmlContentResourceTwo.openStream());

        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, contentTwo);
        requestTwo.setHeaders(httpHeaders);

        final HttpResponse responseTwo = requestTwo.execute();
        final HttpHeaders headersTwo = responseTwo.getHeaders();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headersTwo.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentTwo = responseTwo.parseAsString().trim();

        final URL xmlActualContentResourceTwo = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_1.xml");
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

        final URL xmlActualContentResourceOne = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_2.xml");
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
        final URL xmlContentUrl = StubsPortalRaisedIssueTests.class.getResource("/xml/request/xml_request_issue_29_payload.xml");
        assertThat(xmlContentUrl).isNotNull();
        final String payloadContent = StringUtils.inputStreamToString(xmlContentUrl.openStream());

        final HttpRequest httpRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, payloadContent);
        httpRequest.setHeaders(httpHeaders);

        final HttpResponse httpResponse = httpRequest.execute();
        final HttpHeaders httpResponseHeaders = httpResponse.getHeaders();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(httpResponseHeaders.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String responseContentOne = httpResponse.parseAsString().trim();

        final URL xmlActualContentResourceOne = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_issue_29_body.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContentOne = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(responseContentOne).isEqualTo(expectedResponseContentOne);
    }

    // Originally fixed in: https://github.com/azagniotov/stubby4j/pull/126
    @Test
    public void stubby4jIssue93_WithRequestFilePayloadWithoutResponseBody() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93");

        final URL jsonContentUrl = StubsPortalRaisedIssueTests.class.getResource("/json/request/json_payload_10.json");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethodExtended.PATCH.asString(), requestUrl, content);

        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();
        assertThat(response.parseAsString().trim()).isEmpty();
    }

    // Originally fixed in: https://github.com/azagniotov/stubby4j/pull/126
    @Test
    public void stubby4jIssue93_WithRequestBodyPayloadWithoutResponseBody() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93");

        final String content = "This is a PATCH update";
        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethodExtended.PATCH.asString(), requestUrl, content);

        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();
        assertThat(response.parseAsString().trim()).isEmpty();
    }

    // Originally fixed in: https://github.com/azagniotov/stubby4j/pull/126
    @Test
    public void stubby4jIssue93_WithoutRequestPayloadWithResponseBody() throws Exception {

        // This stub is defined last in the series of stubs for the URI '/azagniotov/stubby4j/issues/93',
        // since its 'request' object has the least number of properties to match, a-la a 'catch-all' stub

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/93");

        final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethodExtended.PATCH.asString(), requestUrl);
        final HttpResponse response = request.execute();
        final HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(headers.getContentType().contains(HEADER_APPLICATION_JSON)).isTrue();

        final String responseContent = response.parseAsString().trim();

        final URL jsonContentUrl = StubsPortalRaisedIssueTests.class.getResource("/json/response/json_response_6.json");
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

    // https://stackoverflow.com/questions/69025268/using-stubby-multiple-responses-for-the-same-patch-request-endpoint
    @Test
    public void stubby4jStackOverFlowQuestion69025268() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/stackoverflow/api/test");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#1 which matches resources/json/response/json_response_2.json
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final String contentOne = "{\"testVar\": \"2\"}";
        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.PATCH, requestUrl, contentOne);

        requestOne.setHeaders(httpHeaders);
        final HttpResponse responseOne = requestOne.execute();
        final String responseOneContentAsString = responseOne.parseAsString().trim();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("OK").isEqualTo(responseOneContentAsString);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#2 which matches resources/json/response/json_response_3.json
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final String contentTwo = "{\"testVar\": \"3\"}";
        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.PATCH, requestUrl, contentTwo);

        requestTwo.setHeaders(httpHeaders);
        final HttpResponse responseTwo = requestTwo.execute();
        final String responseTwoContentAsString = responseTwo.parseAsString().trim();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("Still going strong!").isEqualTo(responseTwoContentAsString);
    }

    @Test
    public void stubby4jIssue399_VanillaRegex() throws Exception {

        // Note: The '?' in <?xml are escaped as these are regex characters
        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/399/vanilla/regex");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        final URL jsonContentUrl = StubsPortalRaisedIssueTests.class.getResource("/xml/request/xml_request_issue_399_payload.xml");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest httpRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        httpRequest.setHeaders(httpHeaders);

        final HttpResponse httpResponse = httpRequest.execute();
        final HttpHeaders httpResponseHeaders = httpResponse.getHeaders();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(httpResponseHeaders.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String httpResponseContent = httpResponse.parseAsString().trim();

        final URL xmlActualContentResourceOne = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_issue_399_body.xml");
        assertThat(xmlActualContentResourceOne).isNotNull();
        final String expectedResponseContent = StringUtils.inputStreamToString(xmlActualContentResourceOne.openStream());

        assertThat(httpResponseContent).isEqualTo(expectedResponseContent);
    }

    @Test
    public void stubby4jIssue399_XmlUnit_Matcher() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/399/xmlunit/matcher");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        final URL jsonContentUrl = StubsPortalRaisedIssueTests.class.getResource("/xml/request/xml_request_issue_399_payload.xml");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest httpRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        httpRequest.setHeaders(httpHeaders);

        final HttpResponse httpResponse = httpRequest.execute();
        final HttpHeaders httpResponseHeaders = httpResponse.getHeaders();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(httpResponseHeaders.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String httpResponseContent = httpResponse.parseAsString().trim();

        final URL xmlActualContentResource = StubsPortalRaisedIssueTests.class.getResource("/xml/response/xml_response_issue_399_body.xml");
        assertThat(xmlActualContentResource).isNotNull();
        final String expectedResponseContent = StringUtils.inputStreamToString(xmlActualContentResource.openStream());

        assertThat(httpResponseContent).isEqualTo(expectedResponseContent);
    }

    @Test
    public void stubby4jIssue399_XmlUnit_Matcher_WithInlinedPost() throws Exception {

        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/stubby4j/issues/399/xmlunit/matcher");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_XML);

        final URL jsonContentUrl = StubsPortalRaisedIssueTests.class.getResource("/xml/request/xml_request_issue_399_payload_2.xml");
        assertThat(jsonContentUrl).isNotNull();
        final String content = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final HttpRequest httpRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        httpRequest.setHeaders(httpHeaders);

        final HttpResponse httpResponse = httpRequest.execute();
        final HttpHeaders httpResponseHeaders = httpResponse.getHeaders();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(httpResponseHeaders.getContentType().contains(HEADER_APPLICATION_XML)).isTrue();

        final String httpResponseContent = httpResponse.parseAsString().trim();
        final String expectedResponseContent = "Captured values are, authority: PIPS with name pid that starts with pid://";

        assertThat(httpResponseContent).isEqualTo(expectedResponseContent);
    }

    // https://stackoverflow.com/questions/70417269/stubby-comma-separated-query-string-implementation-issue
    @Test
    public void stubby4jStackOverFlowQuestion70417269() throws Exception {

        final String requestOneUrl = String.format("%s%s", STUBS_URL, "/stackoverflow/70417269/one-two-five/test?pathid=1,2,5");
        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.GET, requestOneUrl);

        final HttpResponse responseOne = requestOne.execute();
        final String responseOneContentAsString = responseOne.parseAsString().trim();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("matched path id: 1,2,5").isEqualTo(responseOneContentAsString);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        final String requestTwoUrl = String.format("%s%s", STUBS_URL, "/stackoverflow/70417269/one-two/test?pathid=1,2");
        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.GET, requestTwoUrl);

        final HttpResponse responseTwo = requestTwo.execute();
        final String responseTwoContentAsString = responseTwo.parseAsString().trim();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("matched path id: 1,2").isEqualTo(responseTwoContentAsString);
    }
}
