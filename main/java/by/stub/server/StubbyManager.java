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
import org.eclipse.jetty.server.Server;

public final class StubbyManager {

   private final Server server;

   public StubbyManager(final Server server) {
      this.server = server;
   }

   public synchronized void startJetty() throws Exception {
      if (isJettyStarting() || isJettyUp()) {
         return;
      }

      server.start();

      while (!isJettyUp()) {
         Thread.sleep(250);
      }

      ANSITerminal.info("\nQuit: ctrl-c\n");
   }

   public synchronized void stopJetty() throws Exception {
      if (isJettyStopping() || isJettyDown()) {
         return;
      }

      final int timeoutMilliseconds = 100;
      server.setGracefulShutdown(timeoutMilliseconds);
      server.setStopAtShutdown(true);
      server.stop();

      while (!isJettyDown()) {
         Thread.sleep(250);
      }
   }

   public synchronized boolean isJettyStarting() throws Exception {
      if (server.isStarting()) {
         return true;
      }
      return false;
   }

   public synchronized boolean isJettyUp() throws Exception {
      if (server.isStarted() && server.isRunning()) {
         return true;
      }
      return false;
   }

   public synchronized boolean isJettyStopping() throws Exception {
      if (server.isStopping()) {
         return true;
      }
      return false;
   }

   public synchronized boolean isJettyDown() throws Exception {
      if (server.isStopped() && !server.isRunning()) {
         return true;
      }
      return false;
   }
}