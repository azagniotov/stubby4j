package unit.org.stubby.utils;

import junit.framework.Assert;
import org.junit.Test;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.stubs.StubRequest;

import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:33 AM
 */
public class ReflectionUtilsTest {

   @Test
   public void shouldGetObjectPropertiesAndValues() throws Exception {
      final int totalOfStubRequestMemberFields = 5;
      final StubRequest stubRequest = new StubRequest();
      stubRequest.setMethod("POST");
      final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);

      Assert.assertEquals(totalOfStubRequestMemberFields, properties.size());
      Assert.assertEquals("POST", properties.get("method"));
      Assert.assertEquals("Not provided", properties.get("url"));
      Assert.assertEquals("Not provided", properties.get("postbody"));
      Assert.assertEquals("{}", properties.get("headers"));
   }

   @Test
   public void shouldFindMatchingFieldInClass() throws Exception {
      final boolean result = ReflectionUtils.isFieldCorrespondsToYamlNode(StubRequest.class, "url");
      Assert.assertEquals(true, result);
   }

   @Test
   public void shouldNotFindMatchingFieldInClass() throws Exception {
      final boolean result = ReflectionUtils.isFieldCorrespondsToYamlNode(StubRequest.class, "blah");
      Assert.assertEquals(false, result);
   }

   @Test
   public void shouldSetValueOnObjectProperty() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      Assert.assertEquals(null, stubRequest.getUrl());

      ReflectionUtils.setPropertyValue(stubRequest, "url", "google.com");
      Assert.assertEquals("google.com", stubRequest.getUrl());
   }

   @Test
   public void shouldNotSetValueOnObjectProperty() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      Assert.assertEquals(null, stubRequest.getUrl());

      ReflectionUtils.setPropertyValue(stubRequest, "blah", "google.com");
      Assert.assertEquals(null, stubRequest.getUrl());
   }
}
