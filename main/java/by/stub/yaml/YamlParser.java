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

import by.stub.annotations.CoberturaIgnore;
import by.stub.cli.ANSITerminal;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubCallback;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class YamlParser {

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

   public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final String yaml) throws Exception {
      return parse(dataConfigHomeDirectory, FileUtils.constructReader(yaml));
   }

   @CoberturaIgnore
   public List<StubHttpLifecycle> parse(final String dataConfigHomeDirectory, final File yamlFile) throws Exception {
      return parse(dataConfigHomeDirectory, FileUtils.constructReader(yamlFile));
   }

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
         final StubHttpLifecycle stubHttpLifecycle = unmarshallYamlNodeToHttpLifeCycle(parentNodePropertiesMap);
         httpLifecycles.add(stubHttpLifecycle);
         stubHttpLifecycle.setResourceId(httpLifecycles.size() - 1);
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

      httpLifecycle.setHttpLifeCycleAsYaml(marshallNodeMapToYaml(parentNodesMap));

      final Map<String, Object> requestMap = new HashMap<String, Object>();
      requestMap.put(YamlProperties.REQUEST, parentNodesMap.get(YamlProperties.REQUEST));
      httpLifecycle.setRequestAsYaml(marshallNodeToYaml(requestMap));

      final Map<String, Object> responseMap = new HashMap<String, Object>();
      responseMap.put(YamlProperties.RESPONSE, parentNodesMap.get(YamlProperties.RESPONSE));
      httpLifecycle.setResponseAsYaml(marshallNodeToYaml(responseMap));           

      return httpLifecycle;
   }

   private void handleMapNode(final StubHttpLifecycle stubHttpLifecycle, final Map.Entry<String, Object> parentNode) throws Exception {

      final Map<String, Object> yamlProperties = (Map<String, Object>) parentNode.getValue();

      if (parentNode.getKey().equals(YamlProperties.REQUEST)) {
         final StubRequest targetStub = unmarshallYamlMapToTargetStub(yamlProperties, new StubRequestBuilder());
         stubHttpLifecycle.setRequest(targetStub);

         ConsoleUtils.logUnmarshalledStubRequest(targetStub.getMethod(), targetStub.getUrl());

      } else {
         final StubResponse targetStub = unmarshallYamlMapToTargetStub(yamlProperties, new StubResponseBuilder());
         stubHttpLifecycle.setResponse(targetStub);
      }
   }


   private <T, B extends StubBuilder<T>> T unmarshallYamlMapToTargetStub(final Map<String, Object> yamlProperties, final B stubBuilder) throws Exception {

      for (final Map.Entry<String, Object> pair : yamlProperties.entrySet()) {

         final Object rawFieldName = pair.getValue();
         final String fieldName = pair.getKey();
         final Object massagedFieldValue;

         if (rawFieldName instanceof List) {
            massagedFieldValue = rawFieldName;

         } else if (rawFieldName instanceof Map) {            
            if (fieldName.toLowerCase().equals(YamlProperties.CALLBACK)) {
            	Map<String, Object> yamlProperties2 = (Map<String, Object>) rawFieldName;
            	 final StubCallback callbackStub = unmarshallYamlMapToTargetStub(yamlProperties2, new StubCallbackBuilder());
                massagedFieldValue = callbackStub;
           }else {        	   
        	   ConcurrentHashMap<String, String> headers = new ConcurrentHashMap((Map)rawFieldName);               
        	   massagedFieldValue = encodeAuthorizationHeader(headers);
           }

         } else if (fieldName.toLowerCase().equals(YamlProperties.METHOD)) {

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
      final List<StubResponse> populatedResponseStub = unmarshallYamlListToTargetStub(yamlProperties, new StubResponseBuilder());
      stubHttpLifecycle.setResponse(populatedResponseStub);
   }

   private <T, B extends StubBuilder<T>> List<T> unmarshallYamlListToTargetStub(final List yamlProperties, final B stubBuilder) throws Exception {

      final List<T> targetStubList = new LinkedList<T>();
      for (final Object arrayListEntry : yamlProperties) {

         final Map<String, Object> rawSequenceEntry = (Map<String, Object>) arrayListEntry;

         for (final Map.Entry<String, Object> mapEntry : rawSequenceEntry.entrySet()) {
            final String rawFieldName = mapEntry.getKey();
            Object rawFieldValue = mapEntry.getValue();
            if (isPairKeyEqualsToYamlNodeFile(rawFieldName)) {
               rawFieldValue = loadFileContentFromFileUrl(rawFieldValue);
            }            
            if (rawFieldName.toLowerCase().equals(YamlProperties.CALLBACK)) {
            	 Map<String, Object> yamlProperties2 = (Map<String, Object>) rawFieldValue;
            	 final StubCallback callbackStub = unmarshallYamlMapToTargetStub(yamlProperties2, new StubCallbackBuilder());
            	 rawFieldValue = callbackStub;
            }else if (rawFieldName.toLowerCase().equals(YamlProperties.HEADERS)){
            	rawFieldValue = new ConcurrentHashMap((Map)rawFieldValue);
            }
            
            stubBuilder.store(rawFieldName, rawFieldValue);
         }

         targetStubList.add(stubBuilder.build());
      }

      return targetStubList;
   }

   private boolean isPairKeyEqualsToYamlNodeFile(final String pairKey) {
      return pairKey.toLowerCase().equals(YamlProperties.FILE);
   }

   private Object loadFileContentFromFileUrl(final Object rawPairValue) throws IOException {
      final String filePath = StringUtils.objectToString(rawPairValue);
      try {
         return FileUtils.uriToFile(dataConfigHomeDirectory, filePath);
      } catch (final IOException ex) {
         ANSITerminal.error(ex.getMessage() + " " + FAILED_TO_LOAD_FILE_ERR);
      }

      return null;
   }

   private String marshallNodeMapToYaml(final Map<String, Object> parentNodesMap) {
      final ArrayList<Map<String, Object>> placeholder = new ArrayList<Map<String, Object>>() {{
         add(parentNodesMap);
      }};

      return SNAKE_YAML.dumpAs(placeholder, null, DumperOptions.FlowStyle.BLOCK);
   }

   private String marshallNodeToYaml(final Object yamlNode) {
      return SNAKE_YAML.dumpAs(yamlNode, null, DumperOptions.FlowStyle.BLOCK);
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