package integration.by.stub.http.client;

import by.stub.cli.ANSITerminal;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.http.client.ClientHttpResponse;
import by.stub.http.client.StubbyClient;
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

public class StubbyClientIT {

   private static String content;
   private static StubbyClient stubbyClient;

   private static final int SSL_PORT = 4443;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = StubbyClientIT.class.getResource("/yaml/stubbyclient-test-data.yaml");
      Assert.assertNotNull(url);

      ANSITerminal.muteConsole(true);
      stubbyClient = new StubbyClient();
      stubbyClient.startJetty(JettyFactory.DEFAULT_STUBS_PORT, SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, url.getFile());

      content = StringUtils.inputStreamToString(url.openStream());
   }

   @AfterClass
   public static void afterClass() throws Exception {
      stubbyClient.stopJetty();
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetOverSsl(host, uri, SSL_PORT);

      Assert.assertEquals(HttpStatus.OK_200, clientHttpResponse.getResponseCode());
      Assert.assertEquals("{\"id\" : \"1\", \"description\" : \"milk\"}", clientHttpResponse.getContent());
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
   public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.utf8Charset())));

      final String host = "localhost";
      final String uri = "/item/auth";

      final ClientHttpResponse clientHttpResponse = stubbyClient.doGetOverSsl(host, uri, SSL_PORT, encodedCredentials);

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

   @Test
   public void doPost_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, content);

      Assert.assertEquals(HttpStatus.CREATED_201, clientHttpResponse.getResponseCode());
      Assert.assertEquals("Configuration created successfully", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsEmpty() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, "");

      Assert.assertEquals(HttpStatus.NO_CONTENT_204, clientHttpResponse.getResponseCode());
      Assert.assertEquals("POST request on URI null was empty", clientHttpResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsNull() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.RESOURCE_STUBDATA_NEW;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final ClientHttpResponse clientHttpResponse = stubbyClient.doPost(host, uri, port, null);

      Assert.assertEquals(HttpStatus.NO_CONTENT_204, clientHttpResponse.getResponseCode());
      Assert.assertEquals("POST request on URI null was empty", clientHttpResponse.getContent());
   }
}
