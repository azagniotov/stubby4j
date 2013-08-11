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

package by.stub.server;


import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.util.List;

public final class StubbyManager {

   private final Server server;

   public StubbyManager(final Server server) {
      this.server = server;
   }

   public synchronized void startJetty() throws Exception {
      if (server.isStarted() || server.isStarting() || server.isRunning()) {
         return;
      }

      server.start();

      ANSITerminal.info("\nQuit: ctrl-c\n");
   }

   public synchronized void stopJetty() throws Exception {
      if (server.isStopped() || server.isStopping() || !server.isRunning()) {
         return;
      }

      final int timeoutMilliseconds = 100;
      server.setGracefulShutdown(timeoutMilliseconds);
      server.setStopAtShutdown(true);
      server.stop();
   }

   public synchronized boolean isJettyUp() throws Exception {
      if (server.isStarting() || server.isRunning()) {
         return true;
      }
      return false;
   }
}