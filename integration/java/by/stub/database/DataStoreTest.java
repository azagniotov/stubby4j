package by.stub.database;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.cli.CommandLineInterpreter;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
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

   private static final StubRequestBuilder BUILDER = new StubRequestBuilder();

   private static DataStore dataStore;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = DataStoreTest.class.getResource("/yaml/datastore.test.class.data.yaml");
      assertThat(url).isNotNull();

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad();

      dataStore = new DataStore(stubHttpLifecycles);

      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void shouldReturnRedirectResponse_WhenLocationHeaderSet() throws IOException {

      final String url = "/some/redirecting/uri";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(RedirectStubResponse.class);
      assertThat(StubResponseTypes.REDIRECT).isSameAs(foundStubResponse.getStubResponseType());
   }

   @Test
   public void shouldReturnDefaultStubResponse_WhenValidGetRequestMade() throws IOException {

      final String url = "/invoice/123";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnDefaultStubResponse_WhenValidAuthorizationHeaderSet() throws IOException {

      final String url = "/invoice/555";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders(StubRequest.AUTH_HEADER, "Basic Ym9iOnNlY3JldA==").build();  //bob:secret

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnUnauthorizedStubResponse_WhenAuthorizationHeaderMissing() throws IOException {

      final String url = "/invoice/555";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
      assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnUnauthorizedStubResponse_WhenAuthorizationHeaderSetWithBadCredentials() throws IOException {

      final String url = "/invoice/555";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders(StubRequest.AUTH_HEADER, "Basic BadCredentials").build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
      assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenAssertingRequestWasNotMatched() throws IOException {

      final String url = "/invoice/125";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodGet().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnDefaultStubResponse_WhenValidPostRequestMade() throws IOException {

      final String url = "/invoice/567";
      final String postData = "This is a post data";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodPost()
            .withPost(postData).build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
      assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
      assertThat(StubResponseTypes.DEFAULT).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenPostBodyMissing() throws IOException {

      final String url = "/invoice/567";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodPost().build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnNotFoundStubResponse_WhenPostRequestMadeToIncorrectUrl() throws IOException {

      final String url = "/invoice/non-existent-url";
      final String postData = "This is a post data";

      final StubRequest assertingRequest =
         BUILDER
            .withUrl(url)
            .withMethodPost()
            .withPost(postData).build();

      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertingRequest);

      assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
      assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnDefaultStubResponse_WhenQueryParamIsArray() throws IOException {

      final String url = "/entity.find";

      final StubRequest assertingRequest =
         BUILDER
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
   public void shouldReturnDefaultStubResponse_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String url = "/entity.find";

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

}