package by.stub.yaml.stubs;

import by.stub.utils.StringUtils;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 10:49 AM
 */
public class StubResponseTest {

   @Test
   public void shouldReturnBody_WhenFileCannotBeFound() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setBody("this is some body");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("this is some body").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnBody_WhenFileIsNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("this is some body");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("this is some body").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(new byte[]{});
      stubResponse.setBody("this is some body");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("this is some body").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody(null);

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(new byte[]{});
      stubResponse.setBody("");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      final String expectedResponseBody = "content";
      stubResponse.setFile(StringUtils.getBytesUtf8(expectedResponseBody));
      stubResponse.setBody("something");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
   }
}
