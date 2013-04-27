package by.stub;

import by.stub.client.StubbyClient;
import by.stub.handlers.AdminHandler;
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
   public void should_UpdatedStubData_AndMakeGetRequestToStatusPage() throws Exception {

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, "/status");
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

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
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

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
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
   public void should_ReturnNotImplemented_WhenMethodIsPut() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.PUT, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.NOT_IMPLEMENTED_501).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Support for method PUT is not implemented on URI");
   }

   @Test
   public void should_ReturnNotAllowed_WhenMethodIsUnsupported() throws Exception {

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.TRACE, adminRequestUrl, "");

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.METHOD_NOT_ALLOWED_405).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Method TRACE is not allowed on URI /");
   }

   @Test
   public void should_FailToDeleteStubData_WhenNoIdToDeleteProvided() throws Exception {

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.DELETE, adminRequestUrl);

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      System.out.println(responseContentAsString);

      assertThat(HttpStatus.METHOD_NOT_ALLOWED_405).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Method DELETE is not allowed on URI");
   }

   @Test
   public void should_DeleteStubData_WhenIdToDeleteProvided() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/stubs.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.CREATED_201).isEqualTo(adminResponse.getStatusCode());
      assertThat("Configuration created successfully").isEqualTo(responseContentAsString);

      final int indexToDelete = 2;
      final String requestUriToDelete = String.format("%s%s", AdminHandler.ADMIN_ROOT, indexToDelete);
      final String adminRequestDeleteUrl = String.format("%s%s", adminUrlAsString, requestUriToDelete);
      final HttpRequest adminDeleteRequest = constructHttpRequest(HttpMethods.DELETE, adminRequestDeleteUrl);

      final HttpResponse adminDeleteResponse = adminDeleteRequest.execute();
      final String deleteResponseContentAsString = adminDeleteResponse.parseAsString().trim();

      final String successDeletion = String.format("Stub request index#%s deleted successfully", indexToDelete);

      assertThat(HttpStatus.OK_200).isEqualTo(adminDeleteResponse.getStatusCode());
      assertThat(deleteResponseContentAsString).contains(successDeletion);
   }

   @Test
   public void should_NotDeleteAfterSecondAttemptStubData_WhenOnlyOneRequestStubbed() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.CREATED_201).isEqualTo(adminResponse.getStatusCode());
      assertThat("Configuration created successfully").isEqualTo(responseContentAsString);

      int indexToDelete = 0;
      String requestUriToDelete = String.format("%s%s", AdminHandler.ADMIN_ROOT, indexToDelete);
      String adminRequestDeleteUrl = String.format("%s%s", adminUrlAsString, requestUriToDelete);
      HttpRequest adminDeleteRequest = constructHttpRequest(HttpMethods.DELETE, adminRequestDeleteUrl);
      adminDeleteRequest.execute();

      indexToDelete = 1;
      requestUriToDelete = String.format("%s%s", AdminHandler.ADMIN_ROOT, indexToDelete);
      adminRequestDeleteUrl = String.format("%s%s", adminUrlAsString, requestUriToDelete);
      adminDeleteRequest = constructHttpRequest(HttpMethods.DELETE, adminRequestDeleteUrl);

      final HttpResponse adminDeleteResponse = adminDeleteRequest.execute();
      final String deleteResponseContentAsString = adminDeleteResponse.getStatusMessage().trim();

      final String statusMessage = String.format("Stub request index#%s does not exist, cannot delete", indexToDelete);

      assertThat(HttpStatus.NO_CONTENT_204).isEqualTo(adminDeleteResponse.getStatusCode());
      assertThat(deleteResponseContentAsString).contains(statusMessage);
   }


   @Test
   public void should_NotDeleteStubData_WhenIdToDeleteIsGreaterThanListSize() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/stubs.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.CREATED_201).isEqualTo(adminResponse.getStatusCode());
      assertThat("Configuration created successfully").isEqualTo(responseContentAsString);

      final int indexToDelete = 20;
      final String requestUriToDelete = String.format("%s%s", AdminHandler.ADMIN_ROOT, indexToDelete);
      final String adminRequestDeleteUrl = String.format("%s%s", adminUrlAsString, requestUriToDelete);
      final HttpRequest adminDeleteRequest = constructHttpRequest(HttpMethods.DELETE, adminRequestDeleteUrl);

      final HttpResponse adminDeleteResponse = adminDeleteRequest.execute();
      final String deleteResponseContentAsString = adminDeleteResponse.getStatusMessage().trim();

      final String statusMessage = String.format("Stub request index#%s does not exist, cannot delete", indexToDelete);

      assertThat(HttpStatus.NO_CONTENT_204).isEqualTo(adminDeleteResponse.getStatusCode());
      assertThat(deleteResponseContentAsString).contains(statusMessage);
   }

   @Test
   public void should_FailToUpdateStubData_WhenPostBadData() throws Exception {

      final URL url = AdminTest.class.getResource("/yaml/admin.test.class.inavlid.data.yaml");
      assertThat(url).isNotNull();

      final String adminRequestUrl = String.format("%s%s", adminUrlAsString, AdminHandler.ADMIN_ROOT);
      final HttpRequest adminRequest = constructHttpRequest(HttpMethods.POST, adminRequestUrl, StringUtils.inputStreamToString(url.openStream()));

      final HttpResponse adminResponse = adminRequest.execute();
      final String responseContentAsString = adminResponse.parseAsString().trim();

      assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(adminResponse.getStatusCode());
      assertThat(responseContentAsString).contains("Problem handling request in Admin handler");
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
