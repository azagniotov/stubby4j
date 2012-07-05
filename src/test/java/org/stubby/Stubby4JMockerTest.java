package org.stubby;

import org.eclipse.jetty.http.HttpMethods;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/4/12, 11:30 PM
 */
public class Stubby4JMockerTest {

   private static Stubby4J stubby4J;

   @BeforeClass
   public static void beforeClass() throws Exception {
      stubby4J = new Stubby4J();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4J.clearMocks();
   }


   @Test
   public void shouldReturnExpectedResponseWhenSimulateGetOnURI() throws Exception {

      final String responseBody = "This is description of the item 8";
      final String uri = "/item/8";

      stubby4J.
            whenRequest().hasMethod(HttpMethods.GET).hasUri(uri).
            thenResponse().withBody(responseBody).withStatus("200").
            withHeader("content-type", "text/html").
            configure();

      final Map<String, String> responseData = stubby4J.simulateGetOnURI(uri);

      Assert.assertEquals("200", responseData.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals(responseBody, responseData.get(Stubby4J.KEY_RESPONSE));
      Assert.assertEquals("{content-type=text/html}", responseData.get(Stubby4J.KEY_RESPONSE_HEADERS));
   }


   @Test
   public void shouldReturnExpectedResponseWhenSimulatePostOnURI() throws Exception {

      final String responseBody = "This is description of the item 8";
      final String uri = "/item/8";
      final String postedData = "This is post data";

      stubby4J.
            whenRequest().hasMethod(HttpMethods.POST).hasUri(uri).hasPostBody(postedData).
            thenResponse().withBody(responseBody).withStatus("200").
            withHeader("content-type", "text/html").
            configure();

      final Map<String, String> responseData = stubby4J.simulatePostOnURI(uri, postedData);

      Assert.assertEquals("200", responseData.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals(responseBody, responseData.get(Stubby4J.KEY_RESPONSE));
      Assert.assertEquals("{content-type=text/html}", responseData.get(Stubby4J.KEY_RESPONSE_HEADERS));
   }

   @Test
   public void shouldReturn404ResponseWhenSimulateGetOnURI() throws Exception {

      final String responseBody = "This is description of the item 8";
      final String uri = "/item/8";
      final String wrongUri = "some/wrong/uri";

      stubby4J.
            whenRequest().hasMethod(HttpMethods.GET).hasUri(uri).
            thenResponse().withBody(responseBody).withStatus("200").
            withHeader("content-type", "text/html").
            configure();

      final Map<String, String> responseData = stubby4J.simulateGetOnURI(wrongUri);

      Assert.assertEquals("404", responseData.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals(String.format("No response found for %s request on %s", HttpMethods.GET, wrongUri), responseData.get(Stubby4J.KEY_RESPONSE));
   }

   @Test
   public void shouldReturn404ResponseWhenSimulatePostOnURI() throws Exception {

      final String responseBody = "This is description of the item 8";
      final String uri = "/item/8";
      final String wrongUri = "some/wrong/uri";
      final String postedData = "This is post data";

      stubby4J.
            whenRequest().hasMethod(HttpMethods.POST).hasUri(uri).hasPostBody(postedData).
            thenResponse().withBody(responseBody).withStatus("200").
            withHeader("content-type", "text/html").
            configure();

      final Map<String, String> responseData = stubby4J.simulatePostOnURI(wrongUri, postedData);

      final String expected = String.format("No response found for %s request on %s with post data %s", HttpMethods.POST, wrongUri, postedData);
      Assert.assertEquals("404", responseData.get(Stubby4J.KEY_STATUS));
      Assert.assertEquals(expected, responseData.get(Stubby4J.KEY_RESPONSE));
   }
}