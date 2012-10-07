package org.stubby.yaml;

import org.apache.commons.codec.binary.Base64;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class YamlParser {

   private String loadedConfigAbsolutePath;
   private String yamlConfigFilename;

   private YamlParser() {

   }

   public YamlParser(final String yamlConfigFilename) {
      synchronized (this) {
         if (yamlConfigFilename == null) {
            throw new IllegalArgumentException("Given YAML config filename is null!");
         }
         this.yamlConfigFilename = yamlConfigFilename;
      }
   }

   public Reader buildYamlReaderFromFilename() throws IOException {

      final File yamlFile = new File(yamlConfigFilename);
      final String filename = yamlFile.getName().toLowerCase();

      if (!filename.endsWith(".yaml") && !filename.endsWith(".yml")) {
         throw new IOException(String.format("The given filename %s does not ends with YAML or YML", yamlConfigFilename));
      }

      loadedConfigAbsolutePath = yamlFile.getAbsolutePath();

      return new InputStreamReader(new FileInputStream(yamlFile), Charset.forName("UTF-8"));
   }


   @SuppressWarnings("unchecked")
   public List<StubHttpLifecycle> load(final Reader io) throws IOException {
      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

      final List<?> parseYAMLContents = loadListOfElementsThroughSnakeYAML(io);

      if (parseYAMLContents.isEmpty()) {
         return httpLifecycles;
      }

      for (final Object yamlSectionObj : parseYAMLContents) {

         final LinkedHashMap<String, LinkedHashMap> yamlSection = (LinkedHashMap<String, LinkedHashMap>) yamlSectionObj;

         final StubHttpLifecycle parentStub = new StubHttpLifecycle(new StubRequest(), new StubResponse());
         httpLifecycles.add(parentStub);

         mapYamlContentToPojos(parentStub, yamlSection);
      }

      return httpLifecycles;
   }

   private void mapYamlContentToPojos(final StubHttpLifecycle parentStub, final LinkedHashMap<String, LinkedHashMap> yamlSection) throws IOException {
      for (final Map.Entry<String, LinkedHashMap> section : yamlSection.entrySet()) {
         final String key = section.getKey();
         final LinkedHashMap<String, Object> keyValuePair = (LinkedHashMap<String, Object>) section.getValue();

         switch (YamlParentNodes.getFor(key)) {

            case REQUEST:
               mapYamlNodeToPojoProperty(parentStub.getRequest(), keyValuePair);
               break;

            case RESPONSE:
               mapYamlNodeToPojoProperty(parentStub.getResponse(), keyValuePair);

         }
      }
   }

   private void mapYamlNodeToPojoProperty(final Object target, final LinkedHashMap<String, Object> keyValuePair) throws IOException {
      for (final Map.Entry<String, Object> pair : keyValuePair.entrySet()) {

         final Object value = pair.getValue();

         try {
            if (value instanceof Map) {
               final Map<String, String> headers = handleHeaderValues((Map<String, String>) value);
               ReflectionUtils.setValue(target, pair.getKey(), headers);
               continue;
            }
            final String propertyValue = (value != null ? value.toString() : "");
            ReflectionUtils.setValue(target, pair.getKey(), propertyValue);

         } catch (final Exception ex) {
            throw new IOException(String.format("Could not parse YAML %s", yamlConfigFilename), ex);
         }
      }
   }

   private Map<String, String> handleHeaderValues(final Map<String, String> value) {
      final Map<String, String> headers = (Map<String, String>) value;
      if (headers.containsKey(HttpRequestInfo.AUTH_HEADER)) {
         final String authorizationHeader = headers.get(HttpRequestInfo.AUTH_HEADER);
         final byte[] bytes = authorizationHeader.getBytes(Charset.forName("UTF-8"));
         final String encodedAuthorizationHeader = String.format("%s %s", "Basic", new String(Base64.encodeBase64(bytes)));
         headers.put(HttpRequestInfo.AUTH_HEADER, encodedAuthorizationHeader);
      }
      return headers;
   }

   private List<?> loadListOfElementsThroughSnakeYAML(final Reader io) throws IOException {
      try {
         final Yaml yaml = new Yaml();
         final Object loadedYaml = yaml.load(io);

         if (!(loadedYaml instanceof ArrayList)) {
            throw new IOException(String.format("YAML %s was not parsed correctly", yamlConfigFilename));
         }

         return (ArrayList<?>) loadedYaml;
      } catch (final Exception ex) {
         throw new IOException(ex);
      }
   }

   public String getLoadedConfigYamlPath() {
      return loadedConfigAbsolutePath;
   }
}