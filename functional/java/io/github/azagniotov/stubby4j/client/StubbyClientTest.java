package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.exception.Stubby4JException;
import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.utils.StringUtils;
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
    private static final String ENCODED_STRING = "Ym9iOndyb25nLXNlY3JldA==";
    private static final String AUTHORIZATION_HEADER_CUSTOM = String.format("CustomAuthorizationName %s", ENCODED_STRING);
    private static final String BOB_SECRET = "bob:secret";

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
        final String host = "localhost";
        final String uri = "/item/auth";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64(BOB_SECRET)));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"8\", \"description\" : \"authorized using basic\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithBearerAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.BEARER, ENCODED_STRING));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using bearer\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithCustomAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/custom";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.CUSTOM, AUTHORIZATION_HEADER_CUSTOM));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using custom\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetOverSsl_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(host, uri, SSL_PORT, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64(BOB_SECRET)));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"8\", \"description\" : \"authorized using basic\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetOverSsl_ShouldMakeSuccessfulGetWithBearerAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(host, uri, SSL_PORT, new Authorization(Authorization.AuthorizationType.BEARER, ENCODED_STRING));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using bearer\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetOverSsl_ShouldMakeSuccessfulGetWithCustomAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/custom";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(host, uri, SSL_PORT, new Authorization(Authorization.AuthorizationType.CUSTOM, AUTHORIZATION_HEADER_CUSTOM));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using custom\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String uri = "/item/auth";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetUsingDefaults(uri, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64(BOB_SECRET)));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"8\", \"description\" : \"authorized using basic\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithBearerAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String uri = "/item/auth/bearer";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetUsingDefaults(uri, new Authorization(Authorization.AuthorizationType.BEARER, ENCODED_STRING));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using bearer\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGetUsingDefaultStubbyPortAndHost_ShouldMakeSuccessfulGetWithCustomAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String uri = "/item/auth/custom";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetUsingDefaults(uri, new Authorization(Authorization.AuthorizationType.CUSTOM, AUTHORIZATION_HEADER_CUSTOM));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"authorized using custom\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64("bob:wrong-secret")));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(String.format(UnauthorizedResponseHandlingStrategy.WRONG_AUTHORIZATION_HEADER_TEMPLATE, "Basic Ym9iOndyb25nLXNlY3JldA==")).isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithBearerAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.BEARER, "blahblahblah=="));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(String.format(UnauthorizedResponseHandlingStrategy.WRONG_AUTHORIZATION_HEADER_TEMPLATE, "Bearer blahblahblah==")).isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithCustomAuth_WhenWrongAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/custom";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port, new Authorization(Authorization.AuthorizationType.CUSTOM, "CustomAuthorizationName blahblahblah=="));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(String.format(UnauthorizedResponseHandlingStrategy.WRONG_AUTHORIZATION_HEADER_TEMPLATE, "CustomAuthorizationName blahblahblah==")).isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithBasicAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(UnauthorizedResponseHandlingStrategy.NO_AUTHORIZATION_HEADER).isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithBearerAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(UnauthorizedResponseHandlingStrategy.NO_AUTHORIZATION_HEADER).isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doGet_ShouldMakeSuccessfulGetWithCustomAuth_WhenNoAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/custom";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGet(host, uri, port);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        assertThat(UnauthorizedResponseHandlingStrategy.NO_AUTHORIZATION_HEADER).isEqualTo(stubbyResponse.getContent());
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
        final String host = "localhost";
        final String uri = "/item/submit";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;
        final String post = "{\"action\" : \"submit\"}";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPost(host, uri, port, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64(BOB_SECRET)), post);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("OK").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doPostUsingDefaults_ShouldMakeSuccessfulPostWithBasicAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String uri = "/item/submit";
        final String post = "{\"action\" : \"submit\"}";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPostUsingDefaults(uri, post, new Authorization(Authorization.AuthorizationType.BASIC, StringUtils.encodeBase64(BOB_SECRET)));

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

    @Test
    public void doPut_ShouldMakeSuccessfulPut() throws Exception {
        final String host = "localhost";
        final String uri = "/complex/json/tree/put";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final URL jsonContentUrl = StubbyClientTest.class.getResource("/json/graph.2.json");
        assertThat(jsonContentUrl).isNotNull();
        final String payload = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPut(host, uri, port, null, payload);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("OK").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doPutOverSsl_ShouldMakeSuccessfulPut() throws Exception {
        final String host = "localhost";
        final String uri = "/complex/json/tree/put";

        final URL jsonContentUrl = StubbyClientTest.class.getResource("/json/graph.2.json");
        assertThat(jsonContentUrl).isNotNull();
        final String payload = StringUtils.inputStreamToString(jsonContentUrl.openStream());

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doPutOverSsl(host, uri, SSL_PORT, null, payload);

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("OK").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doDelete_ShouldMakeSuccessfulGetWithBearerAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer/1";
        final int port = JettyFactory.DEFAULT_STUBS_PORT;

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doDelete(host, uri, port, new Authorization(Authorization.AuthorizationType.BEARER, ENCODED_STRING));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"deleted authorized using bearer\"}").isEqualTo(stubbyResponse.getContent());
    }

    @Test
    public void doDeleteOverSsl_ShouldMakeSuccessfulGetWithBearerAuth_WhenAuthCredentialsIsProvided() throws Exception {
        final String host = "localhost";
        final String uri = "/item/auth/bearer/1";

        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doDeleteOverSsl(host, uri, SSL_PORT, new Authorization(Authorization.AuthorizationType.BEARER, ENCODED_STRING));

        assertThat(stubbyResponse.getResponseCode()).isEqualTo(HttpStatus.OK_200);
        assertThat("{\"id\" : \"12\", \"description\" : \"deleted authorized using bearer\"}").isEqualTo(stubbyResponse.getContent());
    }
}
