package io.github.azagniotov.stubby4j.cli;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;


public class CommandLineInterpreterTest {


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
                "java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-dc] [-ds] [-h]" + BR +
                "       [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t <arg>]" + BR +
                "       [-v] [-w <arg>]" + BR +
                " -a,--admin <arg>             Port for admin portal. Defaults to 8889." + BR +
                " -d,--data <arg>              Data file to pre-load endpoints. Data file" + BR +
                "                              to pre-load endpoints. Optional valid YAML" + BR +
                "                              1.1 is expected. If YAML is not provided," + BR +
                "                              you will be expected to configure stubs via" + BR +
                "                              the stubby4j HTTP POST API." + BR +
                " -da,--disable_admin_portal   Does not start Admin portal" + BR +
                " -dc,--disable_stub_caching   Disables stubs in-memory caching when stubs" + BR +
                "                              are successfully matched to the incoming" + BR +
                "                              HTTP requests" + BR +
                " -ds,--disable_ssl            Does not enable SSL connections" + BR +
                " -h,--help                    This help text." + BR +
                " -k,--keystore <arg>          Keystore file for custom TLS. By default TLS" + BR +
                "                              is enabled using internal keystore." + BR +
                " -l,--location <arg>          Hostname at which to bind stubby." + BR +
                " -m,--mute                    Mute console output." + BR +
                " -o,--debug                   Dumps raw HTTP request to the console (if" + BR +
                "                              console is not muted!)." + BR +
                " -p,--password <arg>          Password for the provided keystore file." + BR +
                " -s,--stubs <arg>             Port for stub portal. Defaults to 8882." + BR +
                " -t,--tls <arg>               Port for TLS connection. Defaults to 7443." + BR +
                " -v,--version                 Prints out to console stubby version." + BR +
                " -w,--watch <arg>             Periodically scans for changes in last" + BR +
                "                              modification date of the main YAML and" + BR +
                "                              referenced external files (if any). The flag" + BR +
                "                              can accept an optional arg value which is" + BR +
                "                              the watch scan time in milliseconds. If" + BR +
                "                              milliseconds is not provided, the watch" + BR +
                "                              scans every 100ms. If last modification date" + BR +
                "                              changed since the last scan period, the stub" + BR +
                "                              configuration is reloaded";

        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).isEqualTo(expectedConsoleOutput);
    }
}