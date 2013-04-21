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

import java.io.IOException;
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


   public List<StubHttpLifecycle> parse(final Reader yamlReader) throws Exception {

      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();
      final List<?> loadedYamlData = loadYamlData(yamlReader);

      for (final Object rawParentNode : loadedYamlData) {

         final LinkedHashMap<String, Object> parentNode = (LinkedHashMap<String, Object>) rawParentNode;

         final StubHttpLifecycle parentStub = mapRootYamlNodeToStub(parentNode);
         httpLifecycles.add(parentStub);

         final ArrayList<String> method = parentStub.getRequest().getMethod();
         final String url = parentStub.getRequest().getUrl();
         final String loadedMsg = String.format("Loaded: %s %s", method, url);
         ANSITerminal.loaded(loadedMsg);
      }

      return httpLifecycles;
   }

   @SuppressWarnings("unchecked")
   protected StubHttpLifecycle mapRootYamlNodeToStub(final LinkedHashMap<String, Object> parentNode) throws Exception {

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();

      for (final Map.Entry<String, Object> parent : parentNode.entrySet()) {

         if (parent.getValue() instanceof LinkedHashMap) {

            final Object targetStub = parent.getKey().equals(YAML_NODE_REQUEST) ? new StubRequest() : new StubResponse();
            final Object populatedTargetStub = mapPairValueToRespectiveField(targetStub, (LinkedHashMap<String, Object>) parent.getValue());

            if (parent.getKey().equals(YAML_NODE_REQUEST)) {
               stubHttpLifecycle.setRequest((StubRequest) populatedTargetStub);
            } else {
               stubHttpLifecycle.setResponse(populatedTargetStub);
            }

         } else if (parent.getValue() instanceof ArrayList) {
            final Object populatedResponseStub = mapPairValueToRespectiveField((ArrayList) parent.getValue());

            stubHttpLifecycle.setResponse(populatedResponseStub);
         }
      }

      return stubHttpLifecycle;
   }

   @SuppressWarnings("unchecked")
   private Object mapPairValueToRespectiveField(final ArrayList yamlProperties) throws Exception {

      final List<StubResponse> responses = new LinkedList<StubResponse>();

      for (final Object arrayListEntry : yamlProperties) {

         final LinkedHashMap<String, Object> rawSequenceEntry = (LinkedHashMap<String, Object>) arrayListEntry;

         final StubResponse sequenceResponse = new StubResponse();

         for (final Map.Entry<String, Object> mapEntry : rawSequenceEntry.entrySet()) {
            final String rawSequenceEntryKey = mapEntry.getKey();
            final Object rawSequenceEntryValue = mapEntry.getValue();

            ReflectionUtils.setPropertyValue(sequenceResponse, rawSequenceEntryKey, rawSequenceEntryValue);
         }

         responses.add(sequenceResponse);
      }

      return responses;
   }

   @SuppressWarnings("unchecked")
   protected Object mapPairValueToRespectiveField(final Object targetStub, final LinkedHashMap<String, Object> yamlProperties) throws Exception {

      for (final Map.Entry<String, Object> pair : yamlProperties.entrySet()) {

         final Object rawPairValue = pair.getValue();
         final String pairKey = pair.getKey();
         final Object massagedPairValue;

         if (rawPairValue instanceof ArrayList) {
            massagedPairValue = rawPairValue;

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

      return targetStub;
   }

   private byte[] extractBytesFromFilecontent(final Object rawPairValue) throws IOException {

      final String relativeFilePath = pairValueToString(rawPairValue);
      final String extension = StringUtils.extractFilenameExtension(relativeFilePath);

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
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorizationHeader));
      pairValue.put(StubRequest.AUTH_HEADER, encodedAuthorizationHeader);

      return pairValue;
   }

   protected List<?> loadYamlData(final Reader io) throws IOException {

      final Object loadedYaml = SNAKE_YAML.load(io);

      if (loadedYaml instanceof ArrayList) {
         return (ArrayList<?>) loadedYaml;
      }

      throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
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