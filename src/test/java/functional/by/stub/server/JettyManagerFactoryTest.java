package functional.by.stub.server;

import by.stub.cli.ANSITerminal;
import by.stub.server.JettyManager;
import by.stub.server.JettyManagerFactory;
import by.stub.testing.junit.categories.FunctionalTest;
import integration.by.stub.http.client.StubbyClientTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URL;
import java.util.HashMap;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 1:31 PM
 */
@Category(FunctionalTest.class)
public class JettyManagerFactoryTest {

   private static URL url;
   private static JettyManagerFactory jettyManagerFactory;

   @BeforeClass
   public static void beforeClass() throws Exception {
      url = StubbyClientTest.class.getResource("/yaml/jettymanagerfactoryit-test-data.yaml");
      Assert.assertNotNull(url);

      jettyManagerFactory = new JettyManagerFactory();

      ANSITerminal.muteConsole(true);
   }

   @Test
   public void shouldConstructJettyManager() throws Exception {
      final JettyManager jettyManager = jettyManagerFactory.construct(url.getFile(), new HashMap<String, String>());

      Assert.assertNotNull(jettyManager);
   }
}
