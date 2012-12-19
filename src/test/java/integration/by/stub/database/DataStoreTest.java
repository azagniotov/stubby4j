package integration.by.stub.database;

import by.stub.cli.ANSITerminal;
import by.stub.cli.CommandLineInterpreter;
import by.stub.database.DataStore;
import by.stub.testing.junit.categories.IntegrationTest;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.eclipse.jetty.http.HttpMethods;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */


@SuppressWarnings("serial")
@Category(IntegrationTest.class)
public class DataStoreTest {

   private static DataStore dataStore;

   @BeforeClass
   public static void beforeClass() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});

      ANSITerminal.muteConsole(true);

      final URL url = DataStoreTest.class.getResource("/yaml/datastoreit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad();

      dataStore = new DataStore(stubHttpLifecycles);
   }

   @Test
   public void findStubResponseFor_ShouldFindRedirectStubResponse_WhenLocationHeaderIsSet() throws IOException {

      final String pathInfo = "/some/redirecting/uri";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof RedirectStubResponse);
      Assert.assertEquals(StubResponseTypes.REDIRECT, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequest() throws IOException {

      final String pathInfo = "/invoice/123";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof StubResponse);
      Assert.assertEquals(StubResponseTypes.DEFAULT, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequestWithAuthorization() throws IOException {

      final String pathInfo = "/invoice/555";

      final Enumeration<String> headerNames = Collections.enumeration(new ArrayList<String>() {{
         add(StubRequest.AUTH_HEADER);
      }});

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getHeaderNames()).thenReturn(headerNames);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic Ym9iOnNlY3JldA=="); //bob:secret

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof StubResponse);
      Assert.assertEquals(StubResponseTypes.DEFAULT, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequestWithMissingAuthorizationHeader() throws IOException {

      final String pathInfo = "/invoice/555";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);


      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof UnauthorizedStubResponse);
      Assert.assertEquals(StubResponseTypes.UNAUTHORIZED, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequestWithAuthorizationWithBadCredentials() throws IOException {

      final String pathInfo = "/invoice/555";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic 88888nNlY3JldA=="); //bob:secret

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof UnauthorizedStubResponse);
      Assert.assertEquals(StubResponseTypes.UNAUTHORIZED, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldReturnHttpLifecycleForGetRequestWithDefaultResponse() throws IOException {

      final String pathInfo = "/invoice/125";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof NotFoundStubResponse);
      Assert.assertEquals(StubResponseTypes.NOTFOUND, stubResponse.getStubResponseType());
   }


   @Test
   public void shouldFindHttpLifecycleForPostRequest() throws IOException {

      final String pathInfo = "/invoice/567";
      final String postData = "This is a post data";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");
      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertEquals(StubResponseTypes.DEFAULT, stubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnHttpLifecycleForPostRequestWithDefaultResponse() throws IOException {

      final String pathInfo = "/invoice/569";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest mockAssertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof NotFoundStubResponse);
      Assert.assertEquals(StubResponseTypes.NOTFOUND, stubResponse.getStubResponseType());
   }
}