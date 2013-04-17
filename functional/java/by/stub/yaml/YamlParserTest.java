package by.stub.yaml;

import by.stub.builder.yaml.YamlBuilder;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import com.google.api.client.http.HttpMethods;
import org.fest.assertions.data.MapEntry;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserTest {

   private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleHTTPMethods() throws Exception {

      final String expectedUrl = "/some/uri";
      final String expectedHeaderKey = "location";
      final String expectedHeaderValue = "/invoice/123";

      final String stubbedResponseStatus = "301";
      final String stubbedResponseBody = "Hello, this is a response body";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withMethodHead()
         .withUrl(expectedUrl)
         .newStubbedResponse()
         .withStatus(stubbedResponseStatus)
         .withHeaders(expectedHeaderKey, expectedHeaderValue)
         .withLiteralBody(stubbedResponseBody).build();


      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle stubbedHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest stubbedRequest = stubbedHttpLifecycle.getRequest();
      final StubResponse stubbedResponse = stubbedHttpLifecycle.getResponse();

      assertThat(stubbedRequest.getUrl()).isEqualTo(expectedUrl);
      assertThat(stubbedRequest.getMethod()).contains(HttpMethods.GET, HttpMethods.HEAD);
      assertThat(stubbedResponse.getStatus()).isEqualTo(stubbedResponseStatus);

      final MapEntry headerEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
      assertThat(stubbedResponse.getHeaders()).contains(headerEntry);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleQueryParams() throws Exception {

      final String expectedUrl = "/some/uri";
      final String expectedHeaderKey = "location";
      final String expectedHeaderValue = "/invoice/123";

      final String expectedParamOne = "paramOne";
      final String expectedParamOneValue = "one";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String expectedParamTwo = "paramTwo";
      final String expectedParamTwoValue = "two";
      final String fullQueryTwo = String.format("%s=%s", expectedParamTwo, expectedParamTwoValue);


      final String stubbedResponseStatus = "301";
      final String stubbedResponseBody = "Hello, this is a response body";


      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(expectedUrl)
         .withQuery(expectedParamOne, expectedParamOneValue)
         .withQuery(expectedParamTwo, expectedParamTwoValue)
         .newStubbedResponse()
         .withStatus(stubbedResponseStatus)
         .withHeaders(expectedHeaderKey, expectedHeaderValue)
         .withLiteralBody(stubbedResponseBody).build();


      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle stubbedHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest stubbedRequest = stubbedHttpLifecycle.getRequest();
      final StubResponse stubbedResponse = stubbedHttpLifecycle.getResponse();

      assertThat(stubbedRequest.getUrl()).contains(expectedUrl);
      assertThat(stubbedRequest.getUrl()).contains(fullQueryOne);
      assertThat(stubbedRequest.getUrl()).contains(fullQueryTwo);
      assertThat(stubbedRequest.getMethod()).contains(HttpMethods.GET);
      assertThat(stubbedResponse.getStatus()).isEqualTo(stubbedResponseStatus);

      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);
      final MapEntry queryEntryTwo = MapEntry.entry(expectedParamTwo, expectedParamTwoValue);
      assertThat(stubbedRequest.getQuery()).contains(queryEntryOne, queryEntryTwo);

      final MapEntry headerEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
      assertThat(stubbedResponse.getHeaders()).contains(headerEntry);
   }


   @Test
   public void shouldUnmarshallYamlIntoHugeObjectTree_WhenYAMLValid() throws Exception {

      final String baseRequestUrl = "/some/uri";

      final String expectedHeaderKey = "location";
      final String expectedHeaderValue = "/invoice/123";

      final String expectedParamOne = "paramOne";
      final String expectedParamTwo = "paramTwo";

      final String stubbedResponseBody = "Hello, this is a response body";
      final String stubbedResponseStatus = "301";

      final int NUMBER_OF_HTTPCYCLES = 1000;
      final StringBuilder BUILDER = new StringBuilder();

      for (int idx = 1; idx <= NUMBER_OF_HTTPCYCLES; idx++) {
         String expectedUrl = String.format("%s/%s", baseRequestUrl, idx);

         String yaml = YAML_BUILDER.newStubbedRequest()
            .withMethodGet()
            .withUrl(expectedUrl)
            .withQuery(expectedParamOne, String.valueOf(idx))
            .withQuery(expectedParamTwo, String.valueOf(idx))
            .newStubbedResponse()
            .withStatus(stubbedResponseStatus)
            .withHeaders(expectedHeaderKey, expectedHeaderValue)
            .withLiteralBody(stubbedResponseBody).build();

         BUILDER.append(yaml).append("\n\n");
      }

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(BUILDER.toString());
      assertThat(loadedHttpCycles.size()).isEqualTo(NUMBER_OF_HTTPCYCLES);

      final StubHttpLifecycle stubbedHttpLifecycle = loadedHttpCycles.get(989);
      final StubRequest stubbedRequest = stubbedHttpLifecycle.getRequest();
      final StubResponse stubbedResponse = stubbedHttpLifecycle.getResponse();

      assertThat(stubbedRequest.getUrl()).contains(String.format("%s/%s", baseRequestUrl, 990));
      assertThat(stubbedRequest.getUrl()).contains(String.format("%s=%s", expectedParamOne, 990));
      assertThat(stubbedRequest.getUrl()).contains(String.format("%s=%s", expectedParamTwo, 990));

      final MapEntry headerEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
      assertThat(stubbedResponse.getHeaders()).contains(headerEntry);
   }


   private List<StubHttpLifecycle> loadYamlToDataStore(final String yaml) throws Exception {
      final Reader reader = new StringReader(yaml);
      final YamlParser yamlParser = new YamlParser("");

      return yamlParser.parseAndLoad(reader);
   }
}