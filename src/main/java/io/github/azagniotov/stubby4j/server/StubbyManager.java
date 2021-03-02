package io.github.azagniotov.stubby4j.server;


import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.filesystem.ExternalFilesScanner;
import io.github.azagniotov.stubby4j.filesystem.MainIncludedYamlScanner;
import io.github.azagniotov.stubby4j.filesystem.MainYamlScanner;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@GeneratedCodeCoverageExclusion
public final class StubbyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubbyManager.class);

    private final Map<String, String> commandLineArgs;
    private final Server server;
    private final JettyFactory jettyFactory;
    private final StubRepository stubRepository;

    StubbyManager(final Map<String, String> commandLineArgs, final Server server, final JettyFactory jettyFactory, final StubRepository stubRepository) {
        this.commandLineArgs = commandLineArgs;
        this.server = server;
        this.jettyFactory = jettyFactory;
        this.stubRepository = stubRepository;
    }

    public synchronized void startJetty() throws Exception {
        if (isJettyStarting() || isJettyUp()) {
            return;
        }

        server.start();
        while (!isJettyUp()) {
            ANSITerminal.warn("Waiting for Jetty to finish starting up..");
            LOGGER.warn("Waiting for Jetty to finish starting up..");
            Thread.sleep(250);
        }
        stubRepository.retrieveLoadedStubs();

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_WATCH)) {
            final String watchValue = commandLineArgs.get(CommandLineInterpreter.OPTION_WATCH);
            final long watchScanTime = ObjectUtils.isNotNull(watchValue) ? Long.parseLong(watchValue) : 100;
            watchDataStore(stubRepository, watchScanTime);
        }
    }

    public synchronized void stopJetty() throws Exception {
        if (isJettyStopping() || isJettyDown()) {
            return;
        }

        server.stop();

        while (!isJettyDown()) {
            ANSITerminal.warn("Waiting for Jetty to finish shutting down..");
            LOGGER.warn("Waiting for Jetty to finish shutting down..");
            Thread.sleep(250);
        }

        ANSITerminal.status("Jetty successfully shutdown");
        LOGGER.info("Jetty successfully shutdown.");
    }

    public synchronized void joinJetty() throws Exception {
        server.join();
    }

    public List<String> statuses() {
        return jettyFactory.getStatuses();
    }

    private boolean isJettyStarting() {
        return server.isStarting();
    }

    private boolean isJettyUp() {
        return (server.isStarted() && server.isRunning());
    }

    private boolean isJettyStopping() {
        return server.isStopping();
    }

    private boolean isJettyDown() {
        return (server.isStopped() && !server.isRunning());
    }

    private void watchDataStore(final StubRepository stubRepository, final long sleepTime) {

        final MainYamlScanner mainYamlScanner = new MainYamlScanner(stubRepository, sleepTime);
        new Thread(mainYamlScanner, MainYamlScanner.class.getCanonicalName()).start();

        final ExternalFilesScanner externalFilesScanner = new ExternalFilesScanner(stubRepository, sleepTime + 25);
        new Thread(externalFilesScanner, ExternalFilesScanner.class.getCanonicalName()).start();

        final MainIncludedYamlScanner mainIncludedYamlScanner = new MainIncludedYamlScanner(stubRepository, sleepTime + 50);
        new Thread(mainIncludedYamlScanner, MainIncludedYamlScanner.class.getCanonicalName()).start();
    }
}