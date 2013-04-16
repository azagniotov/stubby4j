package by.stub.yaml;

import by.stub.cli.ANSITerminal;
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.FileUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

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
      assertThat(url).isNotNull();

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      assertThat(loadedHttpCycles.size()).isEqualTo(5);
   }

   @Test
   public void load_ShouldSetDefaultResponse_WhenResponseSectionOmitted() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      assertThat(url).isNotNull();

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      final String expectedStatus = loadedHttpCycles.get(4).getResponse().getStatus();
      assertThat(expectedStatus).isEqualTo("200");
   }

   @Test
   public void load_ShouldSetMethodToHaveGET_WhenMethodSectionOmitted() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      assertThat(url).isNotNull();

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();

      final ArrayList<String> expectedMethod = loadedHttpCycles.get(4).getRequest().getMethod();

      assertThat(expectedMethod).contains("GET");
   }

   @Test
   public void load_ShouldContainFilePathInRequest_WhenFileSpecifiedInYaml() throws Exception {
      final URL url = this.getClass().getResource("/yaml/yamlparser.test.class.data.yaml");
      assertThat(url).isNotNull();

      CommandLineInterpreter.parseCommandLine(new String[]{"--data", url.getFile()});

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.parseAndLoad();
      final StubHttpLifecycle cycle = loadedHttpCycles.get(0);
      final StubRequest request = cycle.getRequest();

      final String expectedPostBody = FileUtils.asciiFileToString("../json/yamlparser.test.class.post.body.json");
      final String actualPostBody = new String(request.getFile(), StringUtils.utf8Charset());

      assertThat(expectedPostBody).isEqualTo(actualPostBody);
   }
}
