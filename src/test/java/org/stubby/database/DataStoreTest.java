package org.stubby.database;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.NullStubResponse;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubResponse;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */
public class DataStoreTest {

   private static DataStore dataStore;

   @BeforeClass
   public static void beforeClass() throws IOException {
      final URL url = DataStoreTest.class.getResource("/httplifecycles-noheaders.yaml");
      Assert.assertNotNull(url);

      final List<StubHttpLifecycle> stubHttpLifecycles = YamlConsumer.parseYamlFile(url.getFile());
      dataStore = new DataStore();
      dataStore.setStubHttpLifecycles(stubHttpLifecycles);
   }

   @Test
   public void shouldFindHttpLifecycleForGetRequest() throws IOException {

      final String pathInfo = "/invoice/123";
      final StubResponse stubResponse = dataStore.findGetFor(pathInfo);

      Assert.assertFalse(stubResponse instanceof NullStubResponse);
   }

   @Test
   public void shouldReturnHttpLifecycleForGetRequestWithDefaultResponse() throws IOException {

      final String pathInfo = "/invoice/125";
      final StubResponse stubResponse = dataStore.findGetFor(pathInfo);

      Assert.assertTrue(stubResponse instanceof NullStubResponse);
   }


   @Test
   public void shouldFindHttpLifecycleForPostRequest() throws IOException {

      final String pathInfo = "/invoice/567";
      final String postData = "This is a post data";
      final StubResponse stubResponse = dataStore.findPostFor(pathInfo, postData);

      Assert.assertFalse(stubResponse instanceof NullStubResponse);
   }


   @Test
   public void shouldReturnHttpLifecycleForPostRequestWithDefaultResponse() throws IOException {

      final String pathInfo = "/invoice/569";
      final String postData = "This is a post data";
      final StubResponse stubResponse = dataStore.findPostFor(pathInfo, postData);

      Assert.assertTrue(stubResponse instanceof NullStubResponse);
   }
}
