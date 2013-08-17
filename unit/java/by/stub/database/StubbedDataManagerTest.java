package by.stub.database;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author: Alexander Zagniotov
 * Created: 5/15/13 5:25 PM
 */
public class StubbedDataManagerTest {

   private static StubbedDataManager stubbedDataManager;
   private static final StubRequestBuilder REQUEST_BUILDER = new StubRequestBuilder();

   @BeforeClass
   public static void beforeClass() throws Exception {
      stubbedDataManager = new StubbedDataManager(new File("."), new LinkedList<StubHttpLifecycle>());
   }

   @Before
   public void beforeEach() throws Exception {
      stubbedDataManager.resetStubHttpLifecycles(new LinkedList<StubHttpLifecycle>());
   }

   @Test
   public void shouldExpungeOriginalHttpCycleList_WhenNewHttpCyclesGiven() throws Exception {

      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();

      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);

      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();
   }

   @Test
   public void shouldMatchHttplifecycle_WhenValidIndexGiven() throws Exception {

      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();

      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(0);
      assertThat(matchedHttpLifecycle).isNotNull();
   }

   @Test
   public void shouldNotMatchHttplifecycle_WhenInvalidIndexGiven() throws Exception {

      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();

      final List<StubHttpLifecycle> originalHttpLifecycles = buildHttpLifeCycles("/resource/item/1");
      final boolean resetResult = stubbedDataManager.resetStubHttpLifecycles(originalHttpLifecycles);
      assertThat(resetResult).isTrue();
      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isNotZero();

      final StubHttpLifecycle matchedHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(9999);
      assertThat(matchedHttpLifecycle).isNull();
   }

   @Test
   public void shouldDeleteOriginalHttpCycleList_WhenValidIndexGiven() throws Exception {

      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();

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

      assertThat(stubbedDataManager.getStubHttpLifecycles().size()).isZero();

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

   private List<StubHttpLifecycle> buildHttpLifeCycles(final String url) {
      final StubRequest originalRequest =
         REQUEST_BUILDER
            .withUrl(url)
            .withMethodGet()
            .withHeaders("content-type", "application/json")
            .build();
      final StubHttpLifecycle originalHttpLifecycle = new StubHttpLifecycle();
      originalHttpLifecycle.setRequest(originalRequest);
      originalHttpLifecycle.setResponse(StubResponse.newStubResponse());
      final String expectedMarshalledYaml = "This is marshalled yaml snippet";
      originalHttpLifecycle.setMarshalledYaml(expectedMarshalledYaml);

      return new LinkedList<StubHttpLifecycle>() {{
         add(originalHttpLifecycle);
      }};
   }
}
