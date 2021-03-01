package io.github.azagniotov.stubby4j.stubs;

import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class StubRepositoryTest {

    private static final File CONFIG_FILE = new File("parentPath", "childPath");

    private static final CompletableFuture<YamlParseResultSet> YAML_PARSE_RESULT_SET_FUTURE =
            CompletableFuture.completedFuture(new YamlParseResultSet(new LinkedList<>(), new HashMap<>()));

    private static final String STUB_UUID_ONE = "9136d8b7-f7a7-478d-97a5-53292484aaf6";
    private static final String STUB_UUID_TWO = "2387d97d-wlos-v907-8f9s-k2k5h4k5h365";
    private static final String STUB_UUID_THREE = "9kfhksdjhfwe-wlos-323r-3243-wekfhwek876af";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private StubbyHttpTransport mockStubbyHttpTransport;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private YamlParser mockYamlParser;


    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    @Captor
    private ArgumentCaptor<YamlParseResultSet> yamlParseResultSetCaptor;

    private StubRequest.Builder requestBuilder;
    private StubResponse.Builder responseBuilder;

    private StubRepository spyStubRepository;

    @Before
    public void beforeEach() throws Exception {
        requestBuilder = new StubRequest.Builder();
        responseBuilder = new StubResponse.Builder();

        final StubRepository stubRepository = new StubRepository(CONFIG_FILE, YAML_PARSE_RESULT_SET_FUTURE);
        final Field stubbyHttpTransportField = stubRepository.getClass().getDeclaredField("stubbyHttpTransport");
        FieldSetter.setField(stubRepository, stubbyHttpTransportField, mockStubbyHttpTransport);

        spyStubRepository = spy(stubRepository);
    }

    @Test
    public void shouldGetResourceStatsAsCsv() throws Exception {
        assertThat(spyStubRepository.getResourceStatsAsCsv()).isEqualTo("resourceId,hits\n");
    }

    @Test
    public void canMatchHttpCycleByUuid() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        spyStubRepository.resetStubsCache(yamlParseResultSet);

        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
    }

    @Test
    public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);

        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);
    }

    @Test
    public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        final Optional<StubHttpLifecycle> matchedStubOptional = spyStubRepository.matchStubByIndex(0);
        assertThat(matchedStubOptional.isPresent()).isTrue();
    }

    @Test
    public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);

        final Optional<StubHttpLifecycle> matchedStubOptional = spyStubRepository.matchStubByIndex(9999);
        assertThat(matchedStubOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();

        final StubHttpLifecycle deletedHttpLifecycle = spyStubRepository.deleteStubByIndex(0);
        assertThat(deletedHttpLifecycle).isNotNull();
        assertThat(spyStubRepository.getStubs()).isEmpty();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isFalse();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenStubsExist() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();

        spyStubRepository.deleteAllStubs();
        assertThat(spyStubRepository.getStubs()).isEmpty();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isFalse();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenStubsDontExist() throws Exception {
        spyStubRepository.deleteAllStubs();
        assertThat(spyStubRepository.getStubs()).isEmpty();
    }

    @Test
    public void shouldDeleteOriginalHttpCycleList_WhenInvalidIndexGiven() throws Exception {

        expectedException.expect(IndexOutOfBoundsException.class);

        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final boolean resetResult = spyStubRepository.resetStubsCache(yamlParseResultSet);
        assertThat(resetResult).isTrue();
        assertThat(spyStubRepository.getStubs().size()).isGreaterThan(0);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();

        spyStubRepository.deleteStubByIndex(9999);
    }

    @Test
    public void shouldDeleteStubsByUuid() throws Exception {
        final YamlParseResultSet yamlParseResultSetOne = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final YamlParseResultSet yamlParseResultSetTwo = parseYaml("/resource/item/2", STUB_UUID_TWO);
        final YamlParseResultSet yamlParseResultSetThree = parseYaml("/resource/item/3", STUB_UUID_THREE);

        // Setting state for the test
        spyStubRepository.resetStubsCache(new YamlParseResultSet(new LinkedList<StubHttpLifecycle>() {{
            addAll(yamlParseResultSetOne.getStubs());
            addAll(yamlParseResultSetTwo.getStubs());
            addAll(yamlParseResultSetThree.getStubs());
        }}, new HashMap<String, StubHttpLifecycle>() {{
            putAll(yamlParseResultSetOne.getUuidToStubs());
            putAll(yamlParseResultSetTwo.getUuidToStubs());
            putAll(yamlParseResultSetThree.getUuidToStubs());
        }}));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: deleting by second UUID (STUB_UUID_TWO)
        ///////////////////////////////////////////////////////////////////////////////////////
        assertThat(spyStubRepository.deleteStubByUuid(STUB_UUID_TWO)).isNotNull();

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(2);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        // Stubs that left, do not contain deleted URI
        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/item/2")).isFalse();
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri().equals("/resource/item/2")).isFalse();


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: deleting by third UUID (STUB_UUID_THREE)
        ///////////////////////////////////////////////////////////////////////////////////////
        assertThat(spyStubRepository.deleteStubByUuid(STUB_UUID_THREE)).isNotNull();

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(1);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isFalse();

        // Stubs that left, do not contain deleted URI
        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/item/2")).isFalse();
        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/item/3")).isFalse();


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: deleting by first UUID (STUB_UUID_ONE)
        ///////////////////////////////////////////////////////////////////////////////////////
        assertThat(spyStubRepository.deleteStubByUuid(STUB_UUID_ONE)).isNotNull();

        assertThat(spyStubRepository.getStubs().isEmpty()).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isFalse();
    }

    @Test
    public void shouldUpdateStubsByUuid() throws Exception {
        final YamlParseResultSet yamlParseResultSetOne = parseYaml("/resource/item/1", STUB_UUID_ONE);
        final YamlParseResultSet yamlParseResultSetTwo = parseYaml("/resource/item/2", STUB_UUID_TWO);
        final YamlParseResultSet yamlParseResultSetThree = parseYaml("/resource/item/3", STUB_UUID_THREE);

        // Setting state for the test
        spyStubRepository.resetStubsCache(new YamlParseResultSet(new LinkedList<StubHttpLifecycle>() {{
            addAll(yamlParseResultSetOne.getStubs());
            addAll(yamlParseResultSetTwo.getStubs());
            addAll(yamlParseResultSetThree.getStubs());
        }}, new HashMap<String, StubHttpLifecycle>() {{
            putAll(yamlParseResultSetOne.getUuidToStubs());
            putAll(yamlParseResultSetTwo.getUuidToStubs());
            putAll(yamlParseResultSetThree.getUuidToStubs());
        }}));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri()).isEqualTo("/resource/item/1");
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri()).isEqualTo("/resource/item/2");
        assertThat(spyStubRepository.getStubs().get(2).getRequest().getUri()).isEqualTo("/resource/item/3");

        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: update by second UUID (STUB_UUID_TWO)
        ///////////////////////////////////////////////////////////////////////////////////////
        spyStubRepository.updateStubByUuid(STUB_UUID_TWO,
                parseYaml("/resource/completely/new/item/2", STUB_UUID_TWO).getStubs().get(0));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/item/1")).isTrue();
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri().equals("/resource/completely/new/item/2")).isTrue();
        assertThat(spyStubRepository.getStubs().get(2).getRequest().getUri().equals("/resource/item/3")).isTrue();


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: update by third UUID (STUB_UUID_THREE)
        ///////////////////////////////////////////////////////////////////////////////////////
        spyStubRepository.updateStubByUuid(STUB_UUID_THREE,
                parseYaml("/resource/completely/new/item/3", STUB_UUID_THREE).getStubs().get(0));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/item/1")).isTrue();
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri().equals("/resource/completely/new/item/2")).isTrue();
        assertThat(spyStubRepository.getStubs().get(2).getRequest().getUri().equals("/resource/completely/new/item/3")).isTrue();


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: update by third UUID (STUB_UUID_ONE)
        ///////////////////////////////////////////////////////////////////////////////////////
        spyStubRepository.updateStubByUuid(STUB_UUID_ONE,
                parseYaml("/resource/completely/new/item/1", STUB_UUID_ONE).getStubs().get(0));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();

        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/completely/new/item/1")).isTrue();
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri().equals("/resource/completely/new/item/2")).isTrue();
        assertThat(spyStubRepository.getStubs().get(2).getRequest().getUri().equals("/resource/completely/new/item/3")).isTrue();


        ///////////////////////////////////////////////////////////////////////////////////////
        // The actual test: update by second UUID (STUB_UUID_TWO) & override with a new UUID
        ///////////////////////////////////////////////////////////////////////////////////////
        final String newUuid = "new-uuid-abc-123";
        spyStubRepository.updateStubByUuid(STUB_UUID_TWO,
                parseYaml("/resource/completely/new/item/with/new/uuid/2", newUuid).getStubs().get(0));

        assertThat(spyStubRepository.getStubs().size()).isEqualTo(3);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_THREE)).isTrue();
        assertThat(spyStubRepository.canMatchStubByUuid(newUuid)).isTrue();

        assertThat(spyStubRepository.getStubs().get(0).getRequest().getUri().equals("/resource/completely/new/item/1")).isTrue();
        assertThat(spyStubRepository.getStubs().get(1).getRequest().getUri().equals("/resource/completely/new/item/with/new/uuid/2")).isTrue();
        assertThat(spyStubRepository.getStubs().get(1).getUUID().equals(newUuid)).isTrue();
        assertThat(spyStubRepository.getStubs().get(2).getRequest().getUri().equals("/resource/completely/new/item/3")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldVerifyExpectedHttpLifeCycles_WhenRefreshingStubbedData() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);

        when(mockYamlParser.parse(anyString(), any(File.class))).thenReturn(yamlParseResultSet);

        spyStubRepository.refreshStubsFromYamlConfig(mockYamlParser);

        verify(mockYamlParser).parse(stringCaptor.capture(), fileCaptor.capture());
        verify(spyStubRepository).resetStubsCache(yamlParseResultSetCaptor.capture());

        assertThat(yamlParseResultSetCaptor.getValue()).isEqualTo(yamlParseResultSet);
        assertThat(stringCaptor.getValue()).isEqualTo(CONFIG_FILE.getParent());
        assertThat(fileCaptor.getValue()).isEqualTo(CONFIG_FILE);
    }

    @Test
    public void shouldGetMarshalledYamlByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        spyStubRepository.resetStubsCache(yamlParseResultSet);

        final String actualMarshalledYaml = spyStubRepository.getStubYamlByIndex(0);

        assertThat(actualMarshalledYaml).isEqualTo("This is marshalled yaml snippet");
    }

    @Test
    public void shouldFailToGetMarshalledYamlByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        expectedException.expect(IndexOutOfBoundsException.class);

        final YamlParseResultSet yamlParseResultSet = parseYaml("/resource/item/1", STUB_UUID_ONE);
        spyStubRepository.resetStubsCache(yamlParseResultSet);

        spyStubRepository.getStubYamlByIndex(10);
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenValidHttpCycleListIndexGiven() throws Exception {
        final String expectedOriginalUrl = "/resource/item/1";
        final YamlParseResultSet yamlParseResultSet = parseYaml(expectedOriginalUrl, STUB_UUID_ONE);
        spyStubRepository.resetStubsCache(yamlParseResultSet);
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();

        final String expectedNewUrl = "/resource/completely/new";
        final YamlParseResultSet newYamlParseResultSet = parseYaml(expectedNewUrl, STUB_UUID_TWO);
        final StubHttpLifecycle newStubHttpLifecycle = newYamlParseResultSet.getStubs().get(0);
        spyStubRepository.updateStubByIndex(0, newStubHttpLifecycle);
        final StubRequest stubbedNewRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedNewRequest.getUrl()).isEqualTo(expectedNewUrl);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isFalse();
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_TWO)).isTrue();
    }

    @Test
    public void shouldUpdateStubHttpLifecycleByIndex_WhenInvalidHttpCycleListIndexGiven() throws Exception {
        expectedException.expect(IndexOutOfBoundsException.class);

        final String expectedOriginalUrl = "/resource/item/1";
        final YamlParseResultSet yamlParseResultSet = parseYaml(expectedOriginalUrl, STUB_UUID_ONE);
        spyStubRepository.resetStubsCache(yamlParseResultSet);
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();

        assertThat(stubbedRequest.getUrl()).isEqualTo(expectedOriginalUrl);
        assertThat(spyStubRepository.canMatchStubByUuid(STUB_UUID_ONE)).isTrue();

        final String expectedNewUrl = "/resource/completely/new";
        final YamlParseResultSet newYamlParseResultSet = parseYaml(expectedNewUrl, STUB_UUID_ONE);
        final StubHttpLifecycle newStubHttpLifecycle = newYamlParseResultSet.getStubs().get(0);
        spyStubRepository.updateStubByIndex(10, newStubHttpLifecycle);
    }

    @Test
    public void shouldUpdateStubResponseBody_WhenResponseIsRecordable() throws Exception {
        final String sourceToRecord = "http://google.com";
        final String expectedOriginalUrl = "/resource/item/1";
        final YamlParseResultSet yamlParseResultSet = parseYaml(expectedOriginalUrl, responseBuilder.emptyWithBody(sourceToRecord).build(), STUB_UUID_ONE);

        spyStubRepository.resetStubsCache(yamlParseResultSet);

        final StubResponse stubbedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(stubbedResponse.getBody()).isEqualTo(sourceToRecord);
        assertThat(stubbedResponse.isRecordingRequired()).isTrue();

        final String actualResponseText = "OK, this is recorded response text!";
        final StubRequest stubbedRequest = spyStubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(stubbedRequest), anyString())).thenReturn(new StubbyResponse(200, actualResponseText));

        final List<StubHttpLifecycle> stubs = yamlParseResultSet.getStubs();
        for (int idx = 0; idx < 5; idx++) {
            doReturn(stubs.get(0).getRequest()).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
            final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
            final StubResponse recordedResponse = stubSearchResult.getMatch();

            assertThat(recordedResponse.getBody()).isEqualTo(actualResponseText);
            assertThat(recordedResponse.isRecordingRequired()).isFalse();
            assertThat(stubbedResponse.getBody()).isEqualTo(recordedResponse.getBody());
            assertThat(stubbedResponse.isRecordingRequired()).isFalse();
        }
        verify(mockStubbyHttpTransport).fetchRecordableHTTPResponse(eq(stubbedRequest), anyString());
    }

    @Test
    public void shouldNotUpdateStubResponseBody_WhenResponseIsNotRecordable() throws Exception {
        final String recordingSource = "htt://google.com";  //makes it non recordable
        final String expectedOriginalUrl = "/resource/item/1";
        final YamlParseResultSet yamlParseResultSet = parseYaml(expectedOriginalUrl,
                responseBuilder.emptyWithBody(recordingSource).build(), STUB_UUID_ONE);

        spyStubRepository.resetStubsCache(yamlParseResultSet);

        final StubResponse expectedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final List<StubHttpLifecycle> stubs = yamlParseResultSet.getStubs();
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
        final YamlParseResultSet yamlParseResultSet = parseYaml(stubbedRequest,
                responseBuilder.emptyWithBody(sourceToRecord).build(), STUB_UUID_ONE);

        spyStubRepository.resetStubsCache(yamlParseResultSet);

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
        final YamlParseResultSet yamlParseResultSet = parseYaml(expectedOriginalUrl,
                responseBuilder.emptyWithBody(recordingSource).build(), STUB_UUID_ONE);

        spyStubRepository.resetStubsCache(yamlParseResultSet);

        final StubResponse expectedResponse = spyStubRepository.getStubs().get(0).getResponse(true);
        assertThat(expectedResponse.getBody()).isEqualTo(recordingSource);

        final StubRequest matchedRequest = spyStubRepository.getStubs().get(0).getRequest();
        when(mockStubbyHttpTransport.fetchRecordableHTTPResponse(eq(matchedRequest), anyString())).thenThrow(IOException.class);

        final List<StubHttpLifecycle> stubs = yamlParseResultSet.getStubs();
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

    private YamlParseResultSet parseYaml(final String url, final String uuid) throws Exception {
        return parseYaml(url, StubResponse.okResponse(), uuid);
    }

    private YamlParseResultSet parseYaml(final String url, final StubResponse stubResponse, final String uuid) throws Exception {
        final StubRequest stubRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader("content-type", Common.HEADER_APPLICATION_JSON)
                        .build();

        return parseYaml(stubRequest, stubResponse, uuid);
    }

    private YamlParseResultSet parseYaml(final StubRequest stubRequest, final StubResponse stubResponse, final String uuid) throws Exception {
        final StubHttpLifecycle.Builder stubBuilder = new StubHttpLifecycle.Builder();
        stubBuilder.withRequest(stubRequest)
                .withResponse(stubResponse)
                .withUUID(uuid)
                .withCompleteYAML("This is marshalled yaml snippet");

        final StubHttpLifecycle stubHttpLifecycle = stubBuilder.build();

        return new YamlParseResultSet(new LinkedList<StubHttpLifecycle>() {{
            add(stubHttpLifecycle);
        }}, new HashMap<String, StubHttpLifecycle>() {{
            put(stubHttpLifecycle.getUUID(), stubHttpLifecycle);
        }});
    }
}
