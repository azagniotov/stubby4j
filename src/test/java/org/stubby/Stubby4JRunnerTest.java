package org.stubby;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.server.JettyOrchestrator;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
      final String url = String.format("http://%s:%s/atomfeed/1", JettyOrchestrator.currentHost, JettyOrchestrator.currentClientPort);
      final URL atomFeed = new URL(url);
      final URLConnection con = atomFeed.openConnection();
      final InputStream inputStream = con.getInputStream();

      Assert.assertNotNull(inputStream);
   }

   @AfterClass
   public static void afterClass() throws Exception {
      Stubby4JRunner.stopStubby4J();
   }
}
