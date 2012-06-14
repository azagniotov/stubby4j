package org.stubby.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.nodes.Node;

import java.io.File;
import java.net.URL;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 10:29 PM
 */
public class YamlConsumerTest {

   @Test
   public void sanityCheck() throws Exception {
      Assert.assertTrue(true);
   }

   @Test
   public void testReadYaml() throws Exception {
      final URL url = this.getClass().getResource("/endpoints.yaml");
      final File yaml = new File(url.getFile());
      final Node yamlConfig = YamlConsumer.readYaml(yaml);
      Assert.assertTrue(true);
   }
}
