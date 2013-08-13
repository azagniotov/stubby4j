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
import by.stub.utils.ConsoleUtils;
import by.stub.utils.FileUtils;
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
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class YamlParser {

   public static final String FAILED_TO_LOAD_FILE_ERR = "Failed to load response content using relative path specified in 'file'. Check that response content exists in relative path specified in 'file'";
   private String dataConfigHomeDirectory;
   private final static Yaml SNAKE_YAML;

   static {

      final class YamlParserResolver extends Resolver {

         YamlParserResolver() {
            super();
         }

         @Override
         protected void addImplicitResolvers() {
            // no implicit resolvers - resolve everything to String
         }
      }

      SNAKE_YAML = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new YamlParserResolver());
   }

   private static final String YAML_NODE_REQUEST = "request";
   private static final String YAML_NODE_METHOD = "method";
   private static final String YAML_NODE_FILE = "file";

   public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final Reader yamlReader) throws Exception {

      final Object loadedYaml = SNAKE_YAML.load(yamlReader);
      if (!(loadedYaml instanceof List)) {
         throw new IOException("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");
      }

      this.dataConfigHomeDirectory = dataConfigHomeDirectory;
      final List<?> loadedYamlData = (List) loadedYaml;

      final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();
      for (final Object rawParentNode : loadedYamlData) {

         final Map<String, Object> parentNodePropertiesMap = (Map<String, Object>) rawParentNode;
         httpLifecycles.add(unmarshallYamlNodeToHttpLifeCycle(parentNodePropertiesMap));
      }

      return httpLifecycles;
   }


   private StubHttpLifecycle unmarshallYamlNodeToHttpLifeCycle(final Map<String, Object> parentNodesMap) throws Exception {

      final StubHttpLifecycle httpLifecycle = new StubHttpLifecycle();

      for (final Map.Entry<String, Object> parentNode : parentNodesMap.entrySet()) {

         final Object parentNodeValue = parentNode.getValue();

         if (parentNodeValue instanceof Map) {
            handleMapNode(httpLifecycle, parentNode);

         } else if (parentNodeValue instanceof List) {
            handleListNode(httpLifecycle, parentNode);
         }
      }

      httpLifecycle.setMarshalledYaml(marshallNodeMapToYamlSnippet(parentNodesMap));

      return httpLifecycle;
   }

   private void handleMapNode(final StubHttpLifecycle stubHttpLifecycle, final Map.Entry<String, Object> parentNode) throws Exception {

      final Map<String, Object> yamlProperties = (Map<String, Object>) parentNode.getValue();

      if (parentNode.getKey().equals(YAML_NODE_REQUEST)) {
         final StubRequest targetStub = unmarshallYamlMapToTargetStub(yamlProperties, StubRequestBuilder.class);
         stubHttpLifecycle.setRequest(targetStub);

         ConsoleUtils.logUnmarshalledStubRequest(targetStub.getMethod(), targetStub.getUrl());

      } else {
         final StubResponse targetStub = unmarshallYamlMapToTargetStub(yamlProperties, StubResponseBuilder.class);
         stubHttpLifecycle.setResponse(targetStub);
      }
   }


   private <T, B extends StubBuilder<T>> T unmarshallYamlMapToTargetStub(final Map<String, Object> yamlProperties, final Class<B> stubBuilderClass) throws Exception {

      final B stubBuilder = stubBuilderClass.newInstance();
      for (final Map.Entry<String, Object> pair : yamlProperties.entrySet()) {

         final Object rawFieldName = pair.getValue();
         final String fieldName = pair.getKey();
         final Object massagedFieldValue;

         if (rawFieldName instanceof List) {
            massagedFieldValue = rawFieldName;

         } else if (rawFieldName instanceof Map) {
            massagedFieldValue = encodeAuthorizationHeader(rawFieldName);

         } else if (fieldName.toLowerCase().equals(YAML_NODE_METHOD)) {

            final ArrayList<String> methods = new ArrayList<String>(1);
            methods.add(StringUtils.objectToString(rawFieldName));
            massagedFieldValue = methods;

         } else if (isPairKeyEqualsToYamlNodeFile(fieldName)) {
            massagedFieldValue = loadFileContentFromFileUrl(rawFieldName);
         } else {
            massagedFieldValue = StringUtils.objectToString(rawFieldName);
         }
         stubBuilder.store(fieldName, massagedFieldValue);
      }

      return stubBuilder.build();
   }

   private void handleListNode(final StubHttpLifecycle stubHttpLifecycle, final Map.Entry<String, Object> parentNode) throws Exception {

      final List yamlProperties = (List) parentNode.getValue();
      final List<StubResponse> populatedResponseStub = unmarshallYamlListToTargetStub(yamlProperties, StubResponseBuilder.class);
      stubHttpLifecycle.setResponse(populatedResponseStub);
   }

   private  <T, B extends StubBuilder<T>> List<T> unmarshallYamlListToTargetStub(final List yamlProperties, final Class<B> stubBuilderClass) throws Exception {

      final B stubBuilder = stubBuilderClass.newInstance();
      final List<T> targetStubList = new LinkedList<T>();
      for (final Object arrayListEntry : yamlProperties) {

         final Map<String, Object> rawSequenceEntry = (Map<String, Object>) arrayListEntry;

         for (final Map.Entry<String, Object> mapEntry : rawSequenceEntry.entrySet()) {
            final String rawFieldName = mapEntry.getKey();
            Object rawFieldValue = mapEntry.getValue();
            if (isPairKeyEqualsToYamlNodeFile(rawFieldName)) {
               rawFieldValue = loadFileContentFromFileUrl(rawFieldValue);
            }
            stubBuilder.store(rawFieldName, rawFieldValue);
         }

         targetStubList.add(stubBuilder.build());
      }

      return targetStubList;
   }

   private boolean isPairKeyEqualsToYamlNodeFile(final String pairKey) {
      return pairKey.toLowerCase().equals(YAML_NODE_FILE);
   }

   private Object loadFileContentFromFileUrl(final Object rawPairValue) throws IOException {
      final String filePath = StringUtils.objectToString(rawPairValue);
      try {
         return FileUtils.uriToFile(dataConfigHomeDirectory, filePath);
      } catch (final IOException ex) {
         ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
      }
      File temp = File.createTempFile("tmp", ".tmp");
      temp.deleteOnExit();

      return temp;
   }

   private String marshallNodeMapToYamlSnippet(final Map<String, Object> parentNodesMap) {
      final ArrayList<Map<String, Object>> placeholder = new ArrayList<Map<String, Object>>() {{
         add(parentNodesMap);
      }};

      return SNAKE_YAML.dumpAs(placeholder, null, DumperOptions.FlowStyle.BLOCK);
   }

   private Map<String, String> encodeAuthorizationHeader(final Object value) {

      final Map<String, String> pairValue = (Map<String, String>) value;
      if (!pairValue.containsKey(StubRequest.AUTH_HEADER)) {
         return pairValue;
      }
      final String rawHeader = pairValue.get(StubRequest.AUTH_HEADER);
      final String authorizationHeader = StringUtils.isSet(rawHeader) ? rawHeader.trim() : rawHeader;
      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorizationHeader));
      pairValue.put(StubRequest.AUTH_HEADER, encodedAuthorizationHeader);

      return pairValue;
   }
}