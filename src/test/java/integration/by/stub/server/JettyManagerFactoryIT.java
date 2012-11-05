package integration.by.stub.server;

import integration.by.stub.http.client.StubbyClientIT;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import by.stub.cli.ANSITerminal;
import by.stub.server.JettyManager;
import by.stub.server.JettyManagerFactory;

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
      url = StubbyClientIT.class.getResource("/yaml/jettymanagerfactoryit-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      jettyManagerFactory = new JettyManagerFactory();
   }

   @Test
   public void shouldConstructJettyManager() throws Exception {
      final JettyManager jettyManager = jettyManagerFactory.construct(url.getFile(), new HashMap<String, String>());

      Assert.assertNotNull(jettyManager);
   }
}
