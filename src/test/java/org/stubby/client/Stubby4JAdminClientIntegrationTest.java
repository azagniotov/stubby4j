package org.stubby.client;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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

      stubby4JClient = Stubby4JClientFactory.getInstance();
      stubby4JClient.start();

      content = HandlerUtils.inputStreamToString(url.openStream());
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stop();
      Thread.sleep(2000); //To make sure Jetty has stopped before running another suite
   }

   @Test
   public void shoudlCreateStubbedData() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.registerStubData(content, "localhost", 8889);

      Assert.assertEquals(201, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", stubby4JResponse.getContent());
   }

   @Test
   public void shoudlCleanUpStubbedData() throws Exception {
      stubby4JClient.registerStubData(content, "localhost", 8889);

      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("/item/8", "localhost", 8882);
      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"butter\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shoudlNotFindStubRequestFromOriginalAtomFeedData() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("/item/1", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/1", stubby4JResponse.getContent());
   }
}
