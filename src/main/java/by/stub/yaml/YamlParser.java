/*
HTTP stub server written in Java with embedded Jetty

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

package by.stub.yaml;

import by.stub.cli.ANSITerminal;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.apache.commons.codec.binary.Base64;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class YamlParser {

   private static final String NODE_REQUEST = "request";

   private String loadedConfigAbsolutePath;
   private String yamlConfigFilename;

   private YamlParser() {

   }

   public YamlParser(final String yamlConfigFilename) {
      if (yamlConfigFilename == null) {
         throw new IllegalArgumentException("Given YAML config filename is null!");
      }
      this.yamlConfigFilename = yamlConfigFilename;
   }

   private Reader buildYamlReaderFromFilename() throws IOException {

      final File yamlFile = new File(yamlConfigFilename);
      final String filename = StringUtils.toLower(yamlFile.getName());

      if (!filename.endsWith(".yaml") && !filename.endsWith(".yml")) {
         throw new IOException(String.format("The given filename %s does not ends with YAML or YML", yamlConfigFilename));
      }

      loadedConfigAbsolutePath = yamlFile.getAbsolutePath();

      return new InputStreamReader(new FileInputStream(yamlFile), StringUtils.utf8Charset());
   }

   //TODO Ability get response from WWW via HTTP or ability to load non-textual files, eg.: images, PDFs etc.
   private String loadResponseBodyFromFile(final String filePath) throws IOException {
      final File responseFileFromFilesystem = new File(filePath);
      if (!responseFileFromFilesystem.isFile()) {
         throw new IOException(String.format("Could not load file from path: %s", filePath));
      }

      return StringUtils.inputStreamToString(new FileInputStream(responseFileFromFilesystem));
   }

   public List<StubHttpLifecycle> parseAndLoad() throws Exception {
      return parseAndLoad(buildYamlReaderFromFilename());
   }

   @SuppressWarnings("unchecked")
   public List<StubHttpLifecycle> parseAndLoad(final Reader io) throws Exception {

      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();
      final List<?> loadedYamlData = loadYamlData(io);

      if (loadedYamlData.isEmpty()) {
         return httpLifecycles;
      }

      for (final Object rawParentNode : loadedYamlData) {

         final LinkedHashMap<String, LinkedHashMap> parentNode = (LinkedHashMap<String, LinkedHashMap>) rawParentNode;

         final StubHttpLifecycle parentStub = new StubHttpLifecycle(new StubRequest(), new StubResponse());
         httpLifecycles.add(parentStub);

         mapParentYamlNodeToPojo(parentStub, parentNode);

         final String method = parentStub.getRequest().getMethod();
         final String url = parentStub.getRequest().getUrl();
         final String loadedMsg = String.format("Loaded: %s %s", method, url);
         ANSITerminal.loaded(loadedMsg);
      }

      return httpLifecycles;
   }

   @SuppressWarnings("unchecked")
   protected void mapParentYamlNodeToPojo(final StubHttpLifecycle parentStub, final LinkedHashMap<String, LinkedHashMap> parentNode) throws Exception {
      for (final Map.Entry<String, LinkedHashMap> parent : parentNode.entrySet()) {

         final LinkedHashMap<String, Object> httpSettings = (LinkedHashMap<String, Object>) parent.getValue();

         if (parent.getKey().equals(NODE_REQUEST)) {
            mapHttpSettingsToPojo(parentStub.getRequest(), httpSettings);
            continue;
         }

         mapHttpSettingsToPojo(parentStub.getResponse(), httpSettings);
      }
   }

   @SuppressWarnings("unchecked")
   protected void mapHttpSettingsToPojo(final Object target, final LinkedHashMap<String, Object> httpProperties) throws Exception {

      for (final Map.Entry<String, Object> pair : httpProperties.entrySet()) {

         final Object value = pair.getValue();
         final String propertyName = pair.getKey();

         if (value instanceof Map) {
            final Map<String, String> keyValues = encodeAuthorizationHeader((Map<String, String>) value);
            ReflectionUtils.setPropertyValue(target, propertyName, keyValues);
            continue;
         }

         final String propertyValue = extractPropertyValueAsString(propertyName, value);
         ReflectionUtils.setPropertyValue(target, propertyName, propertyValue);
      }
   }

   private String extractPropertyValueAsString(final String propertyName, final Object value) throws IOException {
      final String valueAsString = value.toString();

      return propertyName.equalsIgnoreCase("file") ?
            loadResponseBodyFromFile(valueAsString) : valueAsString;
   }

   protected Map<String, String> encodeAuthorizationHeader(final Map<String, String> value) {
      if (!value.containsKey(StubRequest.AUTH_HEADER)) {
         return value;
      }

      final String authorizationHeader = value.get(StubRequest.AUTH_HEADER);
      final byte[] bytes = authorizationHeader.getBytes(StringUtils.utf8Charset());
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", Base64.encodeBase64String(bytes));
      value.put(StubRequest.AUTH_HEADER, encodedAuthorizationHeader);

      return value;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {
      final Yaml yaml = new Yaml(new Constructor(), new Representer(),
            new DumperOptions(), new YamlParserResolver());
      final Object loadedYaml = yaml.load(io);

      if (loadedYaml instanceof ArrayList) {
         return (ArrayList<?>) loadedYaml;
      }

      throw new IOException(String.format("Loaded YAML data from %s must be an instance of ArrayList, otherwise something went wrong..", yamlConfigFilename));
   }

   public String getLoadedConfigYamlPath() {
      return loadedConfigAbsolutePath;
   }

   private static final class YamlParserResolver extends Resolver {

      public YamlParserResolver() {
         super();
      }

      @Override
      protected void addImplicitResolvers() {
         // no implicit resolvers - resolve everything to String
      }
   }
}