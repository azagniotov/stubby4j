package integration.by.stub.client;

import by.stub.client.ClientRequest;
import by.stub.server.JettyFactory;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import by.stub.cli.ANSITerminal;
import by.stub.client.Stubby4JClient;
import by.stub.client.Stubby4JResponse;
import by.stub.utils.StringUtils;

import java.net.URL;


/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */

public class Stubby4JClientStubsIT {

   private static Stubby4JClient stubby4JClient;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = Stubby4JClientStubsIT.class.getResource("/yaml/stubby4jclientstubs-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      stubby4JClient = new Stubby4JClient(url.getFile());
      stubby4JClient.startJetty();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubby4JClient.stopJetty();
   }

   @Test
   public void shouldDoPostOnURIWithQueryParams() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, "/item/path?paramOne=valueOne&paramTwo=12345", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.CREATED_201, stubby4JResponse.getResponseCode());
      Assert.assertEquals("OK", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/1", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.OK_200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorization() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));
      final String post = null;
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/auth", "localhost", JettyFactory.DEFAULT_STUBS_PORT, post, encodedCredentials);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.OK_200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorizationWithWrongCredentials() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:wrong-secret".getBytes(StringUtils.utf8Charset())));
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/auth", "localhost", JettyFactory.DEFAULT_STUBS_PORT, null, encodedCredentials);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Unauthorized with supplied encoded credentials: 'Ym9iOndyb25nLXNlY3JldA==' which decodes to 'bob:wrong-secret'", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnURIWithAuthorizationWithMissingCredentials() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/auth", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, stubby4JResponse.getResponseCode());
      Assert.assertEquals("You are not authorized to view this page without supplied 'Authorization' HTTP header", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnEmptyURI() throws Exception {

      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnNullURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, null, "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoGetOnIncorrectURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.GET, "/item/888", "localhost", JettyFactory.DEFAULT_STUBS_PORT);
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/888", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, "/item/1", "localhost", JettyFactory.DEFAULT_STUBS_PORT, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.OK_200, stubby4JResponse.getResponseCode());
      Assert.assertEquals("Got post response", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnEmptyURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, "", "localhost", JettyFactory.DEFAULT_STUBS_PORT, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", stubby4JResponse.getContent());
   }

   @Test
   public void shouldDoPostOnNullURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, null, "localhost", JettyFactory.DEFAULT_STUBS_PORT, "post body");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", stubby4JResponse.getContent());
   }

   @Test
   public void shouldFailWhenDoingIncorrectPostOnURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, "/item/1", "localhost", JettyFactory.DEFAULT_STUBS_PORT, "a");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1 for post data: a", stubby4JResponse.getContent());
   }

   @Test
   public void shouldNotFailWhenDoingEmptyPostOnURI() throws Exception {
      final ClientRequest clientRequest = new ClientRequest(HttpMethods.POST, "/item/1", "localhost", JettyFactory.DEFAULT_STUBS_PORT, "");
      final Stubby4JResponse stubby4JResponse = stubby4JClient.makeRequestWith(clientRequest);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, stubby4JResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1", stubby4JResponse.getContent());
   }
}
