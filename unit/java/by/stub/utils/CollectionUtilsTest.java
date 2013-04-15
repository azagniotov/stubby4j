package by.stub.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 7:00 PM
 */
@SuppressWarnings("serial")

public class CollectionUtilsTest {

   @Test
   public void constructParamMap_ShouldConstructParamMap_WhenQuqeryStringGiven() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramTwo", "two");
         put("paramOne", "one");
      }};

      final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne=one&paramTwo=two");

      assertThat(expectedParams, is(equalTo(actualParams)));
   }

   @Test
   public void constructQueryString_ShouldConstructQueryString_WhenParamMapGiven() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramTwo", "two");
         put("paramOne", "one");
      }};

      final String actualQueryString = CollectionUtils.constructQueryString(expectedParams);
      final String expectedQueryString = "paramTwo=two&paramOne=one";

      assertThat(expectedQueryString, is(equalTo(actualQueryString)));
   }

   @Test
   public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArray() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramOne", "[id,uuid,created,lastUpdated,displayName,email,givenName,familyName]");
      }};


      final String queryString = String.format("paramOne=%s", "%5Bid,uuid,created,lastUpdated,displayName,email,givenName,familyName%5D");
      final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

      assertThat(expectedParams, is(equalTo(actualParams)));
   }

   @Test
   public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithQuotedElements() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramOne", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]");
      }};


      final String queryString = String.format("paramOne=%s", "%5B%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22%5D");
      final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

      assertThat(expectedParams, is(equalTo(actualParams)));
   }

   @Test
   public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithSingleQuoteElements() throws Exception {

      final Map<String, String> expectedParams = new HashMap<String, String>() {{
         put("paramOne", "['id','uuid','created','lastUpdated','displayName','email','givenName','familyName']");
      }};


      final String queryString = String.format("paramOne=%s", "[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]");
      final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

      assertThat(expectedParams, is(equalTo(actualParams)));
   }
}
