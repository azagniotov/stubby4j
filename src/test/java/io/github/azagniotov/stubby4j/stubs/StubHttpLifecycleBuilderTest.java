package io.github.azagniotov.stubby4j.stubs;

import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StubHttpLifecycleBuilderTest {

    private static final String SOME_RESOURCE_URI = "/some/resource/uri";
    private static final String AUTHORIZATION_HEADER_BASIC = "Basic Ym9iOnNlY3JldA==";
    private static final String AUTHORIZATION_HEADER_BASIC_INVALID = "Basic 888888888888==";
    private static final String AUTHORIZATION_HEADER_BEARER = "Bearer Ym9iOnNlY3JldA==";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private StubRequest.Builder requestBuilder;
    private StubResponse.Builder responseBuilder;
    private StubHttpLifecycle.Builder httpCycleBuilder;

    @Before
    public void setUp() throws Exception {
        requestBuilder = new StubRequest.Builder();
        responseBuilder = new StubResponse.Builder();
        httpCycleBuilder = new StubHttpLifecycle.Builder();
    }

    @Test
    public void shouldFindStubHttpLifecycleEqual_WhenComparedToItself() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = httpCycleBuilder.build();

        final boolean assertionResult = expectedStubHttpLifecycle.equals(expectedStubHttpLifecycle);
        assertThat(assertionResult).isTrue();
    }

    @Test
    public void shouldFindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
        final StubHttpLifecycle expectedStubHttpLifecycle = httpCycleBuilder.build();
        final Object assertingObject = StubResponse.okResponse();

        final boolean assertionResult = expectedStubHttpLifecycle.equals(assertingObject);
        assertThat(assertionResult).isFalse();
    }

    @Test
    public void shouldReturnStubResponse_WhenNoSequenceResponses() throws Exception {

        final StubResponse stubResponse = responseBuilder
                .withHttpStatusCode(Code.CREATED)
                .withBody("SELF")
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(stubResponse).build();

        assertThat(stubHttpLifecycle.getResponse(true)).isEqualTo(stubResponse);
    }

    @Test
    public void shouldReturnDescription_WhenDescription() {
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withDescription("wibble").build();

        assertThat(stubHttpLifecycle.getDescription()).isEqualTo("wibble");
    }

    @Test
    public void shouldThrow_WhenResponseObjectIsNotStubResponseType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Trying to set response of the wrong type");

        httpCycleBuilder.withResponse(8).build();
    }

    @Test
    public void shouldThrow_WhenResponseObjectIsNotCollectionType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Trying to set response of the wrong type");


        httpCycleBuilder.withResponse(new HashMap<>()).build();
    }

    @Test
    public void shouldReturnDefaultStubResponse_WhenNoSequenceResponsePresentInTheList() throws Exception {
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(new LinkedList<>()).build();
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);

        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(Code.OK);
        assertThat(actualStubbedResponse.getBody()).isEmpty();
    }

    @Test
    public void shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws Exception {

        final StubResponse stubResponse = responseBuilder
                .withHttpStatusCode(Code.CREATED)
                .withBody("SELF")
                .build();

        final Code expectedStatus = Code.OK;
        final String expectedBody = "This is a sequence response #1";

        final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
            add(responseBuilder.withHttpStatusCode(expectedStatus).withBody(expectedBody).build());
        }};

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(sequence).build();
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);

        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws Exception {

        final StubResponse stubResponse = responseBuilder
                .withHttpStatusCode(Code.CREATED)
                .withBody("SELF")
                .build();

        final Code expectedStatus = Code.INTERNAL_SERVER_ERROR;
        final String expectedBody = "This is a sequence response #2";

        final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
            add(responseBuilder.withHttpStatusCode(Code.OK).withBody("This is a sequence response #1").build());
            add(responseBuilder.withHttpStatusCode(expectedStatus).withBody(expectedBody).build());
        }};

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(sequence).build();

        // Do not remove this stubbing, even if this is an unused variable
        final StubResponse irrelevantStubbedResponse = stubHttpLifecycle.getResponse(true);
        final StubResponse actualStubbedResponse = stubHttpLifecycle.getResponse(true);

        assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
        assertThat(actualStubbedResponse.getHttpStatusCode()).isEqualTo(expectedStatus);
        assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
        assertThat(stubHttpLifecycle.getNextSequencedResponseId()).isEqualTo(0);
    }

    @Test
    public void shouldRequireBasicAuthorization() throws Exception {
        final StubRequest stubRequest = requestBuilder
                .withUrl(SOME_RESOURCE_URI)
                .withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();

        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldRequireBearerAuthorization() throws Exception {
        final StubRequest stubRequest = requestBuilder
                .withUrl(SOME_RESOURCE_URI)
                .withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BEARER)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();

        assertThat(stubHttpLifecycle.isAuthorizationRequired()).isTrue();
    }

    @Test
    public void shouldGetRawBasicAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = requestBuilder
                .withUrl(SOME_RESOURCE_URI)
                .withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();

        assertThat(AUTHORIZATION_HEADER_BASIC).isEqualTo(stubHttpLifecycle.getRawHeaderAuthorization());
    }

    @Test
    public void shouldGetRawBearerAuthorizationHttpHeader() throws Exception {
        final StubRequest stubRequest = requestBuilder
                .withUrl(SOME_RESOURCE_URI)
                .withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();

        assertThat(AUTHORIZATION_HEADER_BEARER).isEqualTo(stubHttpLifecycle.getRawHeaderAuthorization());
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAssertingAuthorizationHeaderBasicIsNotSet() throws Exception {
        final StubRequest assertingStubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubHttpLifecycle assertingStubHttpLifecycle = httpCycleBuilder.withRequest(assertingStubRequest).build();

        final StubRequest stubbedStubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();
        final StubHttpLifecycle stubbedStubHttpLifecycle = httpCycleBuilder.withRequest(stubbedStubRequest).build();

        assertThat(stubbedStubHttpLifecycle.isIncomingRequestUnauthorized(assertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldNotAuthorizeViaBasic_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicIsNotTheSame() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC_INVALID).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BASIC);
    }

    @Test
    public void shouldAuthorizeViaBasic_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBasicEquals() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BASIC).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBasic(AUTHORIZATION_HEADER_BASIC).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BASIC);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAssertingAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotSet() throws Exception {
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).build();
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldNotAuthorizeViaBearer_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {

        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isTrue();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerIsNotTheSame() throws Exception {

        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer("Bearer Ym9iOnNlY3JldA==").build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer 888888888888==").build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldAuthorizeViaBearer_WhenAuthorizationHeaderBearerEquals() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, AUTHORIZATION_HEADER_BEARER).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        assertThat(spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle)).isFalse();
    }

    @Test
    public void shouldVerifyBehaviour_WhenAuthorizationHeaderBearerEquals() throws Exception {
        final StubRequest stubbedRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();
        final StubRequest assertingRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withYAMLHeaderAuthorizationBearer(AUTHORIZATION_HEADER_BEARER).build();

        final StubHttpLifecycle spyStubbedStubHttpLifecycle = spy(httpCycleBuilder.withRequest(stubbedRequest).build());
        final StubHttpLifecycle spyAssertingStubHttpLifecycle = spy(httpCycleBuilder.withRequest(assertingRequest).build());

        spyStubbedStubHttpLifecycle.isIncomingRequestUnauthorized(spyAssertingStubHttpLifecycle);

        verify(spyAssertingStubHttpLifecycle).getRawHeaderAuthorization();
        verify(spyAssertingStubHttpLifecycle, never()).getStubbedHeaderAuthorization(any(StubbableAuthorizationType.class));

        verify(spyStubbedStubHttpLifecycle, never()).getRawHeaderAuthorization();
        verify(spyStubbedStubHttpLifecycle).getStubbedHeaderAuthorization(StubbableAuthorizationType.BEARER);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeRequest() throws Exception {

        final String expectedPost = "this is a POST";
        final StubRequest stubRequest = requestBuilder.withUrl(SOME_RESOURCE_URI).withPost(expectedPost).build();
        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withRequest(stubRequest).build();
        final String actualPost = stubHttpLifecycle.getAjaxResponseContent(StubTypes.REQUEST, "post");

        assertThat(expectedPost).isEqualTo(actualPost);
    }

    @Test
    public void shouldReturnAjaxResponseContent_WhenStubTypeResponse() throws Exception {

        final String expectedBody = "this is a response body";
        final StubResponse stubResponse = responseBuilder
                .withHttpStatusCode(Code.CREATED)
                .withBody(expectedBody)
                .build();

        final StubHttpLifecycle stubHttpLifecycle = httpCycleBuilder.withResponse(stubResponse).build();
        final String actualBody = stubHttpLifecycle.getAjaxResponseContent(StubTypes.RESPONSE, "body");

        assertThat(expectedBody).isEqualTo(actualBody);
    }
}
