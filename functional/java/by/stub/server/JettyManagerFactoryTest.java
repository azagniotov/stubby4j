package by.stub.server;

import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.yaml.YamlParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 1:31 PM
 */
public class JettyManagerFactoryTest {

   private static URL url;
   private static JettyManagerFactory jettyManagerFactory;

   @BeforeClass
   public static void beforeClass() throws Exception {
      url = JettyManagerFactoryTest.class.getResource("/yaml/jettymanagerfactory.test.class.data.yaml");
      Assert.assertNotNull(url);

      jettyManagerFactory = new JettyManagerFactory();

      ANSITerminal.muteConsole(true);
   }

   @Test
   public void shouldConstructJettyManager() throws Exception {
      final JettyManager jettyManager = jettyManagerFactory.construct(url.getFile(), new HashMap<String, String>());

      Assert.assertNotNull(jettyManager);
   }

   @Test
   public void shouldWatchDataFileChanges_WhenWatchFlagGivenInCommandLine() throws Exception {
      final JettyManagerFactory spy = spy(jettyManagerFactory);

      spy.construct(url.getFile(), new HashMap<String, String>() {{
         put("watch", "");
      }});

      verify(spy).watchDataStore(any(YamlParser.class), any(DataStore.class));
   }

   @Test
   public void shouldNOTWatchDataFileChanges_WhenWatchFlagOmittedFromCommandLine() throws Exception {
      final JettyManagerFactory spy = spy(jettyManagerFactory);
      spy.construct(url.getFile(), new HashMap<String, String>());

      verify(spy, never()).watchDataStore(any(YamlParser.class), any(DataStore.class));
   }
}
