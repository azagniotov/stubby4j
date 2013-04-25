package by.stub.client;

import by.stub.exception.Stubby4JException;
import by.stub.handlers.StubsRegistrationHandler;
import by.stub.repackaged.org.apache.commons.codec.binary.Base64;
import by.stub.server.JettyFactory;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */

public class StubbyClientTest {

   private static String content;
   private static StubbyClient stubbyClient;

   private static final int SSL_PORT = 4443;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = StubbyClientTest.class.getResource("/yaml/stubs.data.yaml");
      assertThat(url).isNotNull();

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

      final StubbyResponse stubbyResponse = stubbyClient.doGetOverSsl(host, uri, SSL_PORT);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void makeRequest_ShouldMakeSuccessfulGetOverSsl() throws Exception {

      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = stubbyClient.makeRequest(
         HttpSchemes.HTTPS,
         HttpMethods.GET,
         JettyFactory.DEFAULT_HOST,
         uri,
         SSL_PORT,
         null);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test(expected = Stubby4JException.class)
   public void makeRequest_ShouldFailToMakeRequest_WhenUnsupportedMethodGiven() throws Exception {

      stubbyClient.makeRequest(HttpSchemes.HTTPS, HttpMethods.MOVE, JettyFactory.DEFAULT_HOST,
         "/item/1", SSL_PORT, null);
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGet() throws Exception {

      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = stubbyClient.doGetUsingDefaults(uri);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";

      final StubbyResponse stubbyResponse = stubbyClient.doGetOverSsl(host, uri, SSL_PORT, encodedCredentials);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));
      final String uri = "/item/auth";

      final StubbyResponse stubbyResponse = stubbyClient.doGetUsingDefaults(uri, encodedCredentials);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:wrong-secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port, encodedCredentials);


      assertThat(HttpStatus.UNAUTHORIZED_401).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("Unauthorized with supplied encoded credentials: 'Ym9iOndyb25nLXNlY3JldA==' which decodes to 'bob:wrong-secret'").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port);

      assertThat(HttpStatus.UNAUTHORIZED_401).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("You are not authorized to view this page without supplied 'Authorization' HTTP header").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGet_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "/item/888";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doGet(host, uri, port);

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("No data found for GET request at URI /item/888").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "post body");

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("Got post response").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, encodedCredentials, post);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, post, encodedCredentials);

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost() throws Exception {
      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, "post body");

      assertThat(HttpStatus.OK_200).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("Got post response").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "post body");

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("No data found for POST request at URI / for post data: post body").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenNullUri() throws Exception {
      final String host = "localhost";
      final String uri = null;
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "post body");

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("No data found for POST request at URI / for post data: post body").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenWrongPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "unexpected or wrong post body");

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("No data found for POST request at URI /item/1 for post data: unexpected or wrong post body").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "");

      assertThat(HttpStatus.NOT_FOUND_404).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("No data found for POST request at URI /item/1").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenNullPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, null);

      assertThat(HttpStatus.CREATED_201).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "");

      assertThat(HttpStatus.CREATED_201).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";

      final StubbyResponse stubbyResponse = stubbyClient.doPostUsingDefaults(uri, "");

      assertThat(HttpStatus.CREATED_201).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.ENDPOINT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, content);

      assertThat(HttpStatus.CREATED_201).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("Configuration created successfully").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsEmpty() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.ENDPOINT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, "");

      assertThat(HttpStatus.NO_CONTENT_204).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("POST request on URI null was empty").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsNull() throws Exception {
      final String host = "localhost";
      final String uri = StubsRegistrationHandler.ENDPOINT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = stubbyClient.doPost(host, uri, port, null);

      assertThat(HttpStatus.NO_CONTENT_204).isEqualTo(stubbyResponse.getResponseCode());
      assertThat("POST request on URI null was empty").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGet_AndReturnSequencedResponse() throws Exception {

      final String host = "localhost";
      final String uri = "/uri/with/sequenced/responses";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      StubbyResponse firstSequencedStubbyResponse = stubbyClient.doGet(host, uri, port);
      assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequencedStubbyResponse.getResponseCode());
      assertThat(firstSequencedStubbyResponse.getContent()).isEqualTo("OK");

      final StubbyResponse secondSequencedStubbyResponse = stubbyClient.doGet(host, uri, port);
      assertThat(HttpStatus.CREATED_201).isEqualTo(secondSequencedStubbyResponse.getResponseCode());
      assertThat(secondSequencedStubbyResponse.getContent()).isEqualTo("Still going strong!");

      final StubbyResponse thirdSequencedStubbyResponse = stubbyClient.doGet(host, uri, port);
      assertThat(HttpStatus.INTERNAL_SERVER_ERROR_500).isEqualTo(thirdSequencedStubbyResponse.getResponseCode());
      assertThat(thirdSequencedStubbyResponse.getContent()).isEqualTo("Server Error");

      firstSequencedStubbyResponse = stubbyClient.doGet(host, uri, port);
      assertThat(HttpStatus.CREATED_201).isEqualTo(firstSequencedStubbyResponse.getResponseCode());
      assertThat(firstSequencedStubbyResponse.getContent()).isEqualTo("OK");
   }
}
