package unit.by.stub.utils;

import by.stub.testing.categories.UnitTests;
import by.stub.utils.CollectionUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 7:00 PM
 */
@SuppressWarnings("serial")
@Category(UnitTests.class)
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
}
