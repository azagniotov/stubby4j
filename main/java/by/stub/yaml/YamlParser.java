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
import by.stub.repackaged.org.apache.commons.codec.binary.Base64;
import by.stub.utils.FileUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class YamlParser {

   private final static Yaml SNAKE_YAML;

   static {
      SNAKE_YAML = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new YamlParserResolver());
   }

   private static final String YAML_NODE_REQUEST = "request";
   public static final String YAML_NODE_SEQUENCE = "sequence";
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

      return new InputStreamReader(new FileInputStream(yamlFile), StringUtils.charsetUTF8());
   }

   public List<StubHttpLifecycle> parseAndLoad() throws Exception {
      return parseAndLoad(buildYamlReaderFromFilename());
   }

   public List<StubHttpLifecycle> parseAndLoad(final String yamlPath) throws Exception {
      final Reader yamlReader = StringUtils.constructReader(yamlPath);

      return parseAndLoad(yamlReader);
   }

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

         mapRootYamlNodeToStub(parentStub, parentNode);

         final ArrayList<String> method = parentStub.getRequest().getMethod();
         final String url = parentStub.getRequest().getUrl();
         final String loadedMsg = String.format("Loaded: %s %s", method, url);
         ANSITerminal.loaded(loadedMsg);
      }

      return httpLifecycles;
   }

   @SuppressWarnings("unchecked")
   protected void mapRootYamlNodeToStub(final StubHttpLifecycle parentStub, final LinkedHashMap<String, LinkedHashMap> parentNode) throws Exception {
      for (final Map.Entry<String, LinkedHashMap> parent : parentNode.entrySet()) {

         if (parent.getValue() != null && parent.getValue() instanceof LinkedHashMap) {

            final LinkedHashMap<String, Object> httpSettings = (LinkedHashMap<String, Object>) parent.getValue();

            if (parent.getKey().equals(YAML_NODE_REQUEST)) {
               mapPairValueToRespectiveField(parentStub.getRequest(), httpSettings);
               continue;
            }

            mapPairValueToRespectiveField(parentStub.getResponse(), httpSettings);
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected void mapPairValueToRespectiveField(final Object targetStub, final LinkedHashMap<String, Object> httpProperties) throws Exception {

      for (final Map.Entry<String, Object> pair : httpProperties.entrySet()) {

         final Object rawPairValue = pair.getValue();
         final String pairKey = pair.getKey();
         final Object massagedPairValue;

         if (rawPairValue instanceof ArrayList && !pairKey.equals(YAML_NODE_SEQUENCE)) {
            massagedPairValue = rawPairValue;

         } else if (pairKey.equals(YAML_NODE_SEQUENCE)) {

            final List<StubResponse> sequence = new LinkedList<StubResponse>();

            final ArrayList<LinkedHashMap<String, Object>> rawSequence = (ArrayList<LinkedHashMap<String, Object>>) rawPairValue;
            for (final LinkedHashMap<String, Object> rawSequenceEntry : rawSequence) {
               final LinkedHashMap<String, Object> rawSequenceResponse = (LinkedHashMap<String, Object>) rawSequenceEntry.get("response");

               final StubResponse sequenceResponse = new StubResponse();

               for (final Map.Entry<String, Object> mapEntry : rawSequenceResponse.entrySet()) {
                  final String rawSequenceEntryKey = mapEntry.getKey();
                  final Object rawSequenceEntryValue = mapEntry.getValue();

                  ReflectionUtils.setPropertyValue(sequenceResponse, rawSequenceEntryKey, rawSequenceEntryValue);
               }

               sequence.add(sequenceResponse);
            }

            massagedPairValue = sequence;

         } else if (rawPairValue instanceof Map) {
            massagedPairValue = encodeAuthorizationHeader(rawPairValue);

         } else if (pairKey.toLowerCase().equals("method")) {
            massagedPairValue = new ArrayList<String>(1) {{
               add(pairValueToString(rawPairValue));
            }};

         } else if (pairKey.toLowerCase().equals("file")) {
            massagedPairValue = extractBytesFromFilecontent(rawPairValue);

         } else {
            massagedPairValue = pairValueToString(rawPairValue);
         }

         ReflectionUtils.setPropertyValue(targetStub, pairKey, massagedPairValue);
      }
   }

   private byte[] extractBytesFromFilecontent(final Object rawPairValue) throws IOException {

      final String relativeFilePath = pairValueToString(rawPairValue);
      final int dotLocation = relativeFilePath.lastIndexOf(".");
      final String extension = relativeFilePath.substring(dotLocation);

      if (FileUtils.ASCII_TYPES.contains(extension)) {
         return FileUtils.asciiFileToUtf8Bytes(relativeFilePath);
      }

      return FileUtils.binaryFileToBytes(relativeFilePath);
   }

   private String pairValueToString(final Object value) throws IOException {
      final String rawValue = StringUtils.isObjectSet(value) ? value.toString() : "";

      return rawValue.trim();
   }

   protected Map<String, String> encodeAuthorizationHeader(final Object value) {

      final Map<String, String> pairValue = (HashMap<String, String>) value;
      if (!pairValue.containsKey(StubRequest.AUTH_HEADER)) {
         return pairValue;
      }
      final String rawHeader = pairValue.get(StubRequest.AUTH_HEADER);
      final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
      final byte[] bytes = authorizationHeader.getBytes(StringUtils.charsetUTF8());
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", Base64.encodeBase64String(bytes));
      pairValue.put(StubRequest.AUTH_HEADER, encodedAuthorizationHeader);

      return pairValue;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {

      final Object loadedYaml = SNAKE_YAML.load(io);

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