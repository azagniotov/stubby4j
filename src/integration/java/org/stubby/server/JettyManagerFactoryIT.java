package org.stubby.server;

import org.stubby.client.Stubby4JClientStubsIT;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.cli.ANSITerminal;
import org.stubby.server.JettyManager;
import org.stubby.server.JettyManagerFactory;

import java.net.URL;
import java.util.HashMap;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 1:31 PM
 */
public class JettyManagerFactoryIT {

   private static URL url;
   private static JettyManagerFactory jettyManagerFactory;

   @BeforeClass
   public static void beforeClass() throws Exception {
      url = Stubby4JClientStubsIT.class.getResource("/atom-feed.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.mute = true;
      jettyManagerFactory = new JettyManagerFactory();
   }

   @Test
   public void shouldConstructJettyManager() throws Exception {
      final JettyManager jettyManager = jettyManagerFactory.construct(url.getFile(), new HashMap<String, String>());

      Assert.assertNotNull(jettyManager);
   }
}
