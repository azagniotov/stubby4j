package io.github.azagniotov.stubby4j.server;

import io.github.azagniotov.stubby4j.caching.Cache;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StubbyManagerFactory {

    public StubbyManagerFactory() {

    }

    public synchronized StubbyManager construct(final File configFile,
                                                final Map<String, String> commandLineArgs,
                                                final CompletableFuture<YamlParseResultSet> stubLoadComputation) throws Exception {

        //TODO BUG: When stubs are cached, upon finding the previously cached match by hashCode,
        // if stubbed response has template tokens for dynamic token replacement, the tokens are
        // not replaced with values from the incoming request because the cached stub is not going
        // through the same matching process like upon the first match. Either fix this bug or just
        // deprecate the stub caching all together, as it is causing more headaches than not.
        // Also, deprecate the --disable_stub_caching command line flag if the caching has retired.
        final boolean shouldDisableStubCache = commandLineArgs.containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);
        final Cache<String, StubHttpLifecycle> stubCache = Cache.stubHttpLifecycleCache(true);

        final StubRepository stubRepository = new StubRepository(configFile, stubCache, stubLoadComputation, new StubbyHttpTransport());
        final JettyFactory jettyFactory = new JettyFactory(commandLineArgs, stubRepository);
        final Server server = jettyFactory.construct();

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_MUTE)) {
            ANSITerminal.muteConsole(true);
        }

        return new StubbyManager(commandLineArgs, server, jettyFactory, stubRepository);
    }
}
