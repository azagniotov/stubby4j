package org.stubby.database;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/20/12, 5:27 PM
 */
public class RepositoryIntegrationTest {

   private static Repository repository;

   @BeforeClass
   public static void beforeClass() throws IOException {
      final URL url = RepositoryIntegrationTest.class.getResource("/httplifecycles-noheaders.yaml");
      Assert.assertNotNull(url);

      final List<StubHttpLifecycle> stubHttpLifecycles = new YamlConsumer(url.getFile()).parseYaml();
      repository = new Repository(stubHttpLifecycles);
      repository.init();
   }

   @Test
   public void testGetHttpConfigData_ShouldContainExpectedRequestsAndResponses() throws Exception {

      List<List<Map<String, Object>>> listOfRows = repository.getHttpConfigData();

      final int totalRequestAndResponses = 4;
      Assert.assertEquals(totalRequestAndResponses, listOfRows.size());

      List<Map<String, Object>> requests = listOfRows.get(0);
      int totalRequests = 2;
      Assert.assertEquals(totalRequests, requests.size());

      List<Map<String, Object>> responses = listOfRows.get(2);
      Assert.assertEquals(totalRequests, responses.size());
   }

   @Test
   public void testGetHttpConfigData_ShouldContainExpectedRequestsParams() throws Exception {

      List<List<Map<String, Object>>> listOfRows = repository.getHttpConfigData();

      List<Map<String, Object>> requests = listOfRows.get(0);

      Map<String, Object> requestOneColumns = requests.get(0);
      Assert.assertEquals("GET", requestOneColumns.get("METHOD"));
      Assert.assertEquals("/invoice/123", requestOneColumns.get("URL"));

      Map<String, Object> requestTwoColumns = requests.get(1);
      Assert.assertEquals("GET", requestTwoColumns.get("METHOD"));
      Assert.assertEquals("/invoice/567", requestTwoColumns.get("URL"));
   }


   @Test
   public void testGetHttpConfigData_ShouldContainExpectedResponseParams() throws Exception {

      List<List<Map<String, Object>>> listOfRows = repository.getHttpConfigData();

      List<Map<String, Object>> responses = listOfRows.get(2);

      Map<String, Object> responseOneColumns = responses.get(0);
      Assert.assertEquals("200", responseOneColumns.get("STATUS"));
      Assert.assertEquals("This is a response for 123", responseOneColumns.get("BODY"));

      Map<String, Object> responseTwoColumns = responses.get(1);
      Assert.assertEquals("503", responseTwoColumns.get("STATUS"));
      Assert.assertEquals("This is a response for 567", responseTwoColumns.get("BODY"));
   }

   @Test
   public void testRetrieveResponseFor_ShouldGetResponseForRequest() throws Exception {

      Map<String, String> response = repository.retrieveResponseFor("/invoice/123", "GET", null);

      Assert.assertEquals("200", response.get("STATUS"));
      Assert.assertEquals("This is a response for 123", response.get("BODY"));
   }

   @Test
   public void testRetrieveResponseFor_ShouldNotGetResponseForRequest() throws Exception {

      Map<String, String> response = repository.retrieveResponseFor("/invoice/123", "POST", null);

      Assert.assertEquals(1, response.size());
      Assert.assertEquals("No data found for POST request at URI /invoice/123", response.get("DEFAULT"));
   }
}
