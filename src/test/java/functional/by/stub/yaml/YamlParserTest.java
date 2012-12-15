package functional.by.stub.yaml;

import by.stub.cli.ANSITerminal;
import by.stub.testing.junit.categories.FunctionalTest;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URL;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
@Category(FunctionalTest.class)
public class YamlParserTest {

   @BeforeClass
   public static void beforeClass() throws Exception {
      ANSITerminal.muteConsole(true);
   }

   @Test
   public void load_ShouldGenerateListOfHttpCycles_WhenValidYamlGiven() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparserit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals(3, loadedHttpCycles.size());
   }

   @Test
   public void load_ShouldContainFilePathInRequest_WhenFileSpecifiedInYaml() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparserit-request-with-post-file-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();
      final StubHttpLifecycle cycle = loadedHttpCycles.get(0);
      final StubRequest request = cycle.getRequest();

      Assert.assertEquals("../json/post-body-as-file.json", request.getFile());
   }
}
