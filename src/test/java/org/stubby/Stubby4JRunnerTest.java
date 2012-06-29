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
public class Stubby4JRunnerTest {

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JRunnerTest.class.getResource("/atom-feed.yaml");
      Assert.assertNotNull(url);
      Stubby4JRunner.startStubby4J(url.openStream());
   }

   @Test
   public void sanityCheck() throws Exception {
      final Map<String, String> result = Stubby4JRunner.doGetOnURI("/item/1");
      Assert.assertEquals("200", result.get("status"));
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", result.get("response"));
   }

   @AfterClass
   public static void afterClass() throws Exception {
      Stubby4JRunner.stopStubby4J();
   }
}
