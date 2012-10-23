package org.stubby.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserTest {

   @Test
   public void testHashMapEquality() throws Exception {

      final Map<String, String> paramsOne = new HashMap<String, String>() {{
         put("param1", "123");
         put("param2", "456");
      }};

      final Map<String, String> paramsTwo = new HashMap<String, String>() {{
         put("param2", "456");
         put("param1", "123");
      }};

      Assert.assertEquals(paramsOne, paramsTwo);

   }

   @Test
   public void testEmptyHashMapEquality() throws Exception {

      final Map<String, String> paramsOne = new HashMap<String, String>();
      final Map<String, String> paramsTwo = new HashMap<String, String>();

      Assert.assertEquals(paramsOne, paramsTwo);

   }

   @Test
   public void load_ShouldGenerateListOfHttpCycles_WhenValidYamlGiven() throws Exception {
      final URL url = this.getClass().getResource("/httplifecycles.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final Reader yamlReader = yamlParser.buildYamlReaderFromFilename();
      final List<StubHttpLifecycle> loadedHttpCycles = yamlParser.load(yamlReader);

      Assert.assertEquals(2, loadedHttpCycles.size());
   }
}
