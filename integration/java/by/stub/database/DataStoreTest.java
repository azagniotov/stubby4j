package by.stub.database;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.builder.yaml.YamlBuilder;
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.FileUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import com.google.api.client.http.HttpMethods;
import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */


@SuppressWarnings("serial")
public class DataStoreTest {

   private static DataStore dataStore;
   private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();
   private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

   @BeforeClass
   public static void beforeClass() throws Exception {
      dataStore = new DataStore(new File("."), new LinkedList<StubHttpLifecycle>());
   }

   @Before
   public void beforeEach() throws Exception {
      dataStore.resetStubHttpLifecycles(new LinkedList<StubHttpLifecycle>());
   }


   @Test
   public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasOneResponse() throws Exception {

      final String url = "/some/redirecting/uri";
      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/json";
      final String sequenceResponseStatus = "200";
      final String sequenceResponseBody = "OK";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseLiteralBody(sequenceResponseBody)
         .build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);

      final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
      assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
   }

   @Test
   public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasManyResponses() throws Exception {

      final String url = "/some/uri";

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/xml";
      final String sequenceResponseStatus = "500";
      final String sequenceResponseBody = "OMFG";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseFoldedBody(sequenceResponseBody)
         .build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse irrelevantFirstSequenceResponse = dataStore.findStubResponseFor(assertingRequest);
      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);

      final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
      assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
   }

   @Test
   public void shouldReturnFirstSequenceResponse_WhenAllSequenceResponsesHaveBeenConsumedInTheList() throws Exception {

      final String url = "/some/uri";

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/xml";
      final String sequenceResponseStatus = "200";
      final String sequenceResponseBody = "OK";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseLiteralBody(sequenceResponseBody)
         .withLineBreak()
         .withSequenceResponseStatus("500")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseFoldedBody("OMFG")
         .build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse irrelevantFirstSequenceResponse = dataStore.findStubResponseFor(assertingRequest);
      final StubResponse irrelevantLastSequenceResponse = dataStore.findStubResponseFor(assertingRequest);
      final StubResponse firstSequenceResponseRestarted = dataStore.findStubResponseFor(assertingRequest);

      assertThat(firstSequenceResponseRestarted).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(firstSequenceResponseRestarted.getStubResponseType());

      assertThat(firstSequenceResponseRestarted.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(firstSequenceResponseRestarted.getBody()).isEqualTo(sequenceResponseBody);

      final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
      assertThat(firstSequenceResponseRestarted.getHeaders()).contains(mapEntry);
   }

   @Test
   public void shouldReturnMatchingRedirectResponse_WhenLocationHeaderSet() throws Exception {

      final String url = "/some/redirecting/uri";
      final String expectedStatus = "301";
      final String expectedBody = "oink";
      final String expectedHeaderKey = "location";
      final String expectedHeaderValue = "/invoice/123";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withHeaders(expectedHeaderKey, expectedHeaderValue)
         .withLiteralBody(expectedBody).build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(RedirectStubResponse.class);
      assertThat(StubResponseTypes.REDIRECT).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);

      final MapEntry mapEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
      assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenValidGetRequestMade() throws Exception {

      final String url = "/invoice/123";
      final String expectedStatus = "200";
      final String expectedBody = "This is a response for 123";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withLiteralBody(expectedBody).build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationHeaderSet() throws Exception {

      final String url = "/invoice/555";

      final String expectedStatus = "200";
      final String expectedBody = "This is a response for 555";
      final String expectedHeaderKey = "authorization";
      final String expectedHeaderValue = "'bob:secret'";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withHeaders(expectedHeaderKey, expectedHeaderValue)
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withLiteralBody(expectedBody).build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders(StubRequest.AUTH_HEADER, "Basic Ym9iOnNlY3JldA==").build();  //bob:secret

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
   }


   @Test
   public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderMissing() throws Exception {

      final String url = "/invoice/555";
      final String expectedStatus = "200";
      final String expectedBody = "This is a response for 555";
      final String expectedHeaderKey = "authorization";
      final String expectedHeaderValue = "'bob:secret'";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withHeaders(expectedHeaderKey, expectedHeaderValue)
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withLiteralBody(expectedBody).build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
      assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("401");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderSetWithBadCredentials() throws Exception {

      final String url = "/invoice/555";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withHeaders("authorization", "'bob:secret'")
         .newStubbedResponse()
         .withStatus("200")
         .withLiteralBody("This is a response for 555").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders(StubRequest.AUTH_HEADER, "Basic BadCredentials").build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
      assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("401");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenAssertingRequestWasNotMatched() throws Exception {

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/invoice/125")
         .newStubbedResponse()
         .withStatus("200")
         .withLiteralBody("This is a response for 125").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl("/invoice/300")
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("404");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenValidPostRequestMade() throws Exception {

      final String url = "/invoice/567";
      final String postData = "This is a post data";
      final String expectedStatus = "503";
      final String expectedBody = "This is a response for 567";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodPost()
         .withLiteralPost("This is a post data")
         .withUrl(url)
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withLiteralBody(expectedBody).build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodPost()
            .withPost(postData).build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenPostBodyMissing() throws Exception {

      final String url = "/invoice/567";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodPost()
         .withLiteralPost("This is a post data")
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("503")
         .withLiteralBody("This is a response for 567").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodPost().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("404");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenHittingCorrectUrlButWrongMethod() throws Exception {

      final String url = "/invoice/567";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("503")
         .withLiteralBody("This is a response for 567").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodPost().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("404");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenPostRequestMadeToIncorrectUrl() throws Exception {

      final String url = "/invoice/non-existent-url";
      final String postData = "This is a post data";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodPost()
         .withLiteralPost("This is a post data")
         .withUrl("/invoice/567")
         .newStubbedResponse()
         .withStatus("503")
         .withLiteralBody("This is a response for 567").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodPost()
            .withPost(postData).build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("404");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenQueryParamIsArray() throws Exception {

      final String url = "/entity.find";
      final String expectedStatus = "200";
      final String expectedBody = "{\"status\": \"hello world\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withQuery("type_name", "user")
         .withQuery("client_id", "id")
         .withQuery("client_secret", "secret")
         .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withFoldedBody(expectedBody)
         .withHeaders("content-type", "application/json").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet()
            .withQuery("type_name", "user")
            .withQuery("client_id", "id")
            .withQuery("client_secret", "secret")
            .withQuery("attributes", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]")
            .build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
   }


   @Test
   public void shouldFindHttpCycleListUnchanged_WhenMatchIsdone() throws Exception {

      final String url = "/entity.find";
      final String expectedStatus = "200";
      final String expectedBody = "{\"status\": \"hello world\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withHeaders("content-type", "application/json")
         .withQuery("type_name", "user")
         .withQuery("client_id", "id")
         .withQuery("client_secret", "secret")
         .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withFoldedBody(expectedBody)
         .withHeaders("content-type", "application/json").build();

      final List<StubHttpLifecycle> parsedHttpLifecycles = parseYamlToStubHttpLifecycles(yaml);
      dataStore.resetStubHttpLifecycles(parsedHttpLifecycles);

      final List<StubHttpLifecycle> expectedHttpLifecycles = new LinkedList<StubHttpLifecycle>(parsedHttpLifecycles);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders("content-type", "application/json")
            .withQuery("type_name", "user")
            .withQuery("client_id", "id")
            .withQuery("client_secret", "secret")
            .withQuery("attributes", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]")
            .build();

      dataStore.findStubResponseFor(assertingRequest);

      assertThat(dataStore.getStubHttpLifecycles()).isEqualTo(expectedHttpLifecycles);
   }

   @Test
   public void shouldReturnMatchingStubbedResponse_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String url = "/entity.find";

      final String expectedStatus = "200";
      final String expectedBody = "{\"status\": \"hello world\"}";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withQuery("type_name", "user")
         .withQuery("client_id", "id")
         .withQuery("client_secret", "secret")
         .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
         .newStubbedResponse()
         .withStatus(expectedStatus)
         .withFoldedBody(expectedBody)
         .withHeaders("content-type", "application/json").build();

      loadYamlToDataStore(yaml);

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString())
         .thenReturn(
            "type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
         );

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenQueryParamArrayHasNonMatchedElementsWithinUrlEncodedQuotes() throws Exception {

      final String url = "/entity.find";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .withQuery("type_name", "user")
         .withQuery("client_id", "id")
         .withQuery("client_secret", "secret")
         .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
         .newStubbedResponse()
         .withStatus("200")
         .withFoldedBody("{\"status\": \"hello world\"}")
         .withHeaders("content-type", "application/json").build();

      loadYamlToDataStore(yaml);

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString())
         .thenReturn(
            "type_name=user&client_id=id&client_secret=secret&attributes=[%22NOMATCH%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
         );

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

      assertThat(foundStubResponse.getStatus()).isEqualTo("404");
      assertThat(foundStubResponse.getBody()).isEqualTo("");
   }

   private void loadYamlToDataStore(final String yaml) throws Exception {
      final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(".", FileUtils.constructReader(yaml));

      dataStore.resetStubHttpLifecycles(stubHttpLifecycles);
   }

   private List<StubHttpLifecycle> parseYamlToStubHttpLifecycles(final String yaml) throws Exception {
      return new YamlParser().parse(".", FileUtils.constructReader(yaml));
   }

}