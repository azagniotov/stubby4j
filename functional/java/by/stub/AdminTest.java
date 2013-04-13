package by.stub;

import by.stub.client.StubbyClient;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.utils.StringUtils;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class AdminTest {

   private static final String HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN = "text/plain";
   private static StubbyClient stubbyClient;
   private static String stubsUrlAsString;
   private static String stubsSslUrlAsString;
   private static String adminUrlAsString;
   private static HttpRequestFactory webClient;

   @BeforeClass
   public static void beforeClass() throws Exception {
      webClient = new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
         @Override
         public void initialize(final HttpRequest request) {
            request.setThrowExceptionOnExecuteError(false);
            request.setReadTimeout(45000);
            request.setConnectTimeout(45000);
         }
      });

      int clientPort = 5992;
      int sslPort = 5993;
      int adminPort = 5999;
      final URL url = AdminTest.class.getResource("/yaml/stubs-data.yaml");
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

   @Test
   public void should_UpdatedStubData_AndMakeGetRequestToUpdatedEndpoint() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
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

      final URL url = AdminTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
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

      final URL url = AdminTest.class.getResource("/yaml/systemtest-to-update-test-data.yaml");
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

      final URL url = AdminTest.class.getResource("/yaml/systemtest-broken-yaml-test-data.yaml");
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
