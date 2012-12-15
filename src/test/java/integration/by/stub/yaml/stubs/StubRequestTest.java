package integration.by.stub.yaml.stubs;

import by.stub.testing.junit.categories.IntegrationTest;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 3:44 PM
 */
@SuppressWarnings("serial")
@Category(IntegrationTest.class)
public class StubRequestTest {
   private static List<StubHttpLifecycle> stubHttpLifecycles;

   @BeforeClass
   public static void beforeClass() throws Exception {

      final URL url = StubRequestTest.class.getResource("/yaml/stubrequestit-test-data.yaml");
      Assert.assertNotNull(url);

      final YamlParser yamlParser = new YamlParser(url.getFile());
      stubHttpLifecycles = yamlParser.parseAndLoad();
   }

   @Test
   public void ShouldMatchRequest_WhenMethodInRequestMethodArray() throws Exception {
      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());
      for (final StubHttpLifecycle stub : stubHttpLifecycles) {
         if (stub.getRequest().getUrl().equals("/invoice/789")) {
            Assert.assertEquals(stub.getResponse(), assertionLifecycle.getResponse());
            return;
         }
      }
   }

   @Test
   public void ShouldMatchRequest_WhenAllHeadersMatching() throws Exception {
      final Map<String, String> headers = new HashMap<String, String>() {{
         put("content-type", "application/xml");
         put("content-length", "30");
         put("content-language", "en-US");
      }};

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());
      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenNoHeadersWerePassed() throws Exception {
      final Map<String, String> headers = new HashMap<String, String>();

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenAllCamelCasedHeadersMatching() throws Exception {
      final Map<String, String> headers = new HashMap<String, String>() {{
         put("Content-Type", "application/xml");
         put("Content-Length", "30");
         put("Content-Language", "en-US");
      }};

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldFailToMatchRequest_WhenHeadersMismatch() throws Exception {
      final Map<String, String> headers = new HashMap<String, String>() {{
         put("content-type", "application/json");
         put("content-length", "30");
         put("content-language", "en-US");
      }};

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldFailToMatchRequest_WhenNotAllHeadersMatching() throws Exception {
      final Map<String, String> headers = new HashMap<String, String>() {{
         put("content-type", "application/xml");
         put("content-length", "30");
      }};

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenAllStubbedHeadersMatching() throws Exception {

      final Map<String, String> headers = new HashMap<String, String>() {{
         put("Content-type", "application/xml");
         put("content-length", "30");
         put("Content-Language", "en-US");
         put("content-encoding", "UTF-8");
         put("Pragma", "no-cache");
      }};

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/123");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      assertionRequest.setHeaders(headers);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenStubDidNotSpecifyQueryParams() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/456");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("paramOne=one&paramTwo=two");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenAllQueryParamsMatch() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("paramTwo=two&paramOne=one");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldMatchRequest_WhenStubbedQueryParamsMatch() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("paramTwo=two&paramOne=one&paramThree=three");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertTrue(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldFailRequest_WhenThereIsQueryParamMismatch() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("paramTwo=888&paramOne=one&paramThree=three");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldFailRequest_WhenNoQueryParamsPassed() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

   @Test
   public void ShouldFailRequest_WhenNotAllQueryParamsPassed() throws Exception {

      final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn("/invoice/789");
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("paramOne=one");

      final StubRequest assertionRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      final StubHttpLifecycle assertionLifecycle = new StubHttpLifecycle(assertionRequest, new StubResponse());

      Assert.assertFalse(stubHttpLifecycles.contains(assertionLifecycle));
   }

}
