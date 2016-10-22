package io.github.azagniotov.stubby4j.database;

import io.github.azagniotov.stubby4j.test.builders.stubs.StubRequestBuilder;
import io.github.azagniotov.stubby4j.test.builders.yaml.YamlBuilder;
import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import io.github.azagniotov.stubby4j.yaml.stubs.NotFoundStubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.RedirectStubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponseTypes;
import io.github.azagniotov.stubby4j.yaml.stubs.UnauthorizedStubResponse;
import org.fest.assertions.api.Assertions;
import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BASIC;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BEARER;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.CUSTOM;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */


@SuppressWarnings("serial")
public class StubbedDataManagerTest {

    private static StubbedDataManager stubbedDataManager;
    private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();
    private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

    @BeforeClass
    public static void beforeClass() throws Exception {
        stubbedDataManager = new StubbedDataManager(new File("."), new LinkedList<StubHttpLifecycle>());
    }

    @Before
    public void beforeEach() throws Exception {
        stubbedDataManager.resetStubHttpLifecycles(new LinkedList<StubHttpLifecycle>());
    }


    @Test
    public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasOneResponse() throws Exception {

        final String url = "/some/redirecting/uri";
        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = Common.HEADER_APPLICATION_JSON;
        final String sequenceResponseStatus = "200";
        final String sequenceResponseBody = "OK";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseLiteralBody(sequenceResponseBody)
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        Assertions.assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(sequenceResponseStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);

        final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
        assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
    }

    @Test
    public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasManyResponses() throws Exception {

        final String url = "/some/uri";

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = "application/xml";
        final String sequenceResponseStatus = "500";
        final String sequenceResponseBody = "OMFG";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OK")
                .withLineBreak()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseFoldedBody(sequenceResponseBody)
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse irrelevantFirstSequenceResponse = stubbedDataManager.findStubResponseFor(assertingRequest);
        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(sequenceResponseStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);

        final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
        assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
    }

    @Test
    public void shouldReturnFirstSequenceResponse_WhenAllSequenceResponsesHaveBeenConsumedInTheList() throws Exception {

        final String url = "/some/uri";

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = "application/xml";
        final String sequenceResponseStatus = "200";
        final String sequenceResponseBody = "OK";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseLiteralBody(sequenceResponseBody)
                .withLineBreak()
                .withSequenceResponseStatus("500")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseFoldedBody("OMFG")
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse irrelevantFirstSequenceResponse = stubbedDataManager.findStubResponseFor(assertingRequest);
        final StubResponse irrelevantLastSequenceResponse = stubbedDataManager.findStubResponseFor(assertingRequest);
        final StubResponse firstSequenceResponseRestarted = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(firstSequenceResponseRestarted).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(firstSequenceResponseRestarted.getStubResponseType());

        assertThat(firstSequenceResponseRestarted.getStatus()).isEqualTo(sequenceResponseStatus);
        assertThat(firstSequenceResponseRestarted.getBody()).isEqualTo(sequenceResponseBody);

        final MapEntry mapEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
        assertThat(firstSequenceResponseRestarted.getHeaders()).contains(mapEntry);
    }

    @Test
    public void shouldReturnMatchingRedirectResponse_WhenLocationHeaderSet() throws Exception {

        final String url = "/some/redirecting/uri";
        final String expectedStatus = "301";
        final String expectedBody = "oink";
        final String expectedHeaderKey = "location";
        final String expectedHeaderValue = "/invoice/123";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withHeaders(expectedHeaderKey, expectedHeaderValue)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(RedirectStubResponse.class);
        assertThat(StubResponseTypes.REDIRECT).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);

