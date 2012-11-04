package integration.by.stub.client;

import by.stub.cli.ANSITerminal;
import by.stub.client.http.ClientHttpResponse;
import by.stub.client.http.StubbyClient;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.server.JettyFactory;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;


/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */

public class StubbyHttpClientAdminIT {


   private static StubbyClient stubbyClient;
   private static String content;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = StubbyHttpClientAdminIT.class.getResource("/yaml/stubby4jclientadminit-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      stubbyClient = new StubbyClient(url.getFile());
      stubbyClient.startJetty();

      content = StringUtils.inputStreamToString(url.openStream());
   }

   @Before
   public void beforeEach() throws Exception {

   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubbyClient.stopJetty();
   }

   @Test
   public void makePostRequest_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.makePostRequest(host, uri, port, content);

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", clientHttpResponse.getContent());
   }

   @Test
   public void makePostRequest_ShouldMakeSuccessfulPost_WhenPostStubDataIsEmpty() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.makePostRequest(host, uri, port, "");

      Assert.assertEquals(HttpStatus.NO_CONTENT_204, clientHttpResponse.getResponseCode());
      Assert.assertEquals("POST request on URI null was empty", clientHttpResponse.getContent());
   }

   @Test
   public void makePostRequest_ShouldMakeSuccessfulPost_WhenPostStubDataIsNull() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.makePostRequest(host, uri, port, null);

      Assert.assertEquals(HttpStatus.NO_CONTENT_204, clientHttpResponse.getResponseCode());
      Assert.assertEquals("POST request on URI null was empty", clientHttpResponse.getContent());
   }
}