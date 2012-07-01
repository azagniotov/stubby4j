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
public class Stubby4JTest {

   private static Stubby4J stubby4J;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JTest.class.getResource("/atom-feed.yaml");
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
   public void shouldDoPostOnURI() throws Exception {
      final Map<String, String> result = stubby4J.doPostOnURI("/item/1", "post body");
      Assert.assertEquals("200", result.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals("Got post response", result.get(Stubby4J.KEY_RESPONSE));
   }
}
