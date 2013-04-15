package by.stub.cli;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 2:32 AM
 */

public class CommandLineIntepreterTest {

   @Test
   public void shouldBeTrueWhenYamlIsProvided() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml"});
      final boolean isYamlProvided = CommandLineInterpreter.isYamlProvided();

      assertThat(isYamlProvided).isTrue();
   }

   @Test
   public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"alex", "zagniotov"});
      final boolean isYamlProvided = CommandLineInterpreter.isYamlProvided();

      assertThat(isYamlProvided).isFalse();
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

      assertThat(isHelp).isTrue();
   }

   @Test
   public void shouldReturnOneCommandlineParamWhenHelpArgPresent() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--help"});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isEqualTo(1);
   }

   @Test
   public void shouldReturnEmptyCommandlineParams() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isZero();
   }

   @Test
   public void shouldReturnCommandlineParams() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml", "-s", "12345", "--admin", "567"});
      final Map<String, String> params = CommandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isEqualTo(3);
   }
}