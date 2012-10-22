package integration;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpMethods;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.client.ClientRequestInfo;
import org.stubby.client.Stubby4JClient;
import org.stubby.client.Stubby4JClientFactory;
import org.stubby.client.Stubby4JResponse;

import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */
public class Stubby4JClientIntegrationTest {

   private static Stubby4JClient stubby4JClient;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JClientIntegrationTest.class.getResource("/atom-feed.yaml");
      Assert.assertNotNull(url);

      stubby4JClient = Stubby4JClientFactory.getInstance(url.getFile());
      stubby4JClient.start();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stop();
      Thread.sleep(2000); //To make sure Jetty has stopped before running another suite
   }

   @Test
   public void shouldDoGetOnURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/1", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorization() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(Charset.forName("UTF-8"))));
      final String postBody = null;
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/auth", "localhost", 8882, postBody, encodedCredentials);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorizationWithWrongCredentials() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:wrong-secret".getBytes(Charset.forName("UTF-8"))));
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/auth", "localhost", 8882, null, encodedCredentials);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(401, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Unauthorized with supplied encoded credentials: 'Ym9iOndyb25nLXNlY3JldA==' which decodes to 'bob:wrong-secret'", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorizationWithMissingCredentials() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/auth", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(401, stubby4JResponse.getResponseCode());
      Assert.assertEquals("You are not authorized to view this page without supplied 'Authorization' HTTP header", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnEmptyURI() throws Exception {

      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnNullURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, null, "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnIncorrectURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.GET, "/item/888", "localhost", 8882);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/888", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, "/item/1", "localhost", 8882, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Got post response", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnEmptyURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, "", "localhost", 8882, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnNullURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, null, "localhost", 8882, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", stubby4JResponse.getContent());
   }

   @Test
   public void shouldFailWhenDoingIncorrectPostOnURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, "/item/1", "localhost", 8882, "a");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1 for post data: a", stubby4JResponse.getContent());
   }

   @Test
   public void shouldNotFailWhenDoingEmptyPostOnURI() throws Exception {
      final ClientRequestInfo clientRequest = new ClientRequestInfo(HttpMethods.POST, "/item/1", "localhost", 8882, "");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1", stubby4JResponse.getContent());
   }
}