package integration;

import org.eclipse.jetty.http.HttpMethods;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.client.ClientRequestInfo;
import org.stubby.client.Stubby4JClient;
import org.stubby.client.Stubby4JClientFactory;
import org.stubby.client.Stubby4JResponse;
import org.stubby.handlers.AdminHandler;
import org.stubby.utils.HandlerUtils;

import java.net.URL;

/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */
public class Stubby4JAdminClientIntegrationTest {

   private static Stubby4JClient stubby4JClient;
   private static String content;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JAdminClientIntegrationTest.class.getResource("/atom-feed-for-content-tests.yaml");
      Assert.assertNotNull(url);

      stubby4JClient = Stubby4JClientFactory.getInstance(url.getFile());
      stubby4JClient.start();

      content = HandlerUtils.inputStreamToString(url.openStream());
   }

   @Before
   public void beforeEach() throws Exception {
      final ClientRequestInfo adminRequest = new ClientRequestInfo(HttpMethods.POST, AdminHandler.RESOURCE_STUBDATA_NEW, "localhost", 8889, content);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stop();
      Thread.sleep(2000); //To make sure Jetty has stopped before running another suite
   }

   @Test
   public void shouldCreateStubbedData() throws Exception {
      final ClientRequestInfo adminRequest = new ClientRequestInfo(HttpMethods.POST, AdminHandler.RESOURCE_STUBDATA_NEW, "localhost", 8889, content);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);

      Assert.assertEquals(201, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", stubby4JResponse.getContent());
   }

   @Test
   public void shouldCleanUpStubbedData() throws Exception {

      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/8", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"butter\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldNotFindStubRequestFromOriginalAtomFeedData() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/1", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/1", stubby4JResponse.getContent());
   }
}
