package io.github.azagniotov.stubby4j;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

@GeneratedCodeCoverageExclusion
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_CONFIG_FILE = "/yaml/empty-stub.yaml";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private static CommandLineInterpreter commandLineInterpreter;

    private Main() {

    }

    public static void main(String[] args) {
        commandLineInterpreter = new CommandLineInterpreter();

        //String[] args2 = new String[]{"--data", "/Users/alexanderzagniotov/yaml/stubs.yaml"};
        //parseCommandLineArgs(args2);
        parseCommandLineArgs(args);
        if (printHelpIfRequested() || printVersionIfRequested()) {
            return;
        }

        startStubby4jUsingCommandLineArgs();
    }

    private static void parseCommandLineArgs(final String[] args) {
        try {
            commandLineInterpreter.parseCommandLine(args);
        } catch (final ParseException ex) {
            final String msg =
                    String.format("Could not parse provided command line arguments, error: %s",
                            ex.toString());

            throw new IllegalArgumentException(msg);
        }
    }

    private static boolean printHelpIfRequested() {
        if (!commandLineInterpreter.isHelp()) {
            return false;
        }

        commandLineInterpreter.printHelp();

        return true;
    }

    private static boolean printVersionIfRequested() {
        if (!commandLineInterpreter.isVersion()) {
            return false;
        }

        commandLineInterpreter.printVersion();

        return true;
    }

    private static void startStubby4jUsingCommandLineArgs() {
        try {

            final long initialStart = System.currentTimeMillis();
            final Map<String, String> commandLineArgs = commandLineInterpreter.getCommandlineParams();
            final String configFilename = commandLineArgs.get(CommandLineInterpreter.OPTION_CONFIG);

            ANSITerminal.muteConsole(commandLineInterpreter.isMute());
            ConsoleUtils.enableDebug(commandLineInterpreter.isDebug());

            final File configFile = buildYamlConfigFile(configFilename);
            final CompletableFuture<YamlParseResultSet> stubLoadComputation = CompletableFuture.supplyAsync(() -> {
                try {
                    return new YamlParser().parse(configFile.getParent(), configFile);
                } catch (IOException ioEx) {
                    throw new UncheckedIOException(ioEx);
                }
            }, EXECUTOR_SERVICE);

            final StubbyManager stubbyManager = new StubbyManagerFactory().construct(configFile, commandLineArgs, stubLoadComputation);
            stubbyManager.startJetty();
            final long totalEnd = System.currentTimeMillis();

            ANSITerminal.status(String.format(BR + "stubby4j successfully started after %s milliseconds at %s", (totalEnd - initialStart), DateTimeUtils.systemDefault()) + BR);
            LOGGER.debug("stubby4j successfully started after {} milliseconds.", totalEnd - initialStart);

            stubbyManager.statuses().forEach(ANSITerminal::status);
            stubbyManager.statuses().forEach(LOGGER::debug);

            ANSITerminal.info(BR + "Quit: ctrl-c" + BR);
            LOGGER.info("Quit: ctrl-c");

        } catch (final Exception ex) {
            final String msg =
                    String.format("Could not init stubby4j, error: %s", ex.toString());

            throw new IllegalStateException(msg, ex);
        }
    }

    private static File buildYamlConfigFile(final String configFilename) throws IOException {

        if (!commandLineInterpreter.isYamlProvided()) {
            final String msg =
                    String.format("[WARNING] YAML data was not provided using command line option '--%s'." +
                                    " Is this intentional??? %s"
                                    + "To see all command line options run again with option '--%s'",
                            CommandLineInterpreter.OPTION_CONFIG, BR, CommandLineInterpreter.OPTION_HELP);

            ANSITerminal.warn(BR + msg + BR);
            LOGGER.debug("No YAML config provided upon startup. Is this intentional???");

            final File tempTargetFile = Files.createFile(Paths.get("./empty.yaml")).toFile();
            try (final InputStream inputStream = Main.class.getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                Files.copy(inputStream, tempTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            tempTargetFile.deleteOnExit();
            return tempTargetFile;
        } else {
            return new File(configFilename);
        }
    }
}
