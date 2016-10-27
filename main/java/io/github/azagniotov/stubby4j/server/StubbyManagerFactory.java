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

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.cli.EmptyLogger;
import io.github.azagniotov.stubby4j.database.StubbedDataManager;
import io.github.azagniotov.stubby4j.database.thread.ExternalFilesScanner;
import io.github.azagniotov.stubby4j.database.thread.MainYamlScanner;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StubbyManagerFactory {

    public StubbyManagerFactory() {

    }

    public synchronized StubbyManager construct(final String dataYamlFilename, final Map<String, String> commandLineArgs) throws Exception {

        // Commenting out the following line will configure Jetty for StdErrLog DEBUG level logging
        Log.setLog(new EmptyLogger());

        final File dataYamlFile = new File(dataYamlFilename);
        final List<StubHttpLifecycle> httpLifecycles = new YAMLParser().parse(dataYamlFile.getParent(), FileUtils.constructReader(dataYamlFile));

        System.out.println();

        final StubbedDataManager stubbedDataManager = new StubbedDataManager(dataYamlFile, httpLifecycles);
        final JettyFactory jettyFactory = new JettyFactory(commandLineArgs, stubbedDataManager);
        final Server server = jettyFactory.construct();

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_WATCH)) {
            final String watchValue = commandLineArgs.get(CommandLineInterpreter.OPTION_WATCH);
            final long watchScanTime = ObjectUtils.isNotNull(watchValue) ? Long.parseLong(watchValue) : 100;
            watchDataStore(stubbedDataManager, watchScanTime);
        }

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_MUTE)) {
            ANSITerminal.muteConsole(true);
        }

        return new StubbyManager(server);
    }

    private void watchDataStore(final StubbedDataManager stubbedDataManager, final long sleepTime) {

        final MainYamlScanner mainYamlScanner = new MainYamlScanner(stubbedDataManager, sleepTime);
        new Thread(mainYamlScanner, MainYamlScanner.class.getCanonicalName()).start();

        final ExternalFilesScanner externalFilesScanner = new ExternalFilesScanner(stubbedDataManager, sleepTime);
        new Thread(externalFilesScanner, ExternalFilesScanner.class.getCanonicalName()).start();
    }
}
