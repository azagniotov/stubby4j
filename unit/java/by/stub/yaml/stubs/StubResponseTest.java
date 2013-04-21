package by.stub.yaml.stubs;

import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 10:49 AM
 */
public class StubResponseTest {
   @BeforeClass
   public static void Setup() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void shouldReturnBody_WhenFileCannotBeFound() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnBody_WhenFileIsNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setBody("this is some body");

      assertThat("this is some body").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody(null);

      assertThat("").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }

   @Test
   public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("");

      assertThat("").isEqualTo(StringUtils.newStringUtf8(stubResponse.getResponseBody()));
   }
}
