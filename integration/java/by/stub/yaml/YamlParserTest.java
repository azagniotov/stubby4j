package by.stub.yaml;

import by.stub.builder.yaml.YamlBuilder;
import by.stub.utils.FileUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import com.google.api.client.http.HttpMethods;
import org.fest.assertions.data.MapEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenEmptyYAMLGiven() throws Exception {

      expectedException.expect(IOException.class);
      expectedException.expectMessage("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");

      unmarshall("");
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithNoProperties() throws Exception {

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

      assertThat(actualHttpLifecycle.getAllResponses()).hasSize(1);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithNoSequenceResponses() throws Exception {

      final String expectedStatus = "301";
      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualHttpLifecycle.getAllResponses()).hasSize(1);
      assertThat(actualResponse.getStatus()).isEqualTo(expectedStatus);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithOneSequenceResponse() throws Exception {

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/json";
      final String sequenceResponseStatus = "200";
      final String sequenceResponseBody = "OK";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseLiteralBody(sequenceResponseBody)
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      final MapEntry sequenceHeaderEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);

      assertThat(actualResponse).isInstanceOf(StubResponse.class);
      assertThat(actualResponse.getHeaders()).contains(sequenceHeaderEntry);
      assertThat(actualResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(actualResponse.getBody()).isEqualTo(sequenceResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_AndReturnResponsesInSequence_WithManySequenceResponse() throws Exception {

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/xml";
      final String sequenceResponseStatus = "500";
      final String sequenceResponseBody = "OMFG";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseFoldedBody(sequenceResponseBody)
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

      final StubResponse irrelevantSequenceResponse = actualHttpLifecycle.getResponse();
      final StubResponse actualSequenceResponse = actualHttpLifecycle.getResponse();

      final MapEntry sequenceHeaderEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);

      assertThat(actualSequenceResponse).isInstanceOf(StubResponse.class);
      assertThat(actualSequenceResponse.getHeaders()).contains(sequenceHeaderEntry);
      assertThat(actualSequenceResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(actualSequenceResponse.getBody()).isEqualTo(sequenceResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithUrlAsRegex() throws Exception {

      final String url = "^/[a-z]{3}/[0-9]+/?$";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("301").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getUrl()).isEqualTo(url);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleHTTPMethods() throws Exception {

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withMethodHead()
         .newStubbedResponse()
         .withStatus("301").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getMethod()).contains(HttpMethods.GET, HttpMethods.HEAD);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithDefaultHTTPResponseStatus() throws Exception {

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .newStubbedResponse()
         .withLiteralBody("hello").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getStatus()).isEqualTo(String.valueOf(200));
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFoldedPost() throws Exception {

      final String stubbedRequestPost = "{\"message\", \"Hello, this is a request post\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withFoldedPost(stubbedRequestPost)
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFileFailedToLoadAndPostSet() throws Exception {

      final String stubbedRequestFile = "../../very.big.soap.request.xml";

      final String expectedPost = "{\"message\", \"Hello, this is HTTP request post\"}";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withFile(stubbedRequestFile)
         .withFoldedPost(expectedPost)
         .newStubbedResponse()
         .withLiteralBody("OK")
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);

      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getFile()).isEqualTo(new byte[]{});
      assertThat(actualRequest.getPostBody()).isEqualTo(expectedPost);
   }

   @Test
   public void shouldCaptureConsoleErrorOutput_WhenYAMLValid_WithFileFailedToLoadAndPostSet() throws Exception {

      final String stubbedRequestFile = "../../very.big.soap.request.xml";

      final String expectedPost = "{\"message\", \"Hello, this is HTTP request post\"}";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withFile(stubbedRequestFile)
         .withFoldedPost(expectedPost)
         .newStubbedResponse()
         .withLiteralBody("OK")
         .withStatus("201").build();

      final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
      final boolean NO_AUTO_FLUSH = false;
      System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

      unmarshall(yaml);

      System.setOut(System.out);

      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains("Could not load file from path: ../../very.big.soap.request.xml");
      assertThat(actualConsoleOutput).contains(YamlParser.FAILED_TO_LOAD_FILE_ERR);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFileFailedToLoadAndBodySet() throws Exception {

      final String stubbedResponseFile = "../../very.big.soap.response.xml";

      final String expectedBody = "{\"message\", \"Hello, this is HTTP response body\"}";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withFoldedBody(expectedBody)
         .withFile(stubbedResponseFile)
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);

      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getActualStubbedResponse();

      assertThat(actualResponse.getFile()).isEqualTo(new byte[]{});
      assertThat(StringUtils.newStringUtf8(actualResponse.getResponseBody())).isEqualTo(expectedBody);
   }

   @Test
   public void shouldCaptureConsoleErrorOutput_WhenYAMLValid_WithFileFailedToLoadAndBodySet() throws Exception {

      final String stubbedResponseFile = "../../very.big.soap.response.xml";

      final String expectedBody = "{\"message\", \"Hello, this is HTTP response body\"}";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withFoldedBody(expectedBody)
         .withFile(stubbedResponseFile)
         .withStatus("201").build();

      final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
      final boolean NO_AUTO_FLUSH = false;
      System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

      unmarshall(yaml);

      System.setOut(System.out);

      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains("Could not load file from path: ../../very.big.soap.response.xml");
      assertThat(actualConsoleOutput).contains(YamlParser.FAILED_TO_LOAD_FILE_ERR);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithLiteralPost() throws Exception {

      final String stubbedRequestPost = "Hello, this is a request post";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withLiteralPost(stubbedRequestPost)
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
   }

   @Test
   public void shouldFailUnmarshallYamlIntoObjectTree_WithJsonAsLiteralPost() throws Exception {

      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Can not set java.lang.String field by.stub.yaml.StubRequestBuilder.post to java.util.LinkedHashMap");

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withLiteralPost("{\"message\", \"Hello, this is a request body\"}")
         .newStubbedResponse()
         .withStatus("201").build();

      unmarshall(yaml);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFoldedBody() throws Exception {

      final String stubbedResponseBody = "{\"message\", \"Hello, this is a response body\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withFoldedBody(stubbedResponseBody).build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
   }

   @Test
   public void shouldFailUnmarshallYamlIntoObjectTree_WithJsonAsLiteralBody() throws Exception {

      expectedException.expect(IllegalArgumentException.class);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withLiteralBody("{\"message\", \"Hello, this is a response body\"}").build();

      unmarshall(yaml);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithLiteralBody() throws Exception {

      final String stubbedResponseBody = "This is a sentence";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withLiteralBody(stubbedResponseBody).build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleQueryParams() throws Exception {

      final String expectedParamOne = "paramOne";
      final String expectedParamOneValue = "one";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String expectedParamTwo = "paramTwo";
      final String expectedParamTwoValue = "two";
      final String fullQueryTwo = String.format("%s=%s", expectedParamTwo, expectedParamTwoValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, expectedParamOneValue)
         .withQuery(expectedParamTwo, expectedParamTwoValue)
         .newStubbedResponse()
         .withStatus("500").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);
      final MapEntry queryEntryTwo = MapEntry.entry(expectedParamTwo, expectedParamTwoValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getUrl()).contains(fullQueryTwo);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne, queryEntryTwo);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithAuthorizationHeader() throws Exception {

      final String authorization = "bob:secret";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withHeaderAuthorization(authorization)
         .newStubbedResponse()
         .withStatus("301").build();


      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorization));
      final MapEntry headerOneEntry = MapEntry.entry("authorization", encodedAuthorizationHeader);

      assertThat(actualRequest.getHeaders()).contains(headerOneEntry);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithEmptyAuthorizationHeader() throws Exception {

      final String authorization = "";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withHeaderAuthorization("")
         .newStubbedResponse()
         .withStatus("301").build();


      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorization));
      final MapEntry headerOneEntry = MapEntry.entry("authorization", encodedAuthorizationHeader);

      assertThat(actualRequest.getHeaders()).contains(headerOneEntry);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleHeaders() throws Exception {

      final String location = "/invoice/123";
      final String contentType = "application-json";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withHeaderContentType(contentType)
         .withHeaderLocation(location).build();


      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();
      final MapEntry headerOneEntry = MapEntry.entry("location", location);
      final MapEntry headerTwoEntry = MapEntry.entry("content-type", contentType);

      assertThat(actualResponse.getHeaders()).contains(headerOneEntry);
      assertThat(actualResponse.getHeaders()).contains(headerTwoEntry);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WitQueryParamIsArrayHavingDoubleQuotes() throws Exception {

      final String expectedParamOne = "fruits";
      final String expectedParamOneValue = "[\"apple\",\"orange\",\"banana\"]";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, String.format("'%s'", expectedParamOneValue))
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WitQueryParamIsArrayHavingSingleQuotes() throws Exception {

      final String expectedParamOne = "fruits";
      final String expectedParamOneValue = "['apple','orange','banana']";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, String.format("\"%s\"", expectedParamOneValue))
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne);
   }

   @Test
   public void shouldThrowExceptionWhenUrlRegexPatternCannotBeCompiled() throws Exception {

      expectedException.expect(PatternSyntaxException.class);
      expectedException.expectMessage("Unclosed character class near index 30");

      final String url = "/some/uri/with?param=[one,two\\]";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("201").build();

      unmarshall(yaml);
   }


   private List<StubHttpLifecycle> unmarshall(final String yaml) throws Exception {
      return new YamlParser().parse(".", FileUtils.constructReader(yaml));
   }
}