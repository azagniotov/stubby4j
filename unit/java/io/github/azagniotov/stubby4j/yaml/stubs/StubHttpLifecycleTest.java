package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.builder.stubs.StubRequestBuilder;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created: 4/20/13 5:29 PM
 */
public class StubHttpLifecycleTest {

    private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();
    private static final String SOME_RESOURCE_URI = "/some/resource/uri";
    private static final String AUTHORIZATION_HEADER_BASIC = "Basic Ym9iOnNlY3JldA==";
    private static final String AUTHORIZATION_HEADER_BASIC_INVALID = "Basic 888888888888==";
    private static final String AUTHORIZATION_HEADER_BEARER = "Bearer Ym9iOnNlY3JldA==";

    @Test
    public void shouldFindStubHttpLifecycleEqual_WhenComparedToItself() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = new StubHttpLifecycle();

        final boolean assertionResult = expectedStubHttpLifecycle.equals(expectedStubHttpLifecycle);
        assertThat(assertionResult).isTrue();
    }

    @Test
    public void shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = new StubHttpLifecycle();
        final Object assertingObject = StubResponse.newStubResponse();

        final boolean assertionResult = expectedStubHttpLifecycle.equals(assertingObject);
        assertThat(assertionResult).isFalse();
    }

    @Test
    public void shouldReturnStubResponse_WhenNoSequenceResponses() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setResponse(stubResponse);

        assertThat(stubHttpLifecycle.getResponse(true)).isEqualTo(stubResponse);
    }

    @Test
    public void shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList() throws Exception {

        final List<StubResponse> sequence = new LinkedList<>();

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setResponse(sequence);

        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);
        assertThat(actualStubbedResponse.getStatus()).isEqualTo("200");
        assertThat(actualStubbedResponse.getBody()).isEmpty();
    }

    @Test
    public void shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

        final String expectedStatus = "200";
        final String expectedBody = "This is a sequence response #1";

        final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
            add(StubResponse.newStubResponse(expectedStatus, expectedBody));
        }};

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setResponse(sequence);

        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);
        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

        final String expectedStatus = "500";
        final String expectedBody = "This is a sequence response #2";

        final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
            add(StubResponse.newStubResponse("200", "This is a sequence response #1"));
            add(StubResponse.newStubResponse(expectedStatus, expectedBody));
        }};

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setResponse(sequence);

        // Do not remove this stubbing, even if this is an unused variable
        final StubResponse irrelevantStubbedResponse = stubHttpLifecycle.getResponse(true);
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);

        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldRequireBasicAuthorization() throws Exception {
        final StubRequest stubRequest = REQUEST_BUILDER
                .withUrl(SOME_RESOURCE_URI)
                .withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setRequest(stubRequest);

        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldRequireBearerAuthorization() throws Exception {
        final StubRequest stubRequest = REQUEST_BUILDER
                .withUrl(SOME_RESOURCE_URI)
                .withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BEARER)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setRequest(stubRequest);

        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldGetRawBasicAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = REQUEST_BUILDER
                .withUrl(SOME_RESOURCE_URI)
                .withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setRequest(stubRequest);

        assertThat(AUTHORIZATION_HEADER_BASIC).isEqualTo(stubHttpLifecycle.getRawAuthorizationHttpHeader());
    }

    @Test
    public void shouldGetRawBearerAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = REQUEST_BUILDER
                .withUrl(SOME_RESOURCE_URI)
                .withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setRequest(stubRequest);

        assertThat(AUTHORIZATION_HEADER_BEARER).isEqualTo(stubHttpLifecycle.getRawAuthorizationHttpHeader());
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet() throws Exception {
        final StubHttpLifecycle assertingStubHttpLifecycle = new StubHttpLifecycle();
        assertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).build());

        final StubHttpLifecycle stubbedStubHttpLifecycle = new StubHttpLifecycle();
        stubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build());

        assertThat(stubbedStubHttpLifecycle.isIncomingRequestUnauthorized(assertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle, times(1)).getRawAuthorizationHttpHeader();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedAuthorizationHeaderValue(any(StubAuthorizationTypes.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawAuthorizationHttpHeader();
        verify(spyStubbedStubHttpLifecycle, times(1)).getStubbedAuthorizationHeaderValue(StubAuthorizationTypes.BASIC);
    }

    @Test
    public void shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle, times(1)).getRawAuthorizationHttpHeader();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedAuthorizationHeaderValue(any(StubAuthorizationTypes.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawAuthorizationHttpHeader();
        verify(spyStubbedStubHttpLifecycle, times(1)).getStubbedAuthorizationHeaderValue(StubAuthorizationTypes.BASIC);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle, times(1)).getRawAuthorizationHttpHeader();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedAuthorizationHeaderValue(any(StubAuthorizationTypes.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawAuthorizationHttpHeader();
        verify(spyStubbedStubHttpLifecycle, times(1)).getStubbedAuthorizationHeaderValue(StubAuthorizationTypes.BEARER);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle, times(1)).getRawAuthorizationHttpHeader();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedAuthorizationHeaderValue(any(StubAuthorizationTypes.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawAuthorizationHttpHeader();
        verify(spyStubbedStubHttpLifecycle, times(1)).getStubbedAuthorizationHeaderValue(StubAuthorizationTypes.BEARER);
    }

    @Test
    public void shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals() throws Exception {

        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals() throws Exception {

        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyAssertingStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaders(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER).build());

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(new StubHttpLifecycle());
        spyStubbedStubHttpLifecycle.setRequest(REQUEST_BUILDER.withUrl(SOME_RESOURCE_URI).withHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle, times(1)).getRawAuthorizationHttpHeader();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedAuthorizationHeaderValue(any(StubAuthorizationTypes.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawAuthorizationHttpHeader();
        verify(spyStubbedStubHttpLifecycle, times(1)).getStubbedAuthorizationHeaderValue(StubAuthorizationTypes.BEARER);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeRequest() throws Exception {

        final String expectedPost = "this is a POST";
        final StubRequest stubRequest = StubRequest.newStubRequest(SOME_RESOURCE_URI, expectedPost);

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setRequest(stubRequest);

        final String actualPost = stubHttpLifecycle.getAjaxResponseContent(StubTypes.REQUEST, "post");

        assertThat(expectedPost).isEqualTo(actualPost);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeResponse() throws Exception {

        final String expectedBody = "this is a response body";
        final StubResponse stubResponse = StubResponse.newStubResponse("201", expectedBody);

        final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
        stubHttpLifecycle.setResponse(stubResponse);

        final String actualBody = stubHttpLifecycle.getAjaxResponseContent(StubTypes.RESPONSE, "body");

        assertThat(expectedBody).isEqualTo(actualBody);
    }
}
