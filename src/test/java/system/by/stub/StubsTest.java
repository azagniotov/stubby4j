package system.by.stub;

import by.stub.cli.ANSITerminal;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.http.client.StubbyClient;
import by.stub.testing.junit.categories.SystemTest;
import by.stub.utils.StringUtils;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URL;

@Category(SystemTest.class)
public class StubsTest {


   private static StubbyClient stubbyClient;

   private static String stubsUrlAsString;
   private static String stubsSslUrlAsString;
   private static String adminUrlAsString;
   private static String contentAsString;

   private static HttpRequestFactory webClient;
   private static final String HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON = "application/json";
   private static final String HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN = "text/plain";

   @BeforeClass
   public static void beforeClass() throws Exception {

      ANSITerminal.muteConsole(true);

      webClient = new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
         @Override
         public void initialize(final HttpRequest request) {
            request.setThrowExceptionOnExecuteError(false);
         }
      });

      final URL jsonContentUrl = StubsTest.class.getResource("/json/systemtest-body-response-as-file.json");
      Assert.assertNotNull(jsonContentUrl);
      contentAsString = StringUtils.inputStreamToString(jsonContentUrl.openStream());

      int clientPort = 5992;
      int sslPort = 5993;
      int adminPort = 5999;
      final URL url = StubsTest.class.getResource("/yaml/systemtest-test-data.yaml");
      Assert.assertNotNull(url);

      stubbyClient = new StubbyClient();
      stubbyClient.startJetty(clientPort, sslPort, adminPort, url.getFile());

      stubsUrlAsString = String.format("http://localhost:%s", clientPort);
      stubsSslUrlAsString = String.format("https://localhost:%s", sslPort);
      adminUrlAsString = String.format("http://localhost:%s", adminPort);
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubbyClient.stopJetty();
   }

   @Before
   public void beforeEach() {

   }


   @Test
   public void should_ReturnAllProducts_WhenGetRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice?status=active&type=full");
      final HttpResponse response = constructHttpRequest("GET", requestUrl).execute();

      final String contentTypeHeader = response.getContentType();

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals(contentAsString, response.parseAsString().trim());
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryString() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice?status=active");
      final HttpResponse response = constructHttpRequest("GET", requestUrl).execute();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for GET request at URI /invoice?status=active"));
   }


   @Test
   public void should_ReturnAllProducts_WhenGetRequestMadeOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice?status=active&type=full");
      final HttpResponse response = constructHttpRequest("GET", requestUrl).execute();

      final String contentTypeHeader = response.getContentType();

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals(contentAsString, response.parseAsString().trim());
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryStringOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice?status=active");
      final HttpResponse response = constructHttpRequest("GET", requestUrl).execute();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for GET request at URI /invoice?status=active"));

   }


   @Test
   public void should_UpdateProduct_WhenPutRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/123");
      final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("PUT", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"123\", \"status\": \"updated\"}", response.parseAsString().trim());
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_UpdateProduct_WhenPutRequestMadeOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice/123");
      final String content = "{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("PUT", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"123\", \"status\": \"updated\"}", response.parseAsString().trim());
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_UpdateProduct_WhenPutRequestMadeWithWrongPost() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/123");
      final String content = "{\"wrong\": \"post\"}";
      final HttpRequest request = constructHttpRequest("PUT", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for PUT request at URI /invoice/123"));
   }


   @Test
   public void should_UpdateProduct_WhenPutRequestMadeWithWrongPostOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice/123");
      final String content = "{\"wrong\": \"post\"}";
      final HttpRequest request = constructHttpRequest("PUT", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for PUT request at URI /invoice/123"));
   }


   @Test
   public void should_CreateNewProduct_WhenPostRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/new");
      final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("POST", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.CREATED_201, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"456\", \"status\": \"created\"}", responseContentAsString);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_CreateNewProduct_WhenPostRequestMadeOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice/new");
      final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("POST", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.CREATED_201, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"456\", \"status\": \"created\"}", responseContentAsString);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }


   @Test
   public void should_FailtToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSet() throws Exception {

      final String requestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/new");
      final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("POST", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/wrong");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for POST request at URI /invoice/new"));
   }


   @Test
   public void should_FailtToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSetOverSsl() throws Exception {

      final String requestUrl = String.format("%s%s", stubsSslUrlAsString, "/invoice/new");
      final String content = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = constructHttpRequest("POST", requestUrl, content);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/wrong");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String contentTypeHeader = response.getContentType();
      final String responseContentAsString = response.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for POST request at URI /invoice/new"));
   }


   @Test
   public void should_UpdatedstubData_AndMakeGetRequestToUpdatedEndpoint() throws Exception {

      final URL url = StubsTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
      Assert.assertNotNull(url);

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest("POST", adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.CREATED_201, adminResponse.getStatusCode());
      Assert.assertEquals("Configuration created successfully", responseContentAsString);

      final String clientRequestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/updated");
      final HttpRequest clientRequest = constructHttpRequest("GET", clientRequestUrl);
      final HttpResponse clientResponse = clientRequest.execute();

      final String contentTypeHeader = clientResponse.getContentType();
      final String clientResponseContentAsString = clientResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.OK_200, clientResponse.getStatusCode());
      Assert.assertEquals("updated", clientResponseContentAsString);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN));
   }

   @Test
   public void should_UpdatedstubData_AndFailToMakeGetRequestRemovedEndpoint() throws Exception {

      final URL url = StubsTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
      Assert.assertNotNull(url);

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest("POST", adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.CREATED_201, adminResponse.getStatusCode());
      Assert.assertEquals("Configuration created successfully", responseContentAsString);

      final String clientRequestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/123");
      final HttpRequest clientRequest = constructHttpRequest("GET", clientRequestUrl);
      final HttpResponse clientResponse = clientRequest.execute();

      final String contentTypeHeader = clientResponse.getContentType();
      final String clientResponseContentAsString = clientResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientResponse.getStatusCode());
      Assert.assertTrue(clientResponseContentAsString.contains("No data found for GET request at URI /invoice/123"));
   }

   @Test
   public void should_FailToUpdateStubData_WhenMethodIsNotPost() throws Exception {

      final URL url = StubsTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
      Assert.assertNotNull(url);

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest("PUT", adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, adminResponse.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("Method PUT is not allowed on URI"));
   }

   @Test
   public void should_FailToUpdateStubData_WhenPostBadData() throws Exception {

      final URL url = StubsTest.class.getResource("/yaml/systemtest-broken-yaml-test-data.yaml");
      Assert.assertNotNull(url);

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest("POST", adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, adminResponse.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("Could not parse POSTed YAML"));
   }

   private HttpRequest constructHttpRequest(final String method, final String targetUrl) throws IOException {

      return webClient.buildRequest(method,
            new GenericUrl(targetUrl),
            null);
   }

   private HttpRequest constructHttpRequest(final String method, final String targetUrl, final String content) throws IOException {

      return webClient.buildRequest(method,
            new GenericUrl(targetUrl),
            new ByteArrayContent(null, content.getBytes(StringUtils.utf8Charset())));
   }
}
