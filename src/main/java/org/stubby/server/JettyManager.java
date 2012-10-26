package org.stubby.server;


import org.eclipse.jetty.server.Server;
import org.stubby.cli.ANSITerminal;

public final class JettyManager {

   private final Server server;

   public JettyManager(final Server server) {
      this.server = server;
   }

   public void startJetty() throws Exception {
      synchronized (JettyManager.class) {

         while (server.isStopping()) {
            ANSITerminal.warn("Another Jetty instance is shutting down.. Going to sleep for 200ms before start..");
            Thread.sleep(200);
         }

         if (server.isStopped() && !server.isRunning()) {
            server.start();

            ANSITerminal.status("Jetty running");
            ANSITerminal.info("\nQuit: ctrl-c\n");
         }
      }
   }

   public void stopJetty() throws Exception {
      synchronized (JettyManager.class) {
         server.setGracefulShutdown(250);
         server.setStopAtShutdown(true);
         server.stop();
      }
   }
}