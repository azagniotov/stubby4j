package integration.by.stub.database.thread;

import by.stub.database.DataStore;
import by.stub.database.thread.ConfigurationScanner;
import by.stub.testing.junit.categories.IntegrationTest;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 11/10/12, 10:05 AM
 */
@SuppressWarnings("serial")
@Category(IntegrationTest.class)
public class ConfigurationScannerTest {

   private static ConfigurationScanner configurationScanner;

   @BeforeClass
   public static void beforeClass() throws Exception {

      final URL url = ConfigurationScannerTest.class.getResource("/yaml/datastoreit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad();
      final DataStore dataStore = new DataStore(stubHttpLifecycles);

      configurationScanner = new ConfigurationScanner(yamlParser, dataStore);
   }

   @Test
   public void run_ShouldSomething_WhenSomething() throws IOException {

   }
}
