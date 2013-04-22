package by.stub.utils;

import by.stub.yaml.stubs.StubRequest;
import com.google.api.client.http.HttpMethods;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:33 AM
 */

public class ReflectionUtilsTest {

   @Test
   public void shouldGetObjectPropertiesAndValues() throws Exception {
      final int totalOfStubRequestMemberFields = 6;
      final StubRequest stubRequest = new StubRequest();
      stubRequest.setMethod(HttpMethods.POST);
      final Map<String, String> properties = ReflectionUtils.getProperties(stubRequest);

      assertThat(totalOfStubRequestMemberFields).isEqualTo(properties.size());
      assertThat("[POST]").isEqualTo(properties.get("method"));
      assertThat("Not provided").isEqualTo(properties.get("url"));
      assertThat("Not provided").isEqualTo(properties.get("post"));
      assertThat("{}").isEqualTo(properties.get("headers"));
   }

   @Test
   public void shouldSetValueOnObjectProperty_WhenCorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      assertThat(stubRequest.getUrl()).isNull();

      ReflectionUtils.setPropertyValue(stubRequest, "url", "google.com");

      assertThat(stubRequest.getUrl()).isEqualTo("google.com");
   }

   @Test
   public void shouldNotSetValueOnObjectProperty_WhenIncorrectPropertyNameGiven() throws Exception {
      final StubRequest stubRequest = new StubRequest();
      assertThat(stubRequest.getUrl()).isNull();

      ReflectionUtils.setPropertyValue(stubRequest, "nonExistentProperty", "google.com");

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
