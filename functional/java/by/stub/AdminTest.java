package by.stub;

import by.stub.client.StubbyClient;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.utils.StringUtils;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;

public class AdminTest {

   private static final String HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN = "text/plain";
   private static StubbyClient stubbyClient;
   private static String stubsUrlAsString;
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
      final URL url = AdminTest.class.getResource("/yaml/stubs.data.yaml");
      assertThat(url).isNotNull();

      stubbyClient = new StubbyClient();
      stubbyClient.startJetty(clientPort, sslPort, adminPort, url.getFile());

      stubsUrlAsString = String.format("http://localhost:%s", clientPort);
      adminUrlAsString = String.format("http://localhost:%s", adminPort);
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubbyClient.stopJetty();
   }

   @Test
   public void should_UpdatedStubData_AndMakeGetRequestToPingPage() throws Exception {

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, "/ping");
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.GET, adminRequestUrl);

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("/pdf/hello-world");
      assertThat(responseContentAsString).contains("STATUS");
   }

   @Test
   public void should_UpdatedStubData_AndMakeGetRequestToUpdatedEndpoint() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.CREATED_201).isEqualTo(adminResponse.getStatusCode());
      assertThat("Configuration created successfully").isEqualTo(responseContentAsString);

      final String clientRequestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/updated");
      final HttpRequest clientRequest = constructHttpRequest(HttpMethods.GET, clientRequestUrl);
      final HttpResponse clientResponse = clientRequest.execute();

      final String contentTypeHeader = clientResponse.getContentType();
      final String clientResponseContentAsString = clientResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(clientResponse.getStatusCode());
      assertThat("updated").isEqualTo(clientResponseContentAsString);
      assertThat(contentTypeHeader).contains(HTTP_HEADER_CONTENT_TYPE_TEXT_PLAIN);
   }

   @Test
   public void should_UpdatedstubData_AndFailToMakeGetRequestRemovedEndpoint() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.CREATED_201).isEqualTo(adminResponse.getStatusCode());
      assertThat("Configuration created successfully").isEqualTo(responseContentAsString);

      final String clientRequestUrl = String.format("%s%s", stubsUrlAsString, "/invoice/123");
      final HttpRequest clientRequest = constructHttpRequest(HttpMethods.GET, clientRequestUrl);
      final HttpResponse clientResponse = clientRequest.execute();

      final String clientResponseContentAsString = clientResponse.parseAsString().trim();

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(clientResponse.getStatusCode());
      assertThat(clientResponseContentAsString).contains("No data found for GET request at URI /invoice/123");
   }

   @Test
   public void should_FailToUpdateStubData_WhenMethodIsNotPost() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.PUT, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.METHOD_NOT_ALLOWED_405).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Method PUT is not allowed on URI");
   }

   @Test
   public void should_FailToUpdateStubData_WhenPostBadData() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.inavlid.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Could not parse POSTed YAML");
   }

   private HttpRequest constructHttpRequest(final String method, final String targetUrl) throws IOException {

      return webClient.buildRequest(method,
         new GenericUrl(targetUrl),
         null);
   }

   private HttpRequest constructHttpRequest(final String method, final String targetUrl, final String content) throws IOException {

      return webClient.buildRequest(method,
         new GenericUrl(targetUrl),
         new ByteArrayContent(null, content.getBytes(StringUtils.charsetUTF8())));
   }
}
