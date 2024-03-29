/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.cli;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.JarUtils.readManifestImplementationVersion;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeMethodCoverageExclusion;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
    public static final String OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2 = "enable_tls_with_alpn_and_http_2";
    private static final String OPTION_VERSION = "version";
    private static final String OPTION_DEBUG = "debug";
    private static final CommandLineParser POSIX_PARSER = new DefaultParser();
    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("l", OPTION_ADDRESS, true, "Hostname at which to bind stubby.");
        OPTIONS.addOption("s", OPTION_CLIENTPORT, true, "Port for stub portal. Defaults to 8882.");
        OPTIONS.addOption("a", OPTION_ADMINPORT, true, "Port for admin portal. Defaults to 8889.");
        OPTIONS.addOption("t", OPTION_TLSPORT, true, "Port for TLS connection. Defaults to 7443.");
        OPTIONS.addOption(
                "d",
                OPTION_CONFIG,
                true,
                "Data file to pre-load endpoints. Data file to pre-load endpoints. Optional valid YAML 1.1 is expected. If YAML is not provided, you will be expected to configure stubs via the stubby4j HTTP POST API.");
        OPTIONS.addOption(
                "k",
                OPTION_KEYSTORE,
                true,
                "Keystore file for custom TLS. By default TLS is enabled using internal self-signed certificate.");
        OPTIONS.addOption("p", OPTION_KEYPASS, true, "Password for the provided keystore file.");
        OPTIONS.addOption("h", OPTION_HELP, false, "This help text.");
        OPTIONS.addOption("m", OPTION_MUTE, false, "Mute console output.");
        OPTIONS.addOption("v", OPTION_VERSION, false, "Prints out to console stubby version.");
        OPTIONS.addOption(
                "o", OPTION_DEBUG, false, "Dumps raw HTTP request to the console (if console is not muted!).");
        OPTIONS.addOption("da", OPTION_DISABLE_ADMIN, false, "Does not start Admin portal");
        OPTIONS.addOption(
                "dc",
                OPTION_DISABLE_STUB_CACHING,
                false,
                "Since v7.2.0. Disables stubs in-memory caching when stubs are successfully matched to the incoming HTTP requests");
        OPTIONS.addOption(
                "ta",
                OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2,
                false,
                "Since v7.4.0. Enables HTTP/2 over TCP (h2c) and HTTP/2 over TLS (h2) on TLS v1.2 or newer using ALPN extension");
        OPTIONS.addOption(
                "ds",
                OPTION_DISABLE_SSL,
                false,
                "Disables TLS support (enabled by default) and disables the '--enable_tls_with_alpn_and_http_2' flag, if the latter was provided");
        @SuppressWarnings("static-access")
        Option watch = Option.builder("w")
                .desc(
                        "Since v2.0.11. Periodically scans for changes in last modification date of the main YAML and referenced external files (if any). The flag can accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms. If last modification date changed since the last scan period, the stub configuration is reloaded")
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
    @GeneratedCodeMethodCoverageExclusion
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

        final Map<String, String> providedOptions = new HashMap<String, String>() {
            {
                for (final Option option : options) {
                    put(option.getLongOpt(), option.getValue());
                    final String argValue = ObjectUtils.isNull(option.getValue()) ? "" : "=" + option.getValue();
                    PROVIDED_OPTIONS.add("--" + option.getLongOpt() + argValue);
                }
            }
        };

        if (providedOptions.containsKey(OPTION_DISABLE_SSL)) {
            providedOptions.remove(OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        }

        if (PROVIDED_OPTIONS.contains("--" + OPTION_DISABLE_SSL)) {
            PROVIDED_OPTIONS.removeIf(
                    element -> element.equalsIgnoreCase("--" + OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2));
        }

        return new HashMap<>(providedOptions);
    }
}
