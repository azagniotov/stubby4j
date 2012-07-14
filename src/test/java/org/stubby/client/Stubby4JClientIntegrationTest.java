package org.stubby.client;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */
public class Stubby4JClientIntegrationTest {

   private static Stubby4JClient stubby4JClient;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JClientIntegrationTest.class.getResource("/atom-feed.yaml");
      Assert.assertNotNull(url);

      stubby4JClient = Stubby4JClientFactory.getInstance(url.getFile());
      stubby4JClient.start();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stop();
      Thread.sleep(2000); //To make sure Jetty has stopped before running another suite
   }

   @Test
   public void shoudlRegisterNewEndpoint() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("/item/1", "localhost", 8882);
      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("/item/1", "localhost", 8882);
      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnEmptyURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnNullURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI(null, "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnIncorrectURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doGetOnURI("/item/888", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/888", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI("/item/1", "post body", "localhost", 8882);
      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Got post response", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnEmptyURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI("", "post body", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body",
            stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnNullURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI(null, "post body", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body",
            stubby4JResponse.getContent());
   }

   @Test
   public void shouldFailWhenDoingIncorrectPostOnURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI("/item/1", "a", "localhost", 8882);
      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1 for post data: a",
            stubby4JResponse.getContent());
   }

   @Test
   public void shouldFailWhenDoingEmptyPostOnURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI("/item/1", "", "localhost", 8882);
      Assert.assertEquals(400, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Oh oh :( Bad request, POST body is missing", stubby4JResponse.getContent());
   }

   @Test
   public void shouldFailWhenDoingNullPostOnURI() throws Exception {
      final Stubby4JResponse stubby4JResponse = stubby4JClient.doPostOnURI("/item/1", null, "localhost", 8882);
      Assert.assertEquals(400, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Oh oh :( Bad request, POST body is missing", stubby4JResponse.getContent());
   }
}
