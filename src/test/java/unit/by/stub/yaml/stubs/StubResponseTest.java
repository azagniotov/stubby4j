package unit.by.stub.yaml.stubs;

import by.stub.cli.CommandLineInterpreter;
import by.stub.testing.junit.categories.UnitTest;
import by.stub.utils.IOUtils;
import by.stub.yaml.stubs.StubResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 10:49 AM
 */
@Category(UnitTest.class)
public class StubResponseTest {
   @BeforeClass
   public static void Setup() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void getResponseBody_ShouldReturnBody_WhenFileCannotBeFound() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile("/path/to/nowhere");
      stubResponse.setBody("this is some body");

      Assert.assertEquals("this is some body", stubResponse.getResponseBody());
   }

   @Test
   public void getResponseBody_ShouldReturnBody_WhenFileIsNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("this is some body");

      Assert.assertEquals("this is some body", stubResponse.getResponseBody());
   }

   @Test
   public void getResponseBody_ShouldReturnBody_WhenFileIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile("");
      stubResponse.setBody("this is some body");

      Assert.assertEquals("this is some body", stubResponse.getResponseBody());
   }

   @Test
   public void getResponseBody_ShouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody(null);

      Assert.assertEquals("", stubResponse.getResponseBody());
   }

   @Test
   public void getResponseBody_ShouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

      final StubResponse stubResponse = new StubResponse();
      stubResponse.setFile(null);
      stubResponse.setBody("");

      Assert.assertEquals("", stubResponse.getResponseBody());
   }
}
