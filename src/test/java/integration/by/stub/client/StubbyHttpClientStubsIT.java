package integration.by.stub.client;

import by.stub.cli.ANSITerminal;
import by.stub.client.http.ClientHttpResponse;
import by.stub.client.http.StubbyClient;
import by.stub.server.JettyFactory;
import by.stub.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;


/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */

public class StubbyHttpClientStubsIT {

   private static StubbyClient stubbyClient;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = StubbyHttpClientStubsIT.class.getResource("/yaml/stubby4jclientstubs-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      stubbyClient = new StubbyClient(url.getFile());
      stubbyClient.startJetty();
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubbyClient.stopJetty();
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGet() throws Exception {

      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetUsingDefaults(uri);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", clientHttpResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));
      final String uri = "/item/auth";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetUsingDefaults(uri, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"8\", \"description\" : \"authorized\"}", clientHttpResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:wrong-secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);


      Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Unauthorized with supplied encoded credentials: 'Ym9iOndyb25nLXNlY3JldA==' which decodes to 'bob:wrong-secret'", clientHttpResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, clientHttpResponse.getResponseCode());
      Assert.assertEquals("You are not authorized to view this page without supplied 'Authorization' HTTP header", clientHttpResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGet_WhenNoUriGiven() throws Exception {

      final String host = "localhost";
      final String uri = "";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", clientHttpResponse.getContent());
   }


   @Test
   public void doGet_ShouldThrowException_WhenGivenNullUri() throws Exception {
      final String host = "localhost";
      final String uri = null;
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /", clientHttpResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGet_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "/item/888";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGet(host, uri, port);

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for GET request at URI /item/888", clientHttpResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "post body");

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Got post response", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost() throws Exception {
      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, "post body");

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Got post response", clientHttpResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "post body");

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", clientHttpResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenNullUri() throws Exception {
      final String host = "localhost";
      final String uri = null;
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "post body");

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI / for post data: post body", clientHttpResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenWrongPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "unexpected or wrong post body");

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1 for post data: unexpected or wrong post body", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "");

      Assert.assertEquals(HttpStatus.NOT_FOUND_404, clientHttpResponse.getResponseCode());
      Assert.assertEquals("No data found for POST request at URI /item/1", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenNullPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, null);

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "");

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPostUsingDefaults(uri, "");

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("OK", clientHttpResponse.getContent());
   }
}
