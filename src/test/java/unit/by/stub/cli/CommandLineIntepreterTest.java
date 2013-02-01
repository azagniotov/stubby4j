package unit.by.stub.cli;

import by.stub.cli.CommandLineInterpreter;
import by.stub.testing.junit.categories.UnitTest;
import junit.framework.Assert;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 2:32 AM
 */
@Category(UnitTest.class)
public class CommandLineIntepreterTest {

   @Test
   public void shouldBeTrueWhenYamlIsProvided() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml"});
      final boolean isYamlProvided = CommandLineInterpreter.isYamlProvided();
      Assert.assertEquals(true, isYamlProvided);
   }

   @Test
   public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"alex", "zagniotov"});
      final boolean isYamlProvided = CommandLineInterpreter.isYamlProvided();
      Assert.assertEquals(false, isYamlProvided);
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineLongOptionString() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--alex"});
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineShortOptionString() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"-z"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingShortOption() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"-a"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingLongOption() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--data"});
   }

   @Test
   public void testIsHelpWhenLongOptionGiven() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--help"});
      final boolean isHelp = CommandLineInterpreter.isHelp();
      Assert.assertEquals(true, isHelp);
   }

   @Test
   public void shouldReturnOneCommandlineParamWhenHelpArgPresent() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--help"});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();
      Assert.assertEquals(1, params.size());
   }

   @Test
   public void shouldReturnEmptyCommandlineParams() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();
      Assert.assertEquals(0, params.size());
   }

   @Test
   public void shouldReturnCommandlineParams() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml", "-s", "12345", "--admin", "567"});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();
      Assert.assertEquals(3, params.size());
   }

   @Test
   public void shouldGetCurrentJarLocation() throws Exception {
      final String currentJarLocation = CommandLineInterpreter.getCurrentJarLocation(CommandLineInterpreter.class);
      Assert.assertEquals("stubby4j-x.x.x-SNAPSHOT.jar", currentJarLocation);
   }
}
