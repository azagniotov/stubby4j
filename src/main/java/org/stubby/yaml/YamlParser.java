package org.stubby.yaml;

import org.apache.commons.codec.binary.Base64;
import org.stubby.cli.ANSITerminal;
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
      if (yamlConfigFilename == null) {
         throw new IllegalArgumentException("Given YAML config filename is null!");
      }
      this.yamlConfigFilename = yamlConfigFilename;
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
      final List<?> loadedYamlData = loadYamlData(io);

      if (loadedYamlData.isEmpty()) return httpLifecycles;

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
   protected void mapParentYamlNodeToPojo(final StubHttpLifecycle parentStub, final LinkedHashMap<String, LinkedHashMap> parentNode) throws IOException {
      for (final Map.Entry<String, LinkedHashMap> parent : parentNode.entrySet()) {

         final LinkedHashMap<String, Object> httpSettings = (LinkedHashMap<String, Object>) parent.getValue();

         switch (YamlParentNodes.getFor(parent.getKey())) {
            case REQUEST:
               mapHttpSettingsToPojo(parentStub.getRequest(), httpSettings);
               break;

            case RESPONSE:
               mapHttpSettingsToPojo(parentStub.getResponse(), httpSettings);

         }
      }
   }

   @SuppressWarnings("unchecked")
   protected void mapHttpSettingsToPojo(final Object target, final LinkedHashMap<String, Object> httpProperties) throws IOException {

      for (final Map.Entry<String, Object> pair : httpProperties.entrySet()) {

         final Object value = pair.getValue();
         final String propertyName = pair.getKey();

         try {
            if (value instanceof Map) {
               final Map<String, String> headers = encodeAuthorizationHeader((Map<String, String>) value);
               ReflectionUtils.setPropertyValue(target, propertyName, headers);
               continue;
            }

            final String propertyValue = (value != null ? value.toString() : "");
            ReflectionUtils.setPropertyValue(target, propertyName, propertyValue);

         } catch (final Exception ex) {
            throw new IOException(String.format("Could not assign value '%s' to property '%s' on POJO: %s", value, propertyName, target.getClass().getCanonicalName()), ex);
         }
      }
   }

   protected Map<String, String> encodeAuthorizationHeader(final Map<String, String> value) {
      if (!value.containsKey(HttpRequestInfo.AUTH_HEADER))
         return value;

      final String authorizationHeader = value.get(HttpRequestInfo.AUTH_HEADER);
      final byte[] bytes = authorizationHeader.getBytes(Charset.forName("UTF-8"));
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", Base64.encodeBase64String(bytes));
      value.put(HttpRequestInfo.AUTH_HEADER, encodedAuthorizationHeader);

      return value;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {
      try {
         final Yaml yaml = new Yaml();
         final Object loadedYaml = yaml.load(io);

         if (!(loadedYaml instanceof ArrayList)) {
            throw new IOException(String.format("Loaded YAML data from %s must be an instance of ArrayList, otherwise something went wrong..", yamlConfigFilename));
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