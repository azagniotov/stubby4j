package by.stub.database;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.builder.yaml.YamlBuilder;
import by.stub.cli.CommandLineInterpreter;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

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
      CommandLineInterpreter.parseCommandLine(new String[]{});
      dataStore = new DataStore(new LinkedList<StubHttpLifecycle>());
   }

   @Before
   public void beforeEach() throws Exception {
      dataStore.resetStubHttpLifecycles(new LinkedList<StubHttpLifecycle>());
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

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("200")
         .withLiteralBody("This is a response for 123").build();

      loadYamlToDataStore(yaml);

      final StubRequest assertingRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationHeaderSet() throws Exception {

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
            .withHeaders(StubRequest.AUTH_HEADER, "Basic Ym9iOnNlY3JldA==").build();  //bob:secret

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderMissing() throws Exception {

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
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
      assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());
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
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenValidPostRequestMade() throws Exception {

      final String url = "/invoice/567";
      final String postData = "This is a post data";

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
            .withMethodPost()
            .withPost(postData).build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
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
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenQueryParamIsArray() throws Exception {

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
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnMatchingStubbedResponse_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString())
         .thenReturn(
            "type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
         );

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString())
         .thenReturn(
            "type_name=user&client_id=id&client_secret=secret&attributes=[%22NOMATCH%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
         );

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());
   }

   private void loadYamlToDataStore(final String yaml) throws Exception {
      final Reader reader = new StringReader(yaml);
      final YamlParser yamlParser = new YamlParser("");

      dataStore.resetStubHttpLifecycles(yamlParser.parseAndLoad(reader));
   }

}