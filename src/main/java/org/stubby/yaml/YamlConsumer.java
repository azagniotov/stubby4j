package org.stubby.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 9:46 PM
 */
public final class YamlConsumer {

   private static final Logger logger = LoggerFactory.getLogger(YamlConsumer.class);
   private static final List<String> skippableNodeNames;

   static {
      final List<String> list =
            Arrays.asList(YamlParentNodes.HTTPLIFECYCLE.desc(),
                  YamlParentNodes.REQUEST.desc(),
                  YamlParentNodes.RESPONSE.desc(),
                  YamlParentNodes.HEADERS.desc());
      skippableNodeNames = new LinkedList<String>(list);
   }

   private YamlConsumer() {

   }

   public static List<StubHttpLifecycle> readYaml(final File yamlFile) throws FileNotFoundException {
      final String filename = yamlFile.getName().toLowerCase();
      if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
         final Reader reader = new InputStreamReader(new FileInputStream(yamlFile), Charset.forName("UTF-8"));
         logger.info("Loaded YAML " + filename);

         final Node rootNode = new Yaml().compose(reader);
         final List<StubHttpLifecycle> stubHttpLifecycle = transformYamlNode(rootNode, null);

         return stubHttpLifecycle;
      }
      return new LinkedList<StubHttpLifecycle>();
   }

   public static List<StubHttpLifecycle> readYaml(final String yamlConfigFilename) throws FileNotFoundException {
      final File yamlFile = new File(yamlConfigFilename);
      return readYaml(yamlFile);
   }

   protected static List<StubHttpLifecycle> transformYamlNode(final Node rootNode, StubHttpLifecycle parentStub) {
      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

      if (rootNode instanceof MappingNode) {
         final MappingNode mappingNode = (MappingNode) rootNode;

         for (final NodeTuple tuple : mappingNode.getValue()) {

            if (tuple.getKeyNode() instanceof ScalarNode) {

               final ScalarNode keyScalarNode = (ScalarNode) tuple.getKeyNode();
               final String nodeName = keyScalarNode.getValue().toLowerCase();

               if (nodeName.equals(YamlParentNodes.HTTPLIFECYCLE.desc())) {
                  parentStub = new StubHttpLifecycle(new StubRequest(), new StubResponse());
                  httpLifecycles.add(parentStub);

               } else if (nodeName.equals(YamlParentNodes.REQUEST.desc())) {
                  parentStub.setCurrentlyPopulated(YamlParentNodes.REQUEST);

               } else if (nodeName.equals(YamlParentNodes.RESPONSE.desc())) {
                  parentStub.setCurrentlyPopulated(YamlParentNodes.RESPONSE);
               }

               if (skippableNodeNames.contains(nodeName)) {
                  transformYamlNode(tuple.getValueNode(), parentStub);
               } else {
                  final ScalarNode valueScalarNode = (ScalarNode) tuple.getValueNode();
                  bindYamlValueToPojo(valueScalarNode, nodeName, parentStub);
               }
            }
         }
      }
      return httpLifecycles;
   }

   private static void bindYamlValueToPojo(final ScalarNode valueScalarNode, final String nodeName, final StubHttpLifecycle parentStub) {
      final String nodeValue = valueScalarNode.getValue();

      if (StubRequest.isFieldCorrespondsToYamlNode(nodeName)) {
         setYamlValueToFieldProperty(parentStub, nodeName, nodeValue, YamlParentNodes.REQUEST);

      } else if (StubResponse.isFieldCorrespondsToYamlNode(nodeName)) {
         setYamlValueToFieldProperty(parentStub, nodeName, nodeValue, YamlParentNodes.RESPONSE);

      } else {
         setYamlValueToHeaderProperty(parentStub, nodeName, nodeValue);
      }
   }

   private static void setYamlValueToFieldProperty(final StubHttpLifecycle stubHttpLifecycle, final String nodeName, final String nodeValue, final YamlParentNodes type) {
      try {
         if (type.equals(YamlParentNodes.REQUEST)) {
            stubHttpLifecycle.getRequest().setValue(nodeName, nodeValue);
         } else {
            stubHttpLifecycle.getResponse().setValue(nodeName, nodeValue);
         }
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }
      stubHttpLifecycle.setCurrentlyPopulated(type);
   }

   private static void setYamlValueToHeaderProperty(final StubHttpLifecycle stubHttpLifecycle, final String nodeName, final String nodeValue) {
      if (stubHttpLifecycle.getCurrentlyPopulated().equals(YamlParentNodes.REQUEST)) {
         stubHttpLifecycle.getRequest().addHeader(nodeName, nodeValue);
      } else if (stubHttpLifecycle.getCurrentlyPopulated().equals(YamlParentNodes.RESPONSE)) {
         stubHttpLifecycle.getResponse().addHeader(nodeName, nodeValue);
      }
   }
}