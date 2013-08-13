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
   public void shouldReturnBody_WhenFileIsNull() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("200", "this is some body");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("this is some body").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse("200", "this is some body", new byte[] {}, null, null);

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("this is some body").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("200", null);

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("200", "");

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse("200", "", new byte[] {}, null, null);

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat("").isEqualTo(actualResponseBody);
   }

   @Test
   public void shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws Exception {

      final String expectedResponseBody = "content";
      final StubResponse stubResponse = new StubResponse("200", "something", StringUtils.getBytesUtf8(expectedResponseBody), null, null);

      final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBody());
      assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
   }
}
