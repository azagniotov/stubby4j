package integration.by.stub.client;

import by.stub.cli.ANSITerminal;
import by.stub.client.ClientRequest;
import by.stub.client.Stubby4JClient;
import by.stub.client.Stubby4JResponse;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.server.JettyFactory;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethods;
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

public class Stubby4JClientAdminIT {

   private static Stubby4JClient stubby4JClient;
   private static String content;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JClientAdminIT.class.getResource("/yaml/stubby4jclientadminit-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      stubby4JClient = new Stubby4JClient(url.getFile());
      stubby4JClient.startJetty();

      content = StringUtils.inputStreamToString(url.openStream());
   }

   @Before
   public void beforeEach() throws Exception {

   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stopJetty();
   }

   @Test
   public void shouldCreateStubbedData() throws Exception {
      final ClientRequest adminRequest = new ClientRequest(HttpMethods.POST, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW, "localhost", JettyFactory.DEFAULT_ADMIN_PORT, content);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);

      Assert.assertEquals(HttpStatus.CREATED_201, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", stubby4JResponse.getContent());
   }

   @Test
   public void shouldCleanUpStubbedData() throws Exception {

      final ClientRequest adminRequest = new ClientRequest(HttpMethods.POST, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW, "localhost", JettyFactory.DEFAULT_ADMIN_PORT, null);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(adminRequest);

      Assert.assertEquals(HttpStatus.NO_CONTENT_204, stubby4JResponse.getResponseCode());
      Assert.assertEquals("POST request on URI null was empty", stubby4JResponse.getContent());
   }

   @Test
   public void shouldNotFindStubRequestFromOriginalAtomFeedData() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/1", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/1", stubby4JResponse.getContent());
   }
}

