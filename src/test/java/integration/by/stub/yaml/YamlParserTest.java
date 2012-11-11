package integration.by.stub.yaml;

import by.stub.testing.categories.IntegrationTests;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
@Category(IntegrationTests.class)
public class YamlParserTest {

   @Test
   public void load_ShouldGenerateListOfHttpCycles_WhenValidYamlGiven() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparserit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals(2, loadedHttpCycles.size());
   }

   @Test
   public void load_ShouldContainFileContentInRequest_WhenFileSpecifiedInYaml() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparserit-request-with-post-file-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();
      final StubHttpLifecycle cycle = loadedHttpCycles.get(0);
      final StubRequest request = cycle.getRequest();

      final InputStream loadedInputStream = YamlParserTest.class.getResourceAsStream("/json/post-body-as-file.json");
      final String loadedJson = StringUtils.inputStreamToString(loadedInputStream);

      Assert.assertEquals(loadedJson, request.getFile());
   }
}
