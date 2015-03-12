package by.stub.client;

import by.stub.exception.Stubby4JException;
import by.stub.handlers.AdminPortalHandler;
import by.stub.repackaged.org.apache.commons.codec.binary.Base64;
import by.stub.server.JettyFactory;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 6/28/12, 2:54 PM
 */
public class StubbyClientTest {

   private static final StubbyClient STUBBY_CLIENT = new StubbyClient();

   private static final int SSL_PORT = 4443;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final URL url = StubbyClientTest.class.getResource("/yaml/stubs.yaml");
      STUBBY_CLIENT.startJetty(JettyFactory.DEFAULT_STUBS_PORT, SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, url.getFile());
   }

   @AfterClass
   public static void afterClass() throws Exception {
      STUBBY_CLIENT.stopJetty();
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(host, uri, SSL_PORT);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void makeRequest_ShouldMakeSuccessfulGetOverSsl() throws Exception {

      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.makeRequest(
         HttpScheme.HTTPS.asString(),
         HttpMethod.GET.asString(),
         JettyFactory.DEFAULT_HOST,
         uri,
         SSL_PORT,
         null);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test(expected = Stubby4JException.class)
   public void makeRequest_ShouldFailToMakeRequest_WhenUnsupportedMethodGiven() throws Exception {

      STUBBY_CLIENT.makeRequest(HttpScheme.HTTPS.asString(), HttpMethod.MOVE.asString(), JettyFactory.DEFAULT_HOST,
         "/item/1", SSL_PORT, null);
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGet() throws Exception {

      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGet() throws Exception {

      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetUsingDefaults(uri);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"1\", \"description\" : \"milk\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, encodedCredentials);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(host, uri, SSL_PORT, encodedCredentials);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));
      final String uri = "/item/auth";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetUsingDefaults(uri, encodedCredentials);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("{\"id\" : \"8\", \"description\" : \"authorized\"}").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:wrong-secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, encodedCredentials);


      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
      assertThat("Unauthorized with supplied encoded credentials: 'Ym9iOndyb25nLXNlY3JldA==' which decodes to 'bob:wrong-secret'").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
      final String host = "localhost";
      final String uri = "/item/auth";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
      assertThat("You are not authorized to view this page without supplied 'Authorization' HTTP header").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doGet_ShouldMakeSuccessfulGet_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "/item/888";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(stubbyResponse.getContent()).contains("(404) Nothing found for GET request at URI /item/888");
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "post body");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("Got post response").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String host = "localhost";
      final String uri = "/item/submit";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, encodedCredentials, post);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
      final String encodedCredentials = new String(Base64.encodeBase64("bob:secret".getBytes(StringUtils.charsetUTF8())));

      final String uri = "/item/submit";
      final String post = "{\"action\" : \"submit\"}";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPostUsingDefaults(uri, post, encodedCredentials);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost() throws Exception {
      final String uri = "/item/1";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPostUsingDefaults(uri, "post body");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
      assertThat("Got post response").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyUri() throws Exception {
      final String host = "localhost";
      final String uri = "";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "post body");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(stubbyResponse.getContent()).contains("(404) Nothing found for POST request at URI /");
      assertThat(stubbyResponse.getContent()).contains("With post data: post body");
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenNullUri() throws Exception {
      final String host = "localhost";
      final String uri = null;
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "post body");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(stubbyResponse.getContent()).contains("(404) Nothing found for POST request at URI /");
      assertThat(stubbyResponse.getContent()).contains("With post data: post body");
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenWrongPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "unexpected or wrong post body");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(stubbyResponse.getContent()).contains("(404) Nothing found for POST request at URI /item/1");
      assertThat(stubbyResponse.getContent()).contains("With post data: unexpected or wrong post body");
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenGivenEmptyPostData() throws Exception {
      final String host = "localhost";
      final String uri = "/item/1";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NOT_FOUND_404);
      assertThat(stubbyResponse.getContent()).contains("(404) Nothing found for POST request at URI /item/1");
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenNullPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, null);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }


   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String host = "localhost";
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPostUsingDefaults_ShouldMakeSuccessfulPost_WhenEmptyPostGiven() throws Exception {
      final String uri = "/item/path?paramOne=valueOne&paramTwo=12345";

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPostUsingDefaults(uri, "");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat("OK").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String host = "localhost";
      final String uri = AdminPortalHandler.ADMIN_ROOT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final URL url = StubbyClientTest.class.getResource("/yaml/stubs.yaml");
      final InputStream stubsDatanputStream = url.openStream();
      final String content = StringUtils.inputStreamToString(stubsDatanputStream);
      stubsDatanputStream.close();

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, content);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat("Configuration created successfully").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void updateStubbedData_ShouldMakeSuccessfulPostToCreateStubData() throws Exception {
      final String adminUrl = String.format("http://localhost:%s%s", JettyFactory.DEFAULT_ADMIN_PORT, AdminPortalHandler.ADMIN_ROOT);

      final URL url = StubbyClientTest.class.getResource("/yaml/stubs.yaml");
      final InputStream stubsDatanputStream = url.openStream();
      final String content = StringUtils.inputStreamToString(stubsDatanputStream);
      stubsDatanputStream.close();

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.updateStubbedData(adminUrl, content);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat("Configuration created successfully").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsEmpty() throws Exception {
      final String host = "localhost";
      final String uri = AdminPortalHandler.ADMIN_ROOT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, "");

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat("POST request on URI / was empty").isEqualTo(stubbyResponse.getContent());
   }

   @Test
   public void doPost_ShouldMakeSuccessfulPost_WhenPostStubDataIsNull() throws Exception {
      final String host = "localhost";
      final String uri = AdminPortalHandler.ADMIN_ROOT;
      final int port = JettyFactory.DEFAULT_ADMIN_PORT;

      final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, null);

      assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(stubbyResponse.getContent()).isEqualTo("POST request on URI / was empty");
   }

   @Test
   public void doGet_ShouldMakeSuccessfulGet_AndReturnSequencedResponse() throws Exception {

      final String host = "localhost";
      final String uri = "/uri/with/sequenced/responses";
      final int port = JettyFactory.DEFAULT_STUBS_PORT;

      StubbyResponse firstSequencedStubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);
      assertThat(firstSequencedStubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat(firstSequencedStubbyResponse.getContent()).isEqualTo("OK");

      final StubbyResponse secondSequencedStubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);
      assertThat(secondSequencedStubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat(secondSequencedStubbyResponse.getContent()).isEqualTo("Still going strong!");

      final StubbyResponse thirdSequencedStubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);
      assertThat(thirdSequencedStubbyResponse.getResponseCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
      assertThat(thirdSequencedStubbyResponse.getContent()).isEqualTo("Server Error");

      firstSequencedStubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);
      assertThat(firstSequencedStubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat(firstSequencedStubbyResponse.getContent()).isEqualTo("OK");
   }
}