        final MapEntry mapEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
        assertThat(foundStubResponse.getHeaders()).contains(mapEntry);
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidGetRequestMade() throws Exception {

        final String url = "/invoice/123";
        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 123";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationBasicHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "'bob:secret'";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYamlProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, "Basic Ym9iOnNlY3JldA==").build();  //bob:secret

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationBearerHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "Ym9iOnNlY3JldA==";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BEARER.asYamlProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer Ym9iOnNlY3JldA==").build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationCustomHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "CustomAuthorizationName Ym9iOnNlY3JldA==";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(CUSTOM.asYamlProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, "CustomAuthorizationName Ym9iOnNlY3JldA==").build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderNotSubmitted() throws Exception {

        final String url = "/invoice/555";
        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "'bob:secret'";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYamlProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
        assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("401");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderSubmittedWithBadCredentials() throws Exception {

        final String url = "/invoice/555";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYamlProp(), "'bob:secret'")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 555").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders(BASIC.asYamlProp(), "Basic BadCredentials").build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
        assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("401");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderSubmittedWithNull() throws Exception {

        final String url = "/invoice/555";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYamlProp(), "'bob:secret'")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 555").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders(BASIC.asYamlProp(), null).build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(UnauthorizedStubResponse.class);
        assertThat(StubResponseTypes.UNAUTHORIZED).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("401");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnNotFoundStubResponse_WhenAssertingRequestWasNotMatched() throws Exception {

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/invoice/125")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 125").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl("/invoice/300")
                        .withMethodGet().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
        assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("404");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidPostRequestMade() throws Exception {

        final String url = "/invoice/567";
        final String postData = "This is a post data";
        final String expectedStatus = "503";
        final String expectedBody = "This is a response for 567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodPost()
                        .withPost(postData).build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }


    @Test
    public void shouldReturnNotFoundStubResponse_WhenPostBodyMissing() throws Exception {

        final String url = "/invoice/567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodPost().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
        assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("404");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnNotFoundStubResponse_WhenHittingCorrectUrlButWrongMethod() throws Exception {

        final String url = "/invoice/567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodPost().build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
        assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("404");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnNotFoundStubResponse_WhenPostRequestMadeToIncorrectUrl() throws Exception {

        final String url = "/invoice/non-existent-url";
        final String postData = "This is a post data";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl("/invoice/567")
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodPost()
                        .withPost(postData).build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
        assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("404");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenQueryParamIsArray() throws Exception {

        final String url = "/entity.find";
        final String expectedStatus = "200";
        final String expectedBody = "{\"status\": \"hello world\"}";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withFoldedBody(expectedBody)
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withQuery("type_name", "user")
                        .withQuery("client_id", "id")
                        .withQuery("client_secret", "secret")
                        .withQuery("attributes", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]")
                        .build();

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

        final String url = "/entity.find";

        final String expectedStatus = "200";
        final String expectedBody = "{\"status\": \"hello world\"}";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withFoldedBody(expectedBody)
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString())
                .thenReturn(
                        "type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
                );

        final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isNotInstanceOf(NotFoundStubResponse.class);
        assertThat(foundStubResponse).isInstanceOf(StubResponse.class);
        assertThat(StubResponseTypes.OK_200).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }


    @Test
    public void shouldReturnNotFoundStubResponse_WhenQueryParamArrayHasNonMatchedElementsWithinUrlEncodedQuotes() throws Exception {
        final String url = "/entity.find";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus("200")
                .withFoldedBody("{\"status\": \"hello world\"}")
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString())
                .thenReturn(
                        "type_name=user&client_id=id&client_secret=secret&attributes=[%22NOMATCH%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
                );

        final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertingRequest);

        assertThat(foundStubResponse).isInstanceOf(NotFoundStubResponse.class);
        assertThat(StubResponseTypes.NOTFOUND).isSameAs(foundStubResponse.getStubResponseType());

        assertThat(foundStubResponse.getStatus()).isEqualTo("404");
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnRequestAndResponseExternalFiles() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/two.external.files.yaml");
        final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(2);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnOnlyResponseExternalFile() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/one.external.files.yaml");
        final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isFalse();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnDedupedExternalFile() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/same.external.files.yaml");
        final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isFalse();
    }

    @Test
    public void shouldReturnOnlyResponseExternalFileWhenRequestFileFailedToLoad() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/request.null.external.files.yaml");
        final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isFalse();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnOnlyRequestExternalFileWhenResponseFileFailedToLoad() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubbedDataManagerTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/response.null.external.files.yaml");
        final Map<File, Long> externalFiles = stubbedDataManager.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isFalse();
    }

    @Test
    public void shouldVerifyGetAllResponsesInvokation_WhenInvokingGetExternalFiles() throws Exception {
        final URL yamlUrl = StubbedDataManagerTest.class.getResource("/yaml/two.cycles.with.multiple.responses.yaml");
        final InputStream stubsDatanputStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(parentDirectory, StringUtils.inputStreamToString(stubsDatanputStream));
        assertThat(stubHttpLifecycles.size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(0).getAllResponses().size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(1).getAllResponses().size()).isEqualTo(2);

        final List<StubHttpLifecycle> spyStubHttpLifecycles = new LinkedList<>();
        final StubHttpLifecycle spyCycleOne = spy(stubHttpLifecycles.get(0));
        final StubHttpLifecycle spyCycleTwo = spy(stubHttpLifecycles.get(1));
        spyStubHttpLifecycles.add(spyCycleOne);
        spyStubHttpLifecycles.add(spyCycleTwo);

        stubbedDataManager.resetStubHttpLifecycles(spyStubHttpLifecycles);   // 1st time call to getAllResponses
        stubbedDataManager.getExternalFiles();                               // 2nd time call to getAllResponses

        verify(spyCycleOne, times(2)).getAllResponses();
        verify(spyCycleTwo, times(2)).getAllResponses();
    }

