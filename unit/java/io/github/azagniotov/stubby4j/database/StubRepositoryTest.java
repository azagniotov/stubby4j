package io.github.azagniotov.stubby4j.database;

import io.github.azagniotov.stubby4j.builder.stubs.StubRequestBuilder;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StubRepositoryTest {

    private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();
    private static final File CONFIG_FILE = new File(".");

    private static final Future<List<StubHttpLifecycle>> COMPLETED_FUTURE =
            CompletableFuture.completedFuture(new LinkedList<StubHttpLifecycle>());

    @Mock
    private StubbyHttpTransport mockStubbyHttpTransport;

    @Mock
    private YAMLParser mockYAMLParser;

    @Captor
    private ArgumentCaptor<String> urlToRecordCaptor;

    private StubRepository stubRepository;

    @Before
    public void beforeEach() throws Exception {
        stubRepository = new StubRepository(CONFIG_FILE, COMPLETED_FUTURE);
        ReflectionUtils.injectObjectFields(stubRepository, "stubbyHttpTransport", mockStubbyHttpTransport);
    }

    @Test
    public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubRepository.resetStubsCache(stubs);

        assertThat(resetResult).isTrue();
        assertThat(stubRepository.getStubs().size()).isNotZero();
    }

    @Test
    public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(stubRepository.getStubs().size()).isNotZero();

        final StubHttpLifecycle matchedHttpLifecycle = stubRepository.matchStubByIndex(0);
        assertThat(matchedHttpLifecycle).isNotNull();
    }

    @Test
    public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(stubRepository.getStubs().size()).isNotZero();

        final StubHttpLifecycle matchedHttpLifecycle = stubRepository.matchStubByIndex(9999);
        assertThat(matchedHttpLifecycle).isNull();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(stubRepository.getStubs().size()).isNotZero();

        final StubHttpLifecycle deletedHttpLifecycle = stubRepository.deleteStubByIndex(0);
        assertThat(deletedHttpLifecycle).isNotNull();
        assertThat(stubRepository.getStubs().size()).isZero();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldDeleteOriginalHttpCycleList_WhenInvalidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubRepository.resetStubsCache(stubs);
        assertThat(resetResult).isTrue();
        assertThat(stubRepository.getStubs().size()).isNotZero();

        stubRepository.deleteStubByIndex(9999);
    }

    @Test
    public void shouldGetMarshalledYamlByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        stubRepository.resetStubsCache(stubs);

        final String actualMarshalledYaml = stubRepository.getStubYAMLByIndex(0);

        assertThat(actualMarshalledYaml).isEqualTo("This is marshalled yaml snippet");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToGetMarshalledYamlByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");
        stubRepository.resetStubsCache(stubs);

        stubRepository.getStubYAMLByIndex(10);
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(expectedOriginalUrl);
        stubRepository.resetStubsCache(stubs);
        final StubRequest stubbedRequest = stubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        stubRepository.updateStubByIndex(0, newStubHttpLifecycle);
        final StubRequest stubbedNewRequest = stubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedNewRequest.getUrl()).isEqualTo(expectedNewUrl);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldUpdateStubHttpLifecycleByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(expectedOriginalUrl);
        stubRepository.resetStubsCache(stubs);
        final StubRequest stubbedRequest = stubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        stubRepository.updateStubByIndex(10, newStubHttpLifecycle);
    }

    @Test
    public void shouldUpdateStubResponseBody_WhenResponseIsRecordable() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(expectedOriginalUrl);

        final String sourceToRecord = "http://google.com";
        stubs.get(0).setResponse(StubResponse.newStubResponse("200", sourceToRecord));
        stubRepository.resetStubsCache(stubs);

        final StubResponse stubbedResponse = stubRepository.getStubs().get(0).getResponse(true);
        assertThat(stubbedResponse.getBody()).isEqualTo(sourceToRecord);
        assertThat(stubbedResponse.isRecordingRequired()).isTrue();

        final String actualResponseText = "OK, this is recorded response text!";
        final StubRequest stubbedRequest = stubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), anyString())).thenReturn(new StubbyResponse(200, actualResponseText));

        for (int idx = 0; idx < 5; idx++) {
            final StubResponse recordedResponse = stubRepository.findStubResponseFor(stubs.get(0).getRequest());

            assertThat(recordedResponse.getBody()).isEqualTo(actualResponseText);
            assertThat(recordedResponse.isRecordingRequired()).isFalse();
            assertThat(stubbedResponse.getBody()).isEqualTo(recordedResponse.getBody());
            assertThat(stubbedResponse.isRecordingRequired()).isFalse();
        }
        verify(mockStubbyHttpTransport, times(1)).fetchRecordableHTTPResponse(eq(stubbedRequest), anyString());
    }

    @Test
    public void shouldNotUpdateStubResponseBody_WhenResponseIsNotRecordable() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(expectedOriginalUrl);

        final String recordingSource = "htt://google.com";  //makes it non recordable
        stubs.get(0).setResponse(StubResponse.newStubResponse("200", recordingSource));
        stubRepository.resetStubsCache(stubs);

        final StubResponse expectedResponse = stubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest stubbedRequest = stubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), anyString())).thenReturn(new StubbyResponse(200, "OK, this is recorded response text!"));

        final StubResponse failedToRecordResponse = stubRepository.findStubResponseFor(stubs.get(0).getRequest());
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(failedToRecordResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    public void shouldRecordingUsingIncomingRequestQueryStringAndStubbedRecordableUrl() throws Exception {
        final StubRequest stubbedRequest =
                REQUEST_BUILDER
                        .withUrl("/search")
                        .withMethodGet()
                        .withHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                        .withQuery("queryOne", "([a-zA-Z]+)")
                        .withQuery("queryTwo", "([1-9]+)")
                        .build();
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(stubbedRequest);

        final String sourceToRecord = "http://127.0.0.1:8888";
        stubs.get(0).setResponse(StubResponse.newStubResponse("200", sourceToRecord));
        stubRepository.resetStubsCache(stubs);

        final String actualResponseText = "OK, this is recorded response text!";
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), urlToRecordCaptor.capture())).thenReturn(new StubbyResponse(200, actualResponseText));

        final StubRequest incomingRequest =
                REQUEST_BUILDER
                        .withUrl("/search")
                        .withMethodGet()
                        .withHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                        .withQuery("queryTwo", "12345")
                        .withQuery("queryOne", "arbitraryValue")
                        .build();

        final StubResponse recordedResponse = stubRepository.findStubResponseFor(incomingRequest);

        assertThat(recordedResponse.getBody()).isEqualTo(actualResponseText);
        assertThat(urlToRecordCaptor.getValue()).isEqualTo(String.format("%s%s", sourceToRecord, incomingRequest.getUrl()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotUpdateStubResponseBody_WhenResponseIsRecordableButExceptionThrown() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles(expectedOriginalUrl);

        final String recordingSource = "http://google.com";
        stubs.get(0).setResponse(StubResponse.newStubResponse("200", recordingSource));
        stubRepository.resetStubsCache(stubs);

        final StubResponse expectedResponse = stubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest matchedRequest = stubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenThrow(Exception.class);

        final StubResponse actualResponse = stubRepository.findStubResponseFor(stubs.get(0).getRequest());
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(actualResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldVerifyExpectedHttpLifeCycles_WhenRefreshingStubbedData() throws Exception {
        ArgumentCaptor<List> httpCycleCaptor = ArgumentCaptor.forClass(List.class);

        final List<StubHttpLifecycle> stubs = buildHttpLifeCycles("/resource/item/1");

        when(mockYAMLParser.parse(anyString(), any(File.class))).thenReturn(stubs);

        final StubRepository spyStubRepository = Mockito.spy(stubRepository);

        spyStubRepository.refreshStubsFromYAMLConfig(mockYAMLParser);

        verify(spyStubRepository, times(1)).resetStubsCache(httpCycleCaptor.capture());

        assertThat(httpCycleCaptor.getValue()).isEqualTo(stubs);
    }

    private List<StubHttpLifecycle> buildHttpLifeCycles(final String url) {
        final StubRequest originalRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                        .build();
        return buildHttpLifeCycles(originalRequest);
    }

    private List<StubHttpLifecycle> buildHttpLifeCycles(final StubRequest stubRequest) {
        final StubHttpLifecycle stub = new StubHttpLifecycle();
        stub.setRequest(stubRequest);
        stub.setResponse(StubResponse.newStubResponse());
        final String expectedMarshalledYaml = "This is marshalled yaml snippet";
        stub.setCompleteYAML(expectedMarshalledYaml);

        return new LinkedList<StubHttpLifecycle>() {{
            add(stub);
        }};
    }
}
