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
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import org.eclipse.jetty.server.Server;

import java.util.List;

public final class StubbyManager {

    private final Server server;
    private final JettyFactory jettyFactory;
    private final StubRepository stubRepository;

    StubbyManager(final Server server, final JettyFactory jettyFactory, final StubRepository stubRepository) {
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
            Thread.sleep(250);
        }
        stubRepository.retrieveLoadedStubs();
    }

    public synchronized void stopJetty() throws Exception {
        if (isJettyStopping() || isJettyDown()) {
            return;
        }

        server.stop();

        while (!isJettyDown()) {
            ANSITerminal.warn("Waiting for Jetty to finish shutting down..");
            Thread.sleep(250);
        }
        ANSITerminal.status("Jetty successfully shutdown");
    }

    public synchronized void joinJetty() throws Exception {
        server.join();
    }

    public List<String> statuses() {
        return jettyFactory.statuses();
    }

    private boolean isJettyStarting() throws Exception {
        return server.isStarting();
    }

    private boolean isJettyUp() throws Exception {
        return (server.isStarted() && server.isRunning());
    }

    private boolean isJettyStopping() throws Exception {
        return server.isStopping();
    }

    private boolean isJettyDown() throws Exception {
        return (server.isStopped() && !server.isRunning());
    }
}