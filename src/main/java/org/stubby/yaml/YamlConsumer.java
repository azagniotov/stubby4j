package org.stubby.yaml;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 9:46 PM
 */
public final class YamlConsumer {

   private YamlConsumer() {

   }

   public final static Node readYaml(final File yamlFile) throws FileNotFoundException {
      final String filename = yamlFile.getName().toLowerCase();
      if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
         final Reader reader = new InputStreamReader(new FileInputStream(yamlFile), Charset.forName("UTF-8"));
         return new Yaml().compose(reader);
      }
      return null;
   }

   public final static Node readYaml(final String yamlConfigFilename) throws FileNotFoundException {
      final File yamlFile = new File(yamlConfigFilename);
      return readYaml(yamlFile);
   }

   /*
   private void build(Node yaml, JsonGenerator json) throws IOException {
        if (yaml instanceof MappingNode) {
            final MappingNode mappingNode = (MappingNode) yaml;
            json.writeStartObject();
            for (NodeTuple tuple : mappingNode.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    json.writeFieldName(((ScalarNode) tuple.getKeyNode()).getValue());
                }

                build(tuple.getValueNode(), json);
            }
            json.writeEndObject();
        } else if (yaml instanceof SequenceNode) {
            json.writeStartArray();
            for (Node node : ((SequenceNode) yaml).getValue()) {
                build(node, json);
            }
            json.writeEndArray();
        } else if (yaml instanceof ScalarNode) {
            final ScalarNode scalarNode = (ScalarNode) yaml;
            final String className = scalarNode.getTag().getClassName();
            if ("bool".equals(className)) {
                json.writeBoolean(Boolean.parseBoolean(scalarNode.getValue()));
            } else if ("int".equals(className)) {
                json.writeNumber(Long.parseLong(scalarNode.getValue()));
            } else if ("float".equals(className)) {
                json.writeNumber(Double.parseDouble(scalarNode.getValue()));
            } else if ("null".equals(className)) {
                json.writeNull();
            } else {
                json.writeString(scalarNode.getValue());
            }
        }
    }   / */
}
