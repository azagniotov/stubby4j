package by.stub.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 7:00 PM
 */
@SuppressWarnings("serial")

public class CollectionUtilsTest {

   @Test
   public void constructParamMap_ShouldConstructParamMap_WhenQuqeryStringGiven() throws Exception {

      final Map<String, String> expected = new HashMap<String, String>() {{
         put("paramTwo", "two");
         put("paramOne", "one");
      }};

      final Map<String, String> params = CollectionUtils.constructParamMap("paramOne=one&paramTwo=two");

      Assert.assertEquals(expected, params);
   }

   @Test
   public void constructQueryString_ShouldConstructQueryString_WhenParamMapGiven() throws Exception {

      final Map<String, String> expected = new HashMap<String, String>() {{
         put("paramTwo", "two");
         put("paramOne", "one");
      }};

      final String queryString = CollectionUtils.constructQueryString(expected);

      Assert.assertEquals("paramTwo=two&paramOne=one", queryString);
   }

   @Test
   public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArray() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramOne", "[id,uuid,created,lastUpdated,displayName,email,givenName,familyName]");
      }};


      final String queryString = String.format("paramOne=%s", "%5Bid,uuid,created,lastUpdated,displayName,email,givenName,familyName%5D");
      final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

      Assert.assertEquals(expectedParams, actualParams);
   }

   @Test
   public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithQuotedElements() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramOne", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]");
      }};


      final String queryString = String.format("paramOne=%s", "%5B%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22%5D");
      final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

      Assert.assertEquals(expectedParams, actualParams);
   }
}
