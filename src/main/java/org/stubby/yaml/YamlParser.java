package org.stubby.yaml;

import org.apache.commons.codec.binary.Base64;
import org.stubby.cli.ANSITerminal;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.ReflectionUtils;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class YamlParser {

   private static final String NODE_REQUEST = "request";
   private static final String NODE_RESPONSE = "response";

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
      if (!responseFileFromFilesystem.isFile())
         throw new IOException(String.format("Could not load file from path: %s", filePath));

      return StringUtils.inputStreamToString(new FileInputStream(responseFileFromFilesystem));
   }

   @SuppressWarnings("unchecked")
   public List<StubHttpLifecycle> load(final Reader io) throws Exception {

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

      return propertyName.equalsIgnoreCase("file") ? loadResponseBodyFromFile(valueAsString) : valueAsString;
   }

   protected Map<String, String> encodeAuthorizationHeader(final Map<String, String> value) {
      if (!value.containsKey(HttpRequestInfo.AUTH_HEADER))
         return value;

      final String authorizationHeader = value.get(HttpRequestInfo.AUTH_HEADER);
      final byte[] bytes = authorizationHeader.getBytes(StringUtils.utf8Charset());
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", Base64.encodeBase64String(bytes));
      value.put(HttpRequestInfo.AUTH_HEADER, encodedAuthorizationHeader);

      return value;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {
      final Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new YamlParserResolver());
      final Object loadedYaml = yaml.load(io);

      if (loadedYaml instanceof ArrayList)
         return (ArrayList<?>) loadedYaml;

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