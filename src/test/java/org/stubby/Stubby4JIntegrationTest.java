package org.stubby;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */
public class Stubby4JIntegrationTest {

   private static Stubby4J stubby4J;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JIntegrationTest.class.getResource("/atom-feed.yaml");
      Assert.assertNotNull(url);

      stubby4J = new Stubby4J(url.getFile());
      stubby4J.start();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4J.stop();
   }

   @Test
   public void shouldDoGetOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doGetOnURI("/item/1");
      Assert.assertEquals("200", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoGetOnEmptyURI() throws Exception {
      final Map<String, String> result = stubby4J.doGetOnURI("");
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for GET request at URI /", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoGetOnNullURI() throws Exception {
      final Map<String, String> result = stubby4J.doGetOnURI(null);
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for GET request at URI /", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoGetOnIncorrectURI() throws Exception {
      final Map<String, String> result = stubby4J.doGetOnURI("/item/888");
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for GET request at URI /item/888", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoPostOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("/item/1", "post body");
      Assert.assertEquals("200", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("Got post response", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoPostOnEmptyURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("", "post body");
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for POST request at URI / for post data: post body",
            result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldDoPostOnNullURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI(null, "post body");
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for POST request at URI / for post data: post body",
            result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldFailWhenDoingIncorrectPostOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("/item/1", "a");
      Assert.assertEquals("404", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("No data found for POST request at URI /item/1 for post data: a",
            result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldFailWhenDoingEmptyPostOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("/item/1", "");
      Assert.assertEquals("400", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("Oh oh :( Bad request, POST body is missing", result.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldFailWhenDoingNullPostOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("/item/1", null);
      Assert.assertEquals("400", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("Oh oh :( Bad request, POST body is missing", result.get(Stubby4J.KEY_RESPONSE));
   }
}
