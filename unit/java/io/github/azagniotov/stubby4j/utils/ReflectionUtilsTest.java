package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import com.google.api.client.http.HttpMethods;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:33 AM
 */

public class ReflectionUtilsTest {

   @Test
   public void shouldGetObjectPropertiesAndValues() throws Exception {
      final StubRequest stubRequest = StubRequest.newStubRequest();
      stubRequest.addMethod(HttpMethods.POST);
      final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);

      assertThat("[POST]").isEqualTo(properties.get("method"));
      assertThat(properties.get("url")).isNull();
      assertThat(properties.get("post")).isNull();
      assertThat(properties.get("headers")).isNull();
   }

   @Test
   public void shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = StubRequest.newStubRequest();
      assertThat(stubRequest.getUrl()).isNull();

      final Map<String, Object> values = new HashMap<String, Object>();
      values.put("url", "google.com");
      ReflectionUtils.injectObjectFields(stubRequest, values);

      assertThat(stubRequest.getUrl()).isEqualTo("google.com");
   }

   @Test
   public void shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = StubRequest.newStubRequest();
      assertThat(stubRequest.getUrl()).isNull();

      final Map<String, Object> values = new HashMap<String, Object>();
      values.put("nonExistentProperty", "google.com");
      ReflectionUtils.injectObjectFields(stubRequest, values);

      assertThat(stubRequest.getUrl()).isNull();
   }

   @Test
   public void shouldReturnNullWhenClassHasNoDeclaredMethods() throws Exception {

      final Object result = ReflectionUtils.getPropertyValue(new MethodelessInterface() {
      }, "somePropertyName");

      assertThat(result).isNull();
   }

   @Test
   public void shouldReturnPropertyValueWhenClassHasDeclaredMethods() throws Exception {

      final String expectedMethodValue = "alex";
      final Object result = ReflectionUtils.getPropertyValue(new MethodfullInterface() {
         @Override
         public String getName() {
            return expectedMethodValue;
         }
      }, "name");

      assertThat(result).isEqualTo(expectedMethodValue);
   }

   private static interface MethodelessInterface {

   }

   ;

   private static interface MethodfullInterface {
      String getName();
   }

   ;
}
