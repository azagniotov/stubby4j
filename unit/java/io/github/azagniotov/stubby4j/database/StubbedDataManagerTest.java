package io.github.azagniotov.stubby4j.database;

import io.github.azagniotov.stubby4j.builder.stubs.StubRequestBuilder;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StubbedDataManagerTest {

   private static StubbedDataManager stubbedDataManager;
   private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();

   private StubbyHttpTransport mockStubbyHttpTransport;
   private YamlParser mockYamlParser;

   @BeforeClass
   public static void beforeClass() throws Exception {
      stubbedDataManager = new StubbedDataManager(new File("."), new LinkedList<StubHttpLifecycle>());
   }

   @Before
   public void beforeEach() throws Exception {
      mockStubbyHttpTransport = mock(StubbyHttpTransport.class);
      mockYamlParser = mock(YamlParser.class);
      stubbedDataManager.resetStubHttpLifecycles(new LinkedList<StubHttpLifecycle>());
      ReflectionUtils.injectObjectFields(stubbedDataManager, "stubbyHttpTransport", mockStubbyHttpTransport);
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();
   }

   @Test
   public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();
   }

   @Test
   public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(0);
      assertThat(matchedHttpLifecycle).isNotNull();
   }

   @Test
   public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(9999);
      assertThat(matchedHttpLifecycle).isNull();
   }

   @Test
   public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      final StubHttpLifecycle deletedHttpLifecycle = stubbedDataManager.deleteStubHttpLifecycleByIndex(0);
      assertThat(deletedHttpLifecycle).isNotNull();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void shouldDeleteOriginalHttpCycleList_WhenInvalidIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      stubbedDataManager.deleteStubHttpLifecycleByIndex(9999);
   }

   @Test
   public void shouldGetMarshalledYamlByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      final String actualMarshalledYaml = stubbedDataManager.getMarshalledYamlByIndex(0);

      assertThat(actualMarshalledYaml).isEqualTo("This is marshalled yaml snippet");
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void shouldFailToGetMarshalledYamlByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      stubbedDataManager.getMarshalledYamlByIndex(10);
   }

   @Test
   public void shouldUpdateStubHttpLifecycleByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
      final String expectedOriginalUrl = "/resource/item/1";
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      final StubRequest stubbedRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();

      assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

      final String expectedNewUrl = "/resource/completely/new";
      final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
      final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
      stubbedDataManager.updateStubHttpLifecycleByIndex(0, newStubHttpLifecycle);
      final StubRequest stubbedNewRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();

      assertThat(stubbedNewRequest.getUrl()).isEqualTo(expectedNewUrl);
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void shouldUpdateStubHttpLifecycleByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
      final String expectedOriginalUrl = "/resource/item/1";
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      final StubRequest stubbedRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();

      assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);

      final String expectedNewUrl = "/resource/completely/new";
      final List<StubHttpLifecycle> newHttpLifecycles = buildHttpLifeCycles(expectedNewUrl);
      final StubHttpLifecycle newStubHttpLifecycle = newHttpLifecycles.get(0);
      stubbedDataManager.updateStubHttpLifecycleByIndex(10, newStubHttpLifecycle);
   }

   @Test
   public void shouldUpdateStubResponseBody_WhenResponseIsRecordable() throws Exception {
      final String expectedOriginalUrl = "/resource/item/1";
      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles(expectedOriginalUrl);

      final String sourceToRecord = "http://google.com";
      originalHttpLifecycles.get(0).setResponse(StubResponse.newStubResponse("200", sourceToRecord));
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      final StubResponse expectedResponse = stubbedDataManager.getStubHttpLifecycles().get(0).getResponse(true);
      assertThat(expectedResponse.getBody()).isEqualTo(sourceToRecord);
      assertThat(expectedResponse.isRecordingRequired()).isTrue();

      final String actualResponseText = "OK, this is recorded response text!";
      final StubRequest matchedRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();
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
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      final StubResponse expectedResponse = stubbedDataManager.getStubHttpLifecycles().get(0).getResponse(true);
      assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

      final StubRequest matchedRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();
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
      stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      final StubResponse expectedResponse = stubbedDataManager.getStubHttpLifecycles().get(0).getResponse(true);
      assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

      final StubRequest matchedRequest = stubbedDataManager.getStubHttpLifecycles().get(0).getRequest();
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

      when(mockYamlParser.parse(anyString(), any(File.class))).thenReturn(originalHttpLifecycles);

      final StubbedDataManager spyStubbedDataManager = Mockito.spy(stubbedDataManager);

      spyStubbedDataManager.refreshStubbedData(mockYamlParser);

      verify(spyStubbedDataManager, times(1)).resetStubHttpLifecycles(httpCycleCaptor.capture());

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
      originalHttpLifecycle.setHttpLifeCycleAsYaml(expectedMarshalledYaml);

      return new LinkedList<StubHttpLifecycle>() {{
         add(originalHttpLifecycle);
      }};
   }
}