    @Test
    public void shouldVerifyGetRawFileInvokation_WhenInvokingGetExternalFiles() throws Exception {
        final URL yamlUrl = StubbedDataManagerTest.class.getResource("/yaml/two.cycles.with.multiple.responses.yaml");
        final InputStream stubsDatanputStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(parentDirectory, StringUtils.inputStreamToString(stubsDatanputStream));
        assertThat(stubHttpLifecycles.size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(0).getAllResponses().size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(1).getAllResponses().size()).isEqualTo(2);

        stubHttpLifecycles.get(0).setResponse(new LinkedList<StubResponse>() {{
            add(spy(stubHttpLifecycles.get(0).getAllResponses().get(0)));
            add(spy(stubHttpLifecycles.get(0).getAllResponses().get(1)));
        }});

        stubHttpLifecycles.get(1).setResponse(new LinkedList<StubResponse>() {{
            add(spy(stubHttpLifecycles.get(1).getAllResponses().get(0)));
            add(spy(stubHttpLifecycles.get(1).getAllResponses().get(1)));
        }});

        stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);
        stubbedDataManager.getExternalFiles();

        verify(stubHttpLifecycles.get(0).getAllResponses().get(0), times(1)).getRawFile();
        verify(stubHttpLifecycles.get(0).getAllResponses().get(1), times(1)).getRawFile();
        verify(stubHttpLifecycles.get(1).getAllResponses().get(0), times(1)).getRawFile();
        verify(stubHttpLifecycles.get(1).getAllResponses().get(1), times(1)).getRawFile();
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenSomeHttpCycleWasDeleted() throws Exception {
        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        loadYamlToDataStore(String.format("%s%s%s%s%s", cycleOne, FileUtils.BR, cycleTwo, FileUtils.BR, cycleThree));

        List<StubHttpLifecycle> beforeDeletionLoadedHttpCycles = stubbedDataManager.getStubHttpLifecycles();
        assertThat(beforeDeletionLoadedHttpCycles.size()).isEqualTo(3);

        for (int resourceId = 0; resourceId < beforeDeletionLoadedHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeDeletionLoadedHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        stubbedDataManager.deleteStubHttpLifecycleByIndex(1);

        List<StubHttpLifecycle> afterDeletionLoadedHttpCycles = stubbedDataManager.getStubHttpLifecycles();
        assertThat(afterDeletionLoadedHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < afterDeletionLoadedHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterDeletionLoadedHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenHttpCyclesWereReset() throws Exception {
        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        loadYamlToDataStore(cycleOne);

        List<StubHttpLifecycle> beforeResetHttpCycles = stubbedDataManager.getStubHttpLifecycles();
        assertThat(beforeResetHttpCycles.size()).isEqualTo(1);

        for (int resourceId = 0; resourceId < beforeResetHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeResetHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(".", String.format("%s%s%s", cycleTwo, FileUtils.BR, cycleThree));
        stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);

        List<StubHttpLifecycle> afterResetHttpCycles = stubbedDataManager.getStubHttpLifecycles();
        assertThat(afterResetHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < afterResetHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterResetHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenSomeHttpCycleWasUpdated() throws Exception {

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        loadYamlToDataStore(String.format("%s%s%s", cycleTwo, FileUtils.BR, cycleThree));

        List<StubHttpLifecycle> beforeUpdateHttpCycles = stubbedDataManager.getStubHttpLifecycles();
        assertThat(beforeUpdateHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < beforeUpdateHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeUpdateHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/updating/cycle")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(".", cycleOne);
        final StubHttpLifecycle updatingStubHttpLifecycle = stubHttpLifecycles.get(0);

        stubbedDataManager.updateStubHttpLifecycleByIndex(0, updatingStubHttpLifecycle);
        List<StubHttpLifecycle> afterUpdateHttpCycles = stubbedDataManager.getStubHttpLifecycles();

        assertThat(afterUpdateHttpCycles.size()).isEqualTo(2);
        final String firstCycleUrl = afterUpdateHttpCycles.get(0).getRequest().getUrl();
        assertThat(firstCycleUrl).isEqualTo("/some/uri/updating/cycle?paramName1=paramValue1");

        for (int resourceId = 0; resourceId < afterUpdateHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterUpdateHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getAllResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    private void loadYamlToDataStore(final String yaml) throws Exception {
        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(".", yaml);

        stubbedDataManager.resetStubHttpLifecycles(stubHttpLifecycles);
    }

    private void resetStubHttpLifecyclesFromYamlResource(final String resourcePath) throws Exception {
        final URL yamlUrl = StubbedDataManagerTest.class.getResource(resourcePath);
        final InputStream stubsDatanputStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        stubbedDataManager.resetStubHttpLifecycles(new YamlParser().parse(parentDirectory, StringUtils.inputStreamToString(stubsDatanputStream)));
    }
}