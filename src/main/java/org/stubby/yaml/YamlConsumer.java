package org.stubby.yaml;

import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 9:46 PM
 */
public final class YamlConsumer {

   private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   private static final List<String> nodesWithoutSiblingValues;

   static {
      final List<String> list =
            Arrays.asList(YamlParentNodes.HTTPLIFECYCLE.desc(),
                  YamlParentNodes.REQUEST.desc(),
                  YamlParentNodes.RESPONSE.desc(),
                  YamlParentNodes.HEADERS.desc());
      nodesWithoutSiblingValues = new LinkedList<String>(list);
   }

   private YamlConsumer() {

   }

   public static List<StubHttpLifecycle> readYaml(final File yamlFile) throws IOException {
      final String filename = yamlFile.getName().toLowerCase();
      if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
         final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(yamlFile), Charset.forName("UTF-8"));
         final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         logger.info("Loaded YAML " + filename);

         return transformYamlNode(bufferedReader, null);
      }
      return new LinkedList<StubHttpLifecycle>();
   }

   public static List<StubHttpLifecycle> readYaml(final String yamlConfigFilename) throws IOException {
      final File yamlFile = new File(yamlConfigFilename);
      return readYaml(yamlFile);
   }

   protected static List<StubHttpLifecycle> transformYamlNode(final BufferedReader bufferedReader, StubHttpLifecycle parentStub) throws IOException {
      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

      String yamlLine;
      while ((yamlLine = bufferedReader.readLine()) != null) {
         yamlLine = yamlLine.trim();
         if (yamlLine.isEmpty()) {
            continue;
         }

         final int indexOfColumn = yamlLine.indexOf(":");
         final String nodeKey = yamlLine.substring(0, indexOfColumn).toLowerCase().trim();
         final String nodeValue = yamlLine.substring(indexOfColumn + 1, yamlLine.length()).trim();

         if (nodeKey.equals(YamlParentNodes.HTTPLIFECYCLE.desc())) {
            parentStub = new StubHttpLifecycle(new StubRequest(), new StubResponse());
            httpLifecycles.add(parentStub);

         } else if (nodeKey.equals(YamlParentNodes.REQUEST.desc())) {
            parentStub.setCurrentlyPopulated(YamlParentNodes.REQUEST);

         } else if (nodeKey.equals(YamlParentNodes.RESPONSE.desc())) {
            parentStub.setCurrentlyPopulated(YamlParentNodes.RESPONSE);

         } else if (!nodesWithoutSiblingValues.contains(nodeKey)) {
            bindYamlValueToPojo(nodeKey, nodeValue, parentStub);
         }
      }
      bufferedReader.close();
      return httpLifecycles;
   }

   private static void bindYamlValueToPojo(final String nodeName, final String nodeValue, final StubHttpLifecycle parentStub) {

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