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
   public void testIsHelpWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-h"});
      final boolean isHelp = commandLineInterpreter.isHelp();

      assertThat(isHelp).isTrue();
   }

   @Test
   public void testIsHelpWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--help"});
      final boolean isHelp = commandLineInterpreter.isHelp();

      assertThat(isHelp).isTrue();
   }


   @Test
   public void testIsMuteWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-m"});
      final boolean isMuteProvided = commandLineInterpreter.isMute();

      assertThat(isMuteProvided).isTrue();
   }

   @Test
   public void testIsMuteWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--mute"});
      final boolean isMuteProvided = commandLineInterpreter.isMute();

      assertThat(isMuteProvided).isTrue();
   }


   @Test
   public void testIsYamlProvidedWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-d", "somefilename.yaml"});
      final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

      assertThat(isYamlProvided).isTrue();
   }

   @Test
   public void testIsYamlProvidedWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml"});
      final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

      assertThat(isYamlProvided).isTrue();
   }

   @Test
   public void testtHasAdminPortWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-a", "888"});
      final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);

      assertThat(isAdmin).isTrue();
   }

   @Test
   public void testHasAdminPortWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--admin", "8888"});
      final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);

      assertThat(isAdmin).isTrue();
   }

   @Test
   public void testtHasStubsPortWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-s", "888"});
      final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);

      assertThat(isAdmin).isTrue();
   }

   @Test
   public void testtHasStubsPortWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--stubs", "8888"});
      final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);

      assertThat(isAdmin).isTrue();
   }

   @Test
   public void testtIsWatchWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-w"});
      final boolean isWatch = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);

      assertThat(isWatch).isTrue();
   }

   @Test
   public void testIsWatchWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--watch"});
      final boolean isWatch = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);

      assertThat(isWatch).isTrue();
   }

   @Test
   public void testtHasKeystoreLocationWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-k", "some/path/to/key"});
      final boolean isKeystore = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);

      assertThat(isKeystore).isTrue();
   }

   @Test
   public void testHasKeystoreLocationWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--keystore", "some/path/to/key"});
      final boolean isKeystore = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);

      assertThat(isKeystore).isTrue();
   }

   @Test
   public void testtHasLocationWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-l", "hostname"});
      final boolean isLocation = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);

      assertThat(isLocation).isTrue();
   }

   @Test
   public void testHasLocationWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--location", "hostname"});
      final boolean isLocation = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);

      assertThat(isLocation).isTrue();
   }

   @Test
   public void testtHasPasswordWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-p", "very-complex-password"});
      final boolean isPassword = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);

      assertThat(isPassword).isTrue();
   }

   @Test
   public void testHasPasswordWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--password", "very-complex-password"});
      final boolean isPassword = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);

      assertThat(isPassword).isTrue();
   }

   @Test
   public void testHasSslPortWhenShortOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-t", "2443"});
      final boolean isSslGiven = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);

      assertThat(isSslGiven).isTrue();
   }

   @Test
   public void testHasSslPortWhenLongOptionGiven() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--tls", "2443"});
      final boolean isSslGiven = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);

      assertThat(isSslGiven).isTrue();
   }


   @Test
   public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"alex", "zagniotov"});
      final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();

      assertThat(isYamlProvided).isFalse();
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineLongOptionString() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--alex"});
   }

   @Test(expected = ParseException.class)
   public void shouldFailOnInvalidCommandlineShortOptionString() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-z"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingShortOption() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"-a"});
   }

   @Test(expected = MissingArgumentException.class)
   public void shouldFailOnMissingArgumentForExistingLongOption() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--data"});
   }


   @Test
   public void shouldReturnOneCommandlineParamWhenHelpArgPresent() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--help"});
      final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isEqualTo(1);
   }

   @Test
   public void shouldReturnEmptyCommandlineParams() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{});
      final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isZero();
   }

   @Test
   public void shouldReturnCommandlineParams() throws Exception {
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(new String[]{"--data", "somefilename.yaml", "-s", "12345", "--admin", "567"});
      final Map<String, String> params = commandLineInterpreter.getCommandlineParams();

      assertThat(params.size()).isEqualTo(3);
   }
}