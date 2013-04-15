package by.stub.utils;

import by.stub.yaml.stubs.StubRequest;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:33 AM
 */

public class ReflectionUtilsTest {

   @Test
   public void shouldGetObjectPropertiesAndValues() throws Exception {
      final int totalOfStubRequestMemberFields = 6;
      final StubRequest stubRequest = new StubRequest();
      stubRequest.setMethod("POST");
      final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);

      assertThat(totalOfStubRequestMemberFields, is(equalTo(properties.size())));
      assertThat("[POST]", is(equalTo(properties.get("method"))));
      assertThat("Not provided", is(equalTo(properties.get("url"))));
      assertThat("Not provided", is(equalTo(properties.get("post"))));
      assertThat("{}", is(equalTo(properties.get("headers"))));
   }

   @Test
   public void shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      assertThat(stubRequest.getUrl(), is(nullValue()));

      ReflectionUtils.setPropertyValue(stubRequest, "url", "google.com");

      assertThat(stubRequest.getUrl(), is(equalTo("google.com")));
   }

   @Test
   public void shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      assertThat(stubRequest.getUrl(), is(nullValue()));

      ReflectionUtils.setPropertyValue(stubRequest, "nonExistentProperty", "google.com");

      assertThat(stubRequest.getUrl(), is(nullValue()));
   }
}
