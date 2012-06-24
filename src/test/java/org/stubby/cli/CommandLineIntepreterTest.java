package org.stubby.cli;

import junit.framework.Assert;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 2:32 AM
 */
public class CommandLineIntepreterTest {

   @Test
   public void shouldBeTrueWhenYamlIsProvided() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"--config", "somefilename.yaml"});
      final boolean isYamlProvided = CommandLineIntepreter.isYamlProvided();
      Assert.assertEquals(true, isYamlProvided);
   }

   @Test
   public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"alex", "zagniotov"});
      final boolean isYamlProvided = CommandLineIntepreter.isYamlProvided();
      Assert.assertEquals(false, isYamlProvided);
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineLongOptionString() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"--alex"});
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineShortOptionString() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"-z"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingShortOption() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"-a"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingLongOption() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"--config"});
   }

   @Test
   public void testIsHelpWhenLongOptionGiven() throws Exception {
      CommandLineIntepreter.parseCommandLine(new String[]{"--help"});
      final boolean isHelp = CommandLineIntepreter.isHelp();
      Assert.assertEquals(true, isHelp);
   }
}
