package by.stub.yaml;

import by.stub.cli.ANSITerminal;
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.FileUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserTest {

   @BeforeClass
   public static void beforeClass() throws Exception {
      ANSITerminal.muteConsole(true);
   }

   @Test
   public void load_ShouldGenerateListOfHttpCycles_WhenValidYamlGiven() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      Assert.assertNotNull(url);

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals(5, loadedHttpCycles.size());
   }

   @Test
   public void load_ShouldSetDefaultResponse_WhenResponseSectionOmitted() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      Assert.assertNotNull(url);

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals("200", loadedHttpCycles.get(4).getResponse().getStatus());
   }

   @Test
   public void load_ShouldSetMethodToHaveGET_WhenMethodSectionOmitted() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      Assert.assertNotNull(url);

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      Assert.assertEquals(new ArrayList<String>(1) {{
         add("GET");
      }}, loadedHttpCycles.get(4).getRequest().getMethod());
   }

   @Test
   public void load_ShouldContainFilePathInRequest_WhenFileSpecifiedInYaml() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      Assert.assertNotNull(url);

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();
      final StubHttpLifecycle cycle = loadedHttpCycles.get(0);
      final StubRequest request = cycle.getRequest();

      final String expectedPostBody = FileUtils.asciiFileToString("../json/yamlparser.test.class.post.body.json");
      Assert.assertEquals(expectedPostBody, new String(request.getFile(), StringUtils.utf8Charset()));
   }
}
