package by.stub.cli;

import by.stub.utils.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 2:32 AM
 */

public class CommandLineIntepreterTest {


   @Test
   public void consolePrintedHelpMessageShouldBeAsExpected() throws Exception {

      final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
      final boolean NO_AUTO_FLUSH = false;
      System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.printHelp();

      System.setOut(System.out);

      final String expectedConsoleOutput = "usage:\n" +
         "       java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-h] [-k <arg>]\n" +
         "       [-l <arg>] [-m] [-p <arg>] [-s <arg>] [-t <arg>] [-w]\n" +
         " -a,--admin <arg>      Port for admin portal. Defaults to 8889.\n" +
         " -d,--data <arg>       Data file to pre-load endpoints. Valid YAML 1.1\n" +
         "                       expected.\n" +
         " -h,--help             This help text.\n" +
         " -k,--keystore <arg>   Keystore file for custom SSL. By default SSL is\n" +
         "                       enabled using internal keystore.\n" +
         " -l,--location <arg>   Hostname at which to bind stubby.\n" +
         " -m,--mute             Prevent stubby from printing to the console.\n" +
         " -p,--password <arg>   Password for the provided keystore file.\n" +
         " -s,--stubs <arg>      Port for stub portal. Defaults to 8882.\n" +
         " -t,--ssl <arg>        Port for SSL connection. Defaults to 7443.\n" +
         " -w,--watch            Reload datafile when changes are made.";

      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).isEqualTo(expectedConsoleOutput);
   }
}