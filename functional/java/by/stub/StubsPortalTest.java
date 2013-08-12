package by.stub;

import by.stub.cli.ANSITerminal;
import by.stub.client.StubbyClient;
import by.stub.client.StubbyResponse;
import by.stub.utils.StringUtils;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class StubsPortalTest {

   private static final String HEADER_APPLICATION_JSON = "application/json";

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

         final String errorMessage = String.format("No data found for GET request at URI %s", assertingRequest);
         assertThat(responseContent).contains(errorMessage);
         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      }
   }

   @Test
   public void should_MakeSuccesfulRequest_WhenQueryParamsAreAnArrayWithEscapedSingleQuoteElements() throws Exception {
      final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find.single.quote?type_name=user&client_id=id&client_secret=secret&attributes=[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]");
      final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse response = request.execute();
      final String responseContent = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"status\": \"hello world with single quote\"}").isEqualTo(responseContent);
   }


   @Test
   public void should_MakeSuccesfulRequest_WhenQueryParamsAreAnArrayWithEscapedQuotedElements() throws Exception {
      final String requestUrl = String.format("%s%s", STUBS_URL, "/entity.find?type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]");
      final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse response = request.execute();
      final String responseContent = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"status\": \"hello world\"}").isEqualTo(responseContent);
   }

   @Test
   public void should_MakeSuccesfulRequest_WhenQueryParamsAreAnArray() throws Exception {
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
   public void should_ReturnPDF_WhenGetRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_URL, "/pdf/hello-world");
      final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(response.getHeaders()).containsKey("content-type");
      assertThat(response.getHeaders().getContentType()).contains("application/pdf;charset=UTF-8");
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
      assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
   }

   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryString() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice?status=active");
      final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
      final String responseContentAsString = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(responseContentAsString).contains("No data found for GET request at URI /invoice?status=active");
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
      assertThat(contentTypeHeader).contains(HEADER_APPLICATION_JSON);
   }

   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryStringOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_SSL_URL, "/invoice?status=active");
      final HttpResponse response = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl).execute();
      final String responseContentAsString = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(responseContentAsString).contains("No data found for GET request at URI /invoice?status=active");

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
      final String responseContentAsString = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(responseContentAsString).contains("No data found for PUT request at URI /invoice/123");
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
      final String responseContentAsString = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(responseContentAsString).contains("No data found for PUT request at URI /invoice/123");
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
      final String responseContentAsString = response.parseAsString().trim();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(responseContentAsString).contains("No data found for POST request at URI /invoice/new");
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
      assertThat(responseContentAsString).contains("No data found for POST request at URI /invoice/new");
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
}
