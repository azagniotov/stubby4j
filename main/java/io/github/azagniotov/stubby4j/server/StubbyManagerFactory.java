/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.azagniotov.stubby4j.server;

import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.cli.EmptyLogger;
import io.github.azagniotov.stubby4j.filesystem.ExternalFilesScanner;
import io.github.azagniotov.stubby4j.filesystem.MainYamlScanner;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class StubbyManagerFactory {

    public StubbyManagerFactory() {

    }

    public synchronized StubbyManager construct(final File configFile,
                                                final Map<String, String> commandLineArgs,
                                                final Future<List<StubHttpLifecycle>> stubLoadComputation) throws Exception {

        // Commenting out the following line will configure Jetty for StdErrLog DEBUG level logging
        Log.setLog(new EmptyLogger());

        final StubRepository stubRepository = new StubRepository(configFile, stubLoadComputation);
        final JettyFactory jettyFactory = new JettyFactory(commandLineArgs, stubRepository);
        final Server server = jettyFactory.construct();

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_WATCH)) {
            final String watchValue = commandLineArgs.get(CommandLineInterpreter.OPTION_WATCH);
            final long watchScanTime = ObjectUtils.isNotNull(watchValue) ? Long.parseLong(watchValue) : 100;
            watchDataStore(stubRepository, watchScanTime);
        }

        return new StubbyManager(server, jettyFactory, stubRepository);
    }

    private void watchDataStore(final StubRepository stubRepository, final long sleepTime) {

        final MainYamlScanner mainYamlScanner = new MainYamlScanner(stubRepository, sleepTime);
        new Thread(mainYamlScanner, MainYamlScanner.class.getCanonicalName()).start();

        final ExternalFilesScanner externalFilesScanner = new ExternalFilesScanner(stubRepository, sleepTime);
        new Thread(externalFilesScanner, ExternalFilesScanner.class.getCanonicalName()).start();
    }
}
