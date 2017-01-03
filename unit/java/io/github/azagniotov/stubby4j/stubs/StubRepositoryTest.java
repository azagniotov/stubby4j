package io.github.azagniotov.stubby4j.stubs;

import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StubRepositoryTest {

    private static final File CONFIG_FILE = new File("parentPath", "childPath");

    private static final Future<List<StubHttpLifecycle>> COMPLETED_FUTURE =
            CompletableFuture.completedFuture(new LinkedList<StubHttpLifecycle>());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private StubbyHttpTransport mockStubbyHttpTransport;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private YAMLParser mockYAMLParser;


    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    @Captor
    private ArgumentCaptor<List<StubHttpLifecycle>> stubsCaptor;

    private StubRequest.Builder requestBuilder;
    private StubResponse.Builder responseBuilder;

    private StubRepository spyStubRepository;

    @Before
    public void beforeEach() throws Exception {
        requestBuilder = new StubRequest.Builder();
        responseBuilder = new StubResponse.Builder();

        final StubRepository stubRepository = new StubRepository(CONFIG_FILE, COMPLETED_FUTURE);
        final Field stubbyHttpTransportField = stubRepository.getClass().getDeclaredField("stubbyHttpTransport");
        FieldSetter.setField(stubRepository, stubbyHttpTransportField, mockStubbyHttpTransport);

        spyStubRepository = spy(stubRepository);
    }

    @Test
    public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        final boolean resetResult = spyStubRepository.resetStubsCache(stubs);

        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);
    }

    @Test
    public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        final boolean resetResult = spyStubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        final Optional<StubHttpLifecycle> matchedStubOptional = spyStubRepository.matchStubByIndex(0);
        assertThat(matchedStubOptional.isPresent()).isTrue();
    }

    @Test
    public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        final boolean resetResult = spyStubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        final Optional<StubHttpLifecycle> matchedStubOptional = spyStubRepository.matchStubByIndex(9999);
        assertThat(matchedStubOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        final boolean resetResult = spyStubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        final StubHttpLifecycle deletedHttpLifecycle = spyStubRepository.deleteStubByIndex(0);
        assertThat(deletedHttpLifecycle).isNotNull();
        assertThat(spyStubRepository.getStubs()).isEmpty();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenInvalidIndexGiven() throws Exception {

        expectedException.expect(IndexOutOfBoundsException.class);

        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        final boolean resetResult = spyStubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        spyStubRepository.deleteStubByIndex(9999);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldVerifyExpectedHttpLifeCycles_WhenRefreshingStubbedData() throws Exception {
        final List<StubHttpLifecycle> expectedStubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");

        when(mockYAMLParser.parse(anyString(), any(File.class))).thenReturn(expectedStubs);

        spyStubRepository.refreshStubsFromYAMLConfig(mockYAMLParser);

        verify(mockYAMLParser, times(1)).parse(stringCaptor.capture(), fileCaptor.capture());
        verify(spyStubRepository, times(1)).resetStubsCache(stubsCaptor.capture());

        assertThat(stubsCaptor.getValue()).isEqualTo(expectedStubs);
        assertThat(stringCaptor.getValue()).isEqualTo(CONFIG_FILE.getParent());
        assertThat(fileCaptor.getValue()).isEqualTo(CONFIG_FILE);
    }

    @Test
    public void shouldGetMarshalledYamlByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        spyStubRepository.resetStubsCache(stubs);

        final String actualMarshalledYaml = spyStubRepository.getStubYAMLByIndex(0);

        assertThat(actualMarshalledYaml).isEqualTo("This is marshalled yaml snippet");
    }

    @Test
    public void shouldFailToGetMarshalledYamlByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        expectedException.expect(IndexOutOfBoundsException.class);

        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse("/resource/item/1");
        spyStubRepository.resetStubsCache(stubs);

        spyStubRepository.getStubYAMLByIndex(10);
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse(expectedOriginalUrl);
        spyStubRepository.resetStubsCache(stubs);
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCyclesWithDefaultResponse(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        spyStubRepository.updateStubByIndex(0, newStubHttpLifecycle);
        final StubRequest stubbedNewRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedNewRequest.getUrl()).isEqualTo(expectedNewUrl);
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        expectedException.expect(IndexOutOfBoundsException.class);

        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithDefaultResponse(expectedOriginalUrl);
        spyStubRepository.resetStubsCache(stubs);
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCyclesWithDefaultResponse(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        spyStubRepository.updateStubByIndex(10, newStubHttpLifecycle);
    }

    @Test
    public void shouldUpdateStubResponseBody_WhenResponseIsRecordable() throws Exception {
        final String sourceToRecord = "http://google.com";
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithCustomResponse(expectedOriginalUrl, responseBuilder.emptyWithBody(sourceToRecord).build());

        spyStubRepository.resetStubsCache(stubs);

        final StubResponse stubbedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(stubbedResponse.getBody()).isEqualTo(sourceToRecord);
        assertThat(stubbedResponse.isRecordingRequired()).isTrue();

        final String actualResponseText = "OK, this is recorded response text!";
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), anyString())).thenReturn(new StubbyResponse(200, actualResponseText));

        for (int idx = 0; idx < 5; idx++) {
            doReturn(stubs.get(0).getRequest()).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
            final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
            final StubResponse recordedResponse = stubSearchResult.getMatch();

            assertThat(recordedResponse.getBody()).isEqualTo(actualResponseText);
            assertThat(recordedResponse.isRecordingRequired()).isFalse();
            assertThat(stubbedResponse.getBody()).isEqualTo(recordedResponse.getBody());
            assertThat(stubbedResponse.isRecordingRequired()).isFalse();
        }
        verify(mockStubbyHttpTransport, times(1)).fetchRecordableHTTPResponse(eq(stubbedRequest), anyString());
    }

    @Test
    public void shouldNotUpdateStubResponseBody_WhenResponseIsNotRecordable() throws Exception {
        final String recordingSource = "htt://google.com";  //makes it non recordable
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithCustomResponse(expectedOriginalUrl, responseBuilder.emptyWithBody(recordingSource).build());

        spyStubRepository.resetStubsCache(stubs);

        final StubResponse expectedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        doReturn(stubs.get(0).getRequest()).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
        final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
        final StubResponse failedToRecordResponse = stubSearchResult.getMatch();

        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(failedToRecordResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    public void shouldRecordingUsingIncomingRequestQueryStringAndStubbedRecordableUrl() throws Exception {
        final String sourceToRecord = "http://127.0.0.1:8888";
        final StubRequest stubbedRequest =
                requestBuilder
                        .withUrl("/search")
                        .withMethodGet()
                        .withHeader("content-type", Common.HEADER_APPLICATION_JSON)
                        .withQuery("queryOne", "([a-zA-Z]+)")
                        .withQuery("queryTwo", "([1-9]+)")
                        .build();
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithCustomResponse(stubbedRequest, responseBuilder.emptyWithBody(sourceToRecord).build());

        spyStubRepository.resetStubsCache(stubs);

        final String actualResponseText = "OK, this is recorded response text!";
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), stringCaptor.capture())).thenReturn(new StubbyResponse(200, actualResponseText));

        final StubRequest incomingRequest =
                requestBuilder
                        .withUrl("/search")
                        .withMethodGet()
                        .withHeader("content-type", Common.HEADER_APPLICATION_JSON)
                        .withQuery("queryTwo", "12345")
                        .withQuery("queryOne", "arbitraryValue")
                        .build();

        doReturn(incomingRequest).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
        final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
        final StubResponse recordedResponse = stubSearchResult.getMatch();

        assertThat(recordedResponse.getBody()).isEqualTo(actualResponseText);
        assertThat(stringCaptor.getValue()).isEqualTo(String.format("%s%s", sourceToRecord, incomingRequest.getUrl()));
    }

    @Test
    public void shouldNotUpdateStubResponseBody_WhenResponseIsRecordableButExceptionThrown() throws Exception {
        final String recordingSource = "http://google.com";
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCyclesWithCustomResponse(expectedOriginalUrl, responseBuilder.emptyWithBody(recordingSource).build());

        spyStubRepository.resetStubsCache(stubs);

        final StubResponse expectedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest matchedRequest = spyStubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenThrow(Exception.class);

        doReturn(stubs.get(0).getRequest()).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
        final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
        final StubResponse actualResponse = stubSearchResult.getMatch();

        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(actualResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "[\"alex\",\"tracy\"]";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();

        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamUrlEncodedArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "[\"alex\",\"tracy\"]";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();

        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=%5B%22alex%22,%22tracy%22%5D");

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamUrlEncodedArrayHasElementsWithinUrlEncodedSingleQuotes() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "['alex','tracy']";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=%5B%27alex%27,%27tracy%27%5D");

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryValuesHaveEncodedSinglePlus() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "stalin lenin truman";
        final String encodedRawQuery = paramOneValue.replaceAll("\\s+", "%2B");

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=" + encodedRawQuery);

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryValuesHaveMultipleRawPluses() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "stalin lenin truman";
        final String encodedRawQuery = paramOneValue.replaceAll("\\s+", "+++");

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=" + encodedRawQuery);

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryValuesHaveEncodedMultiplePluses() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "stalin lenin truman";
        final String encodedRawQuery = paramOneValue.replaceAll("\\s+", "%2B%2B%2B");

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=" + encodedRawQuery);

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryValues_HasArrayElementsWithEncodedSpacesWithinUrlEncodedSingleQuotes() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "['stalin and truman','are best friends']";
        final String encodedRawQuery = paramOneValue
                .replaceAll("\\s+", "%20%20")
                .replaceAll(Pattern.quote("["), "%5B")
                .replaceAll("\\]", "%5D")
                .replaceAll("'", "%27");

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=" + encodedRawQuery);

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryValues_HasArrayElementsWithEncodedPlusWithinUrlEncodedSingleQuotes() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "['stalin and truman','are best friends']";
        final String encodedRawQuery = paramOneValue
                .replaceAll("\\s+", "%2B%2B%2B")
                .replaceAll(Pattern.quote("["), "%5B")
                .replaceAll("\\]", "%5D")
                .replaceAll("'", "%27");

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=" + encodedRawQuery);

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamArrayElementsHaveDifferentSpacing() throws Exception {

        final String paramOne = "names";
        final String paramOneValue = "[\"alex\", \"tracy\"]";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                requestBuilder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue).build();


        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    private List<StubHttpLifecycle> buildHttpLifeCyclesWithDefaultResponse(final String url) throws Exception {
        return buildHttpLifeCyclesWithCustomResponse(url, StubResponse.okResponse());
    }

    private List<StubHttpLifecycle> buildHttpLifeCyclesWithCustomResponse(final String url, final StubResponse stubResponse) throws Exception {
        final StubRequest stubRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader("content-type", Common.HEADER_APPLICATION_JSON)
                        .build();

        return buildHttpLifeCyclesWithCustomResponse(stubRequest, stubResponse);
    }

    private List<StubHttpLifecycle> buildHttpLifeCyclesWithCustomResponse(final StubRequest stubRequest, final StubResponse stubResponse) throws Exception {
        final StubHttpLifecycle.Builder stubBuilder = new StubHttpLifecycle.Builder();
        stubBuilder.withRequest(stubRequest)
                .withResponse(stubResponse)
                .withCompleteYAML("This is marshalled yaml snippet");

        return new LinkedList<StubHttpLifecycle>() {{
            add(stubBuilder.build());
        }};
    }
}
