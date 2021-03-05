package io.github.azagniotov.stubby4j.cli;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.JarUtils.readManifestImplementationVersion;

public final class CommandLineInterpreter {

    public static final List<String> PROVIDED_OPTIONS = Collections.synchronizedList(new LinkedList<>());
    public static final String OPTION_ADDRESS = "location";
    public static final String OPTION_CLIENTPORT = "stubs";
    public static final String OPTION_TLSPORT = "tls";
    public static final String OPTION_ADMINPORT = "admin";
    public static final String OPTION_CONFIG = "data";
    public static final String OPTION_KEYSTORE = "keystore";
    public static final String OPTION_KEYPASS = "password";
    public static final String OPTION_MUTE = "mute";
    public static final String OPTION_WATCH = "watch";
    public static final String OPTION_HELP = "help";
    public static final String OPTION_DISABLE_ADMIN = "disable_admin_portal";
    public static final String OPTION_DISABLE_SSL = "disable_ssl";
    public static final String OPTION_DISABLE_STUB_CACHING = "disable_stub_caching";
    private static final String OPTION_VERSION = "version";
    private static final String OPTION_DEBUG = "debug";
    private static final CommandLineParser POSIX_PARSER = new DefaultParser();
    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("l", OPTION_ADDRESS, true, "Hostname at which to bind stubby.");
        OPTIONS.addOption("s", OPTION_CLIENTPORT, true, "Port for stub portal. Defaults to 8882.");
        OPTIONS.addOption("a", OPTION_ADMINPORT, true, "Port for admin portal. Defaults to 8889.");
        OPTIONS.addOption("t", OPTION_TLSPORT, true, "Port for TLS connection. Defaults to 7443.");
        OPTIONS.addOption("d", OPTION_CONFIG, true, "Data file to pre-load endpoints. Data file to pre-load endpoints. Optional valid YAML 1.1 is expected. If YAML is not provided, you will be expected to configure stubs via the stubby4j HTTP POST API.");
        OPTIONS.addOption("k", OPTION_KEYSTORE, true, "Keystore file for custom TLS. By default TLS is enabled using internal keystore.");
        OPTIONS.addOption("p", OPTION_KEYPASS, true, "Password for the provided keystore file.");
        OPTIONS.addOption("h", OPTION_HELP, false, "This help text.");
        OPTIONS.addOption("m", OPTION_MUTE, false, "Mute console output.");
        OPTIONS.addOption("v", OPTION_VERSION, false, "Prints out to console stubby version.");
        OPTIONS.addOption("o", OPTION_DEBUG, false, "Dumps raw HTTP request to the console (if console is not muted!).");
        OPTIONS.addOption("da", OPTION_DISABLE_ADMIN, false, "Does not start Admin portal");
        OPTIONS.addOption("dc", OPTION_DISABLE_STUB_CACHING, false, "Disables stubs in-memory caching when stubs are successfully matched to the incoming HTTP requests");
        OPTIONS.addOption("ds", OPTION_DISABLE_SSL, false, "Does not enable SSL connections");
        @SuppressWarnings("static-access")
        Option watch =
                Option.builder("w")
                        .desc("Periodically scans for changes in last modification date of the main YAML and referenced external files (if any). The flag can accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms. If last modification date changed since the last scan period, the stub configuration is reloaded")
                        .longOpt(OPTION_WATCH)
                        .hasArg(true)
                        .optionalArg(true)
                        .build();
        OPTIONS.addOption(watch);
    }

    private CommandLine line;

    public void parseCommandLine(final String[] args) throws ParseException {
        line = POSIX_PARSER.parse(OPTIONS, args);
    }

    /**
     * Checks if console output has been muted
     *
     * @return true if the user disabled output to console using command line arg
     */
    public boolean isMute() {
        return line.hasOption(OPTION_MUTE);
    }

    /**
     * Checks if data YAML was provided
     *
     * @return true if the user provided stub data as YAML file with using command line arg
     */
    public boolean isYamlProvided() {
        return line.hasOption(OPTION_CONFIG);
    }

    /**
     * Checks if help option was provided
     *
     * @return true if the user has provided 'help' command line arg
     */
    public boolean isHelp() {
        return line.hasOption(OPTION_HELP);
    }

    /**
     * Checks if version option was provided
     *
     * @return true if the user has provided 'version' command line arg
     */
    public boolean isVersion() {
        return line.hasOption(OPTION_VERSION);
    }

    /**
     * Checks if debug option was provided
     *
     * @return true if the user has provided 'debug' command line arg
     */
    public boolean isDebug() {
        return line.hasOption(OPTION_DEBUG);
    }

    /**
     * Prints 'help' message which describes available command line arguments
     */
    public void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        final String command = String.format("%sjava -jar stubby4j-x.x.xx.jar", BR);
        formatter.printHelp(command, OPTIONS, true);
    }

    /**
     * Prints current stubby4j version to the console
     */
    @GeneratedCodeCoverageExclusion
    public void printVersion() {
        final HelpFormatter formatter = new HelpFormatter();
        try (final PrintWriter printWriter = new PrintWriter(System.out)) {
            formatter.printWrapped(printWriter, HelpFormatter.DEFAULT_WIDTH, readManifestImplementationVersion());
            printWriter.flush();
        }
    }


    /**
     * Identifies what command line arguments that have been passed by user are matching available options
     *
     * @return a map of passed command line arguments where key is the name of the argument
     */
    public Map<String, String> getCommandlineParams() {

        final Option[] options = line.getOptions();

        return new HashMap<String, String>() {{
            for (final Option option : options) {
                put(option.getLongOpt(), option.getValue());
                final String argValue = ObjectUtils.isNull(option.getValue()) ? "" : "=" + option.getValue();
                PROVIDED_OPTIONS.add("--" + option.getLongOpt() + argValue);
            }
        }};
    }
}
