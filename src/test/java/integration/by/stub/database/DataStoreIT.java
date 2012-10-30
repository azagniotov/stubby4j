package integration.by.stub.database;

import org.eclipse.jetty.http.HttpMethods;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */


public class DataStoreIT {

   private static DataStore dataStore;

   @BeforeClass
   public static void beforeClass() throws Exception {

      ANSITerminal.muteConsole(true);

      final URL url = DataStoreIT.class.getResource("/yaml/datastoreit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.parseAndLoad();

      dataStore = new DataStore(stubHttpLifecycles);
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequest() throws IOException {

      final String pathInfo = "/invoice/123";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof StubResponse);
      Assert.assertEquals(StubResponseTypes.DEFAULT, stubResponse.getStubResponseType());
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequestWithAuthorization() throws IOException {

      final String pathInfo = "/invoice/555";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic Ym9iOnNlY3JldA=="); //bob:secret

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
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
      when(mockHttpServletRequest.getQueryString()).thenReturn("");


      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
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
      when(mockHttpServletRequest.getQueryString()).thenReturn("");
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic 88888nNlY3JldA=="); //bob:secret

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
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
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
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

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertEquals(StubResponseTypes.DEFAULT, stubResponse.getStubResponseType());
   }


   @Test
   public void shouldReturnHttpLifecycleForPostRequestWithDefaultResponse() throws IOException {

      final String pathInfo = "/invoice/569";
      final String postData = "This is a post data";

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(pathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest mockAssertionRequest = StubRequest.creatFromHttpServletRequest(mockHttpServletRequest);
      final StubResponse stubResponse = dataStore.findStubResponseFor(mockAssertionRequest);

      Assert.assertTrue(stubResponse instanceof NotFoundStubResponse);
      Assert.assertEquals(StubResponseTypes.NOTFOUND, stubResponse.getStubResponseType());
   }
}