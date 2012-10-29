package integration.org.stubby.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.net.URL;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserIT {

   @Test
   public void load_ShouldGenerateListOfHttpCycles_WhenValidYamlGiven() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparserit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals(2, loadedHttpCycles.size());
   }
}
