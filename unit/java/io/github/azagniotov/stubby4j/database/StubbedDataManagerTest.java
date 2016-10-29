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
public class StubbedDataManagerTest {

    private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();
    private static final File CONFIG_FILE = new File(".");

    private static final Future<List<StubHttpLifecycle>> COMPLETED_FUTURE =
            CompletableFuture.completedFuture(new LinkedList<StubHttpLifecycle>());

    @Mock
    private StubbyHttpTransport mockStubbyHttpTransport;

    @Mock
    private YAMLParser mockYAMLParser;

    private StubbedDataManager stubbedDataManager;

    @Before
    public void beforeEach() throws Exception {
        stubbedDataManager = new StubbedDataManager(CONFIG_FILE, COMPLETED_FUTURE);
        ReflectionUtils.injectObjectFields(stubbedDataManager, "stubbyHttpTransport", mockStubbyHttpTransport);
    }

    @Test
    public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        assertThat(resetResult).isTrue();
        assertThat(stubbedDataManager.getStubs().size()).isNotZero();
    }

    @Test
    public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        assertThat(resetResult).isTrue();
        assertThat(stubbedDataManager.getStubs().size()).isNotZero();

        final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.matchStubByIndex(0);
        assertThat(matchedHttpLifecycle).isNotNull();
    }

    @Test
    public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        assertThat(resetResult).isTrue();
        assertThat(stubbedDataManager.getStubs().size()).isNotZero();

        final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.matchStubByIndex(9999);
        assertThat(matchedHttpLifecycle).isNull();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        assertThat(resetResult).isTrue();
        assertThat(stubbedDataManager.getStubs().size()).isNotZero();

        final StubHttpLifecycle deletedHttpLifecycle = stubbedDataManager.deleteStubByIndex(0);
        assertThat(deletedHttpLifecycle).isNotNull();
        assertThat(stubbedDataManager.getStubs().size()).isZero();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldDeleteOriginalHttpCycleList_WhenInvalidIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        final boolean resetResult = stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        assertThat(resetResult).isTrue();
        assertThat(stubbedDataManager.getStubs().size()).isNotZero();

        stubbedDataManager.deleteStubByIndex(9999);
    }

    @Test
    public void shouldGetMarshalledYamlByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        final String actualMarshalledYaml = stubbedDataManager.getStubYAMLByIndex(0);

        assertThat(actualMarshalledYaml).isEqualTo("This is marshalled yaml snippet");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToGetMarshalledYamlByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        stubbedDataManager.getStubYAMLByIndex(10);
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        final StubRequest stubbedRequest = stubbedDataManager.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        stubbedDataManager.updateStubByIndex(0, newStubHttpLifecycle);
        final StubRequest stubbedNewRequest = stubbedDataManager.getStubs().get(0).getRequest();

        assertThat(stubbedNewRequest.getUrl()).isEqualTo(expectedNewUrl);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldUpdateStubHttpLifecycleByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);
        final StubRequest stubbedRequest = stubbedDataManager.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

        final String expectedNewUrl = "/resource/completely/new";
        final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
        final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
        stubbedDataManager.updateStubByIndex(10, newStubHttpLifecycle);
    }

    @Test
    public void shouldUpdateStubResponseBody_WhenResponseIsRecordable() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);

        final String sourceToRecord = "http://google.com";
        originalHttpLifecycles.get(0).setResponse(StubResponse.newStubResponse("200", sourceToRecord));
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        final StubResponse expectedResponse = stubbedDataManager.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(sourceToRecord);
        assertThat(expectedResponse.isRecordingRequired()).isTrue();

        final String actualResponseText = "OK, this is recorded response text!";
        final StubRequest matchedRequest = stubbedDataManager.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenReturn(new StubbyResponse(200, actualResponseText));

        for (int idx = 0; idx < 5; idx++) {
            final StubResponse actualResponse = stubbedDataManager.findStubResponseFor(originalHttpLifecycles.get(0).getRequest());

            assertThat(actualResponse.getBody()).isEqualTo(actualResponseText);
            assertThat(expectedResponse.getBody()).isEqualTo(actualResponse.getBody());
            assertThat(expectedResponse.isRecordingRequired()).isFalse();
            assertThat(actualResponse.isRecordingRequired()).isFalse();
        }
        verify(mockStubbyHttpTransport, times(1)).fetchRecordableHTTPResponse(eq(matchedRequest), anyString());
    }

    @Test
    public void shouldNotUpdateStubResponseBody_WhenResponseIsNotRecordable() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);

        final String recordingSource = "htt://google.com";  //makes it non recordable
        originalHttpLifecycles.get(0).setResponse(StubResponse.newStubResponse("200", recordingSource));
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        final StubResponse expectedResponse = stubbedDataManager.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest matchedRequest = stubbedDataManager.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenReturn(new StubbyResponse(200, "OK, this is recorded response text!"));

        final StubResponse actualResponse = stubbedDataManager.findStubResponseFor(originalHttpLifecycles.get(0).getRequest());
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(actualResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotUpdateStubResponseBody_WhenResponseIsRecordableButExceptionThrown() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);

        final String recordingSource = "http://google.com";
        originalHttpLifecycles.get(0).setResponse(StubResponse.newStubResponse("200", recordingSource));
        stubbedDataManager.resetStubsCache(originalHttpLifecycles);

        final StubResponse expectedResponse = stubbedDataManager.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest matchedRequest = stubbedDataManager.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenThrow(Exception.class);

        final StubResponse actualResponse = stubbedDataManager.findStubResponseFor(originalHttpLifecycles.get(0).getRequest());
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);
        assertThat(actualResponse.getBody()).isEqualTo(recordingSource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldVerifyExpectedHttpLifeCycles_WhenRefreshingStubbedData() throws Exception {
        ArgumentCaptor<List> httpCycleCaptor = ArgumentCaptor.forClass(List.class);

        final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");

        when(mockYAMLParser.parse(anyString(), any(File.class))).thenReturn(originalHttpLifecycles);

        final StubbedDataManager spyStubbedDataManager = Mockito.spy(stubbedDataManager);

        spyStubbedDataManager.refreshStubsFromYAMLConfig(mockYAMLParser);

        verify(spyStubbedDataManager, times(1)).resetStubsCache(httpCycleCaptor.capture());

        assertThat(httpCycleCaptor.getValue()).isEqualTo(originalHttpLifecycles);
    }

    private List<StubHttpLifecycle> buildHttpLifeCycles(final String url) {
        final StubRequest originalRequest =
                REQUEST_BUILDER
                        .withUrl(url)
                        .withMethodGet()
                        .withHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                        .build();
        final StubHttpLifecycle originalHttpLifecycle = new StubHttpLifecycle();
        originalHttpLifecycle.setRequest(originalRequest);
        originalHttpLifecycle.setResponse(StubResponse.newStubResponse());
        final String expectedMarshalledYaml = "This is marshalled yaml snippet";
        originalHttpLifecycle.setCompleteYAML(expectedMarshalledYaml);

        return new LinkedList<StubHttpLifecycle>() {{
            add(originalHttpLifecycle);
        }};
    }
}
