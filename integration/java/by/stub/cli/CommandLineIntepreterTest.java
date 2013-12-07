package by.stub.cli;

import by.stub.utils.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static by.stub.utils.FileUtils.BR;
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
      final PrintStream oldPrintStream = System.out;
      System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.printHelp();

      System.setOut(oldPrintStream);

      final String expectedConsoleOutput = "usage:" + BR +
         "       java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-h] [-k <arg>]" + BR +
         "       [-l <arg>] [-m] [-p <arg>] [-s <arg>] [-t <arg>] [-v] [-w]" + BR +
         " -a,--admin <arg>      Port for admin portal. Defaults to 8889." + BR +
         " -d,--data <arg>       Data file to pre-load endpoints. Valid YAML 1.1" + BR +
         "                       expected." + BR +
         " -h,--help             This help text." + BR +
         " -k,--keystore <arg>   Keystore file for custom TLS. By default TLS is" + BR +
         "                       enabled using internal keystore." + BR +
         " -l,--location <arg>   Hostname at which to bind stubby." + BR +
         " -m,--mute             Prevent stubby from printing to the console." + BR +
         " -p,--password <arg>   Password for the provided keystore file." + BR +
         " -s,--stubs <arg>      Port for stub portal. Defaults to 8882." + BR +
         " -t,--tls <arg>        Port for TLS connection. Defaults to 7443." + BR +
         " -v,--version          Prints out to console stubby version." + BR +
         " -w,--watch            Periodically scans for changes in last modification" + BR +
         "                       date of the main YAML and referenced external files" + BR +
         "                       (if any). The flag can accept an optional arg value" + BR +
         "                       which is the watch scan time in milliseconds. If" + BR +
         "                       milliseconds is not provided, the watch scans every" + BR +
         "                       100ms. If last modification date changed since the" + BR +
         "                       last scan period, the stub configuration is" + BR +
         "                       reloaded";

      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).isEqualTo(expectedConsoleOutput);
   }
}