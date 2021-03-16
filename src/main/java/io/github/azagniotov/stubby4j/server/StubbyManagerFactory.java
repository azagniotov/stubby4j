package io.github.azagniotov.stubby4j.server;

import io.github.azagniotov.stubby4j.caching.Cache;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.cli.EmptyLogger;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StubbyManagerFactory {

    public StubbyManagerFactory() {

    }

    public synchronized StubbyManager construct(final File configFile,
                                                final Map<String, String> commandLineArgs,
                                                final CompletableFuture<YamlParseResultSet> stubLoadComputation) throws Exception {

        // Commenting out the following line will configure Jetty for StdErrLog DEBUG level logging
        Log.setLog(new EmptyLogger());

        final boolean shouldDisableStubCache = commandLineArgs.containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);
        final Cache<String, StubHttpLifecycle> stubCache = Cache.stubHttpLifecycleCache(shouldDisableStubCache);

        final StubRepository stubRepository = new StubRepository(configFile, stubCache, stubLoadComputation, new StubbyHttpTransport());
        final JettyFactory jettyFactory = new JettyFactory(commandLineArgs, stubRepository);
        final Server server = jettyFactory.construct();

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_MUTE)) {
            ANSITerminal.muteConsole(true);
        }

        return new StubbyManager(commandLineArgs, server, jettyFactory, stubRepository);
    }
}
