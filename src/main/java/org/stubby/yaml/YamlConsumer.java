/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.stubby.yaml;

import org.stubby.exception.Stubby4JException;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 9:46 PM
 */
public final class YamlConsumer {

   public static String LOADED_CONFIG;

   private static final String YAMLLINE_KEY = "nodeKey";
   private static final String YAMLLINE_VALUE = "nodeValue";

   private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

   private YamlConsumer() {

   }

   public static List<StubHttpLifecycle> parseYamlContent(final String yamlConfigContent) throws IOException {
      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

      final InputStreamReader inputStreamReader =
            new InputStreamReader(
                  new ByteArrayInputStream(yamlConfigContent.getBytes(Charset.forName("UTF-8"))));
      final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      httpLifecycles.addAll(unmarshallYaml(bufferedReader));
      bufferedReader.close();

      validateStubHttpLifecycles(httpLifecycles);

      return httpLifecycles;
   }

   public static List<StubHttpLifecycle> parseYamlFile(final String yamlConfigFilename) throws IOException {
      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

      final File yamlFile = new File(yamlConfigFilename);
      final String filename = yamlFile.getName().toLowerCase();
      if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
         LOADED_CONFIG = yamlFile.getAbsolutePath();

         final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(yamlFile), Charset.forName("UTF-8"));
         final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         httpLifecycles.addAll(unmarshallYaml(bufferedReader));
         bufferedReader.close();
      }
      validateStubHttpLifecycles(httpLifecycles);

      return httpLifecycles;
   }

   private static void validateStubHttpLifecycles(final List<StubHttpLifecycle> httpLifecycles) {
      if (httpLifecycles.size() == 0) {
         throw new Stubby4JException("No HttpLifecycles loaded.. Please check your YAML configuration");
      }
      for (final StubHttpLifecycle stubHttpLifecycle : httpLifecycles) {
         if (!stubHttpLifecycle.isComplete()) {
            throw new Stubby4JException("Detected incomplete HttpLifecycle.. Did you omit some configuration detail in YAML (url, method or status etc.)?");
         }
      }
   }

   private static final List<StubHttpLifecycle> unmarshallYaml(final BufferedReader buffRead) throws IOException {

      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();
      StubHttpLifecycle parentStub = null;

      for (String yamlLine = buffRead.readLine(); yamlLine != null; yamlLine = buffRead.readLine()) {

         if (yamlLine.trim().isEmpty()) {
            continue;
         }

         final Map<String, String> keyValuePair = breakDownYamlLineToKeyValuePair(yamlLine);

         switch (YamlParentNodes.getFor(keyValuePair.get(YAMLLINE_KEY))) {

            case HTTPLIFECYCLE:
               parentStub = new StubHttpLifecycle(new StubRequest(), new StubResponse());
               httpLifecycles.add(parentStub);
               continue;

            case REQUEST:
               parentStub.setCurrentlyPopulated(YamlParentNodes.REQUEST);
               continue;

            case RESPONSE:
               parentStub.setCurrentlyPopulated(YamlParentNodes.RESPONSE);
               continue;

            case HEADERS:
               continue;

            default:
               bindYamlValueToPojo(keyValuePair, parentStub);
         }
      }
      return httpLifecycles;
   }

   private static Map<String, String> breakDownYamlLineToKeyValuePair(final String yamlLine) {
      final Map<String, String> keyValuePair = new HashMap<String, String>();

      final String[] keyAndValue = yamlLine.split(":", 2);
      final String nodeKey = keyAndValue[0].toLowerCase().trim();
      final String nodeValue = (keyAndValue.length == 2 ? keyAndValue[1].trim() : "");

      keyValuePair.put(YAMLLINE_KEY, nodeKey);
      keyValuePair.put(YAMLLINE_VALUE, nodeValue);

      return keyValuePair;
   }

   private static void bindYamlValueToPojo(final Map<String, String> keyValuePair, final StubHttpLifecycle parentStub) {
      final String nodeName = keyValuePair.get(YAMLLINE_KEY);
      final String nodeValue = keyValuePair.get(YAMLLINE_VALUE);

      if (ReflectionUtils.isFieldCorrespondsToYamlNode(StubRequest.class, nodeName)) {
         setYamlValueToFieldProperty(parentStub, nodeName, nodeValue, YamlParentNodes.REQUEST);

      } else if (ReflectionUtils.isFieldCorrespondsToYamlNode(StubResponse.class, nodeName)) {
         setYamlValueToFieldProperty(parentStub, nodeName, nodeValue, YamlParentNodes.RESPONSE);

      } else {
         setYamlValueToHeaderProperty(parentStub, nodeName, nodeValue);
      }
   }

   private static void setYamlValueToFieldProperty(final StubHttpLifecycle stubHttpLifecycle, final String nodeName, final String nodeValue, final YamlParentNodes type) {
      try {
         if (type.equals(YamlParentNodes.REQUEST)) {
            ReflectionUtils.setValue(stubHttpLifecycle.getRequest(), nodeName, nodeValue);
         } else {
            ReflectionUtils.setValue(stubHttpLifecycle.getResponse(), nodeName, nodeValue);
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