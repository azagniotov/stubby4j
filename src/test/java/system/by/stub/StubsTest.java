package system.by.stub;

import by.stub.cli.ANSITerminal;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.http.client.StubbyClient;
import by.stub.testing.categories.SystemTests;
import by.stub.utils.StringUtils;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URL;

@Category(SystemTests.class)
public class StubsTest {


   private static StubbyClient stubbyClient;

   private static String stubsUrlAsString;
   private static String stubsSslUrlAsString;
   private static String adminUrlAsString;
   private static String contentAsString;

   private final static WebClient webClient = new WebClient();
   private static final String HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON = "application/json";
   private static final String HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN = "text/plain";
   public static final String HTTP_HEADER_SERVER_NAME = "stubby4j/x.x.xx (HTTP stub server)";

   @BeforeClass
   public static void beforeClass() throws Exception {

      ANSITerminal.muteConsole(true);

      webClient.setUseInsecureSSL(true);

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

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice?status=active&type=full"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.GET);
      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals(contentAsString, response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryString() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice?status=active"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.GET);
      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String responseContentAsString = response.getContentAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for GET request at URI /invoice?status=active"));
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
   }

   @Test
   public void should_ReturnAllProducts_WhenGetRequestMadeOverSsl() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsSslUrlAsString, "/invoice?status=active&type=full"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.GET);

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals(contentAsString, response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_FailToReturnAllProducts_WhenGetRequestMadeWithoutRequiredQueryStringOverSsl() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsSslUrlAsString, "/invoice?status=active"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.GET);

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
   }

   @Test
   public void should_UpdateProduct_WhenPutRequestMade() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice/123"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.PUT);
      request.setRequestBody("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/json");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"123\", \"status\": \"updated\"}", response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_UpdateProduct_WhenPutRequestMadeOverSsl() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsSslUrlAsString, "/invoice/123"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.PUT);
      request.setRequestBody("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/json");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.OK_200, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"123\", \"status\": \"updated\"}", response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_UpdateProduct_WhenPutRequestMadeWithWrongPost() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice/123"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.PUT);
      request.setRequestBody("{\"wrong\": \"post\"}");
      request.setAdditionalHeader("content-type", "application/json");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");
      final String responseContentAsString = response.getContentAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for PUT request at URI /invoice/123"));
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
   }

   @Test
   public void should_CreateNewProduct_WhenPostRequestMade() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice/new"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.POST);
      request.setRequestBody("{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/json");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.CREATED_201, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"456\", \"status\": \"created\"}", response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_CreateNewProduct_WhenPostRequestMadeOverSsl() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsSslUrlAsString, "/invoice/new"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.POST);
      request.setRequestBody("{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/json");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.CREATED_201, response.getStatusCode());
      Assert.assertEquals("{\"id\": \"456\", \"status\": \"created\"}", response.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_APPLICATION_JSON));
   }

   @Test
   public void should_FailtToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSet() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice/new"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.POST);
      request.setRequestBody("{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/wrong");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");
      final String responseContentAsString = response.getContentAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for POST request at URI /invoice/new"));
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
   }

   @Test
   public void should_FailtToCreateNewProduct_WhenPostRequestMadeWhenWrongHeaderSetOverSsl() throws Exception {

      final URL requestUrl = new URL(String.format("%s%s", stubsSslUrlAsString, "/invoice/new"));
      final WebRequest request = new WebRequest(requestUrl, HttpMethod.POST);
      request.setRequestBody("{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}");
      request.setAdditionalHeader("content-type", "application/wrong");

      final WebResponse response = webClient.loadWebResponse(request);
      final String serverNameHeader = response.getResponseHeaderValue("Server");
      final String contentTypeHeader = response.getResponseHeaderValue("Content-Type");
      final String responseContentAsString = response.getContentAsString().trim();

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, response.getStatusCode());
      Assert.assertTrue(responseContentAsString.contains("No data found for POST request at URI /invoice/new"));
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
   }

   @Test
   public void should_UpdatedstubData_AndMakeGetRequestToUpdatedEndpoint() throws Exception {

      final URL url = StubsTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
      Assert.assertNotNull(url);

      final URL adminRequestUrl = new URL(String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW));
      final WebRequest adminRequest = new WebRequest(adminRequestUrl, HttpMethod.POST);
      adminRequest.setRequestBody(StringUtils.inputStreamToString(url.openStream()));

      final WebResponse adminResponse = webClient.loadWebResponse(adminRequest);
      final String responseContentAsString = adminResponse.getContentAsString().trim();

      Assert.assertEquals(HttpStatus.CREATED_201, adminResponse.getStatusCode());
      Assert.assertEquals("Configuration created successfully", responseContentAsString);

      final URL clientRequestUrl = new URL(String.format("%s%s", stubsUrlAsString, "/invoice/updated"));
      final WebRequest clientRequest = new WebRequest(clientRequestUrl, HttpMethod.GET);
      final WebResponse clientResponse = webClient.loadWebResponse(clientRequest);
      final String serverNameHeader = clientResponse.getResponseHeaderValue("Server");
      final String contentTypeHeader = clientResponse.getResponseHeaderValue("Content-Type");

      Assert.assertEquals(HttpStatus.OK_200, clientResponse.getStatusCode());
      Assert.assertEquals("updated", clientResponse.getContentAsString().trim());
      Assert.assertEquals(HTTP_HEADER_SERVER_NAME, serverNameHeader);
      Assert.assertTrue(contentTypeHeader.contains(HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN));

   }
}
