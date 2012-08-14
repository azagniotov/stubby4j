/*
A Java-based HTTP stub server

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

package org.stubby.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.DataStore;
import org.stubby.handlers.AdminHandler;
import org.stubby.handlers.ClientHandler;
import org.stubby.handlers.SslHandler;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:36 PM
 */
public class JettyOrchestrator {

   private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

   public static final int DEFAULT_ADMIN_PORT = 8889;
   public static final int DEFAULT_CLIENT_PORT = 8882;
   public static final int DEFAULT_SSL_PORT = 7443;

   public static final String DEFAULT_HOST = "localhost";

   private static final String ADMIN_CONNECTOR_NAME = "adminConnector";
   private static final String CLIENT_CONNECTOR_NAME = "clientConnector";
   private static final String SSL_CONNECTOR_NAME = "sslConnector";

   private int currentClientPort = DEFAULT_CLIENT_PORT;
   private int currentAdminPort = DEFAULT_ADMIN_PORT;
   private String currentHost = DEFAULT_HOST;

   private final Server server;
   private final DataStore dataStore;
   private final Map<String, String> commandLineArgs;

   public JettyOrchestrator(final Server server, final DataStore dataStore, final Map<String, String> commandLineArgs) {
      this.server = server;
      this.dataStore = dataStore;
      this.commandLineArgs = commandLineArgs;
   }

   public void startJetty() throws Exception {
      do {
         stopServer();
      }
      while (!server.isStopped());

      if (!server.isStarting() && !server.isStarted()) {
         server.setConnectors(buildConnectorList());
         server.setHandler(buildHandlerList());
         server.start();
      } else {
         logger.info("Could not start Jetty - it has been already started, how so?!");
      }
   }

   public boolean isSslConfigured() throws Exception {
      for (final Connector connector : server.getConnectors()) {
         if (connector instanceof SslSocketConnector) {
            return true;
         }
      }
      return false;
   }

   private HandlerList buildHandlerList() {
      final HandlerList handlers = new HandlerList();
      handlers.setHandlers(
            new Handler[]
                  {
                        createClientContextHandler(),
                        createAdminContextHandler(),
                        createSslContextHandler()
                  }
      );
      return handlers;
   }

   public void stopJetty() {

      try {
         if (server != null && !server.isStopped()) {
            logger.info("Shutting down the server...");
            stopServer();
            logger.info("Server has stopped.");
         }
      } catch (Exception ex) {
         logger.info("Error when stopping Jetty server: " + ex.getMessage());
      }
      /*
      new Thread() {
         public void run() {
            try {
               if (server != null && !server.isStopped()) {
                  logger.info("Shutting down the server...");
                  stopServer();
                  logger.info("Server has stopped.");
               }
            } catch (Exception ex) {
               logger.info("Error when stopping Jetty server: " + ex.getMessage());
            }
         }
      }.start();
      */
   }

   private void stopServer() throws Exception {
      server.setGracefulShutdown(250);
      server.setStopAtShutdown(true);
      server.stop();
   }

   private Connector[] buildConnectorList() {
      final Connector[] connectors = new Connector[]{buildClientConnector(), buildAdminConnector()};
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYSTORE) &&
            commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYPASS)) {
         return new Connector[]{buildSslConnector(), connectors[0], connectors[1]};
      }
      return connectors;
   }

   private SslSocketConnector buildSslConnector() {

      final String password = commandLineArgs.get(CommandLineIntepreter.OPTION_KEYPASS);
      final String keystorePath = commandLineArgs.get(CommandLineIntepreter.OPTION_KEYSTORE);

      final SslSocketConnector sslConnector = new SslSocketConnector();
      sslConnector.setPort(DEFAULT_SSL_PORT);
      sslConnector.setName(SSL_CONNECTOR_NAME);
      logger.info("Stubby4j SSL was set to listen on port " + DEFAULT_SSL_PORT);

      sslConnector.getSslContextFactory().setKeyStorePassword(password);
      sslConnector.getSslContextFactory().setTrustStorePassword(password);
      sslConnector.getSslContextFactory().setKeyManagerPassword(password);
      sslConnector.getSslContextFactory().setKeyStorePath(keystorePath);

      return sslConnector;
   }

   private SelectChannelConnector buildAdminConnector() {
      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));
      logger.info("Stubby4j admin was set to listen on port " + adminChannel.getPort());

      adminChannel.setName(ADMIN_CONNECTOR_NAME);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         adminChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
         currentHost = adminChannel.getHost();
         logger.info("Stubby4j admin was set to run on host " + adminChannel.getHost());
      }
      return adminChannel;
   }

   private SelectChannelConnector buildClientConnector() {
      final SelectChannelConnector clientChannel = new SelectChannelConnector();
      clientChannel.setPort(getClientPort(commandLineArgs));
      logger.info("Stubby4j client was set to listen on port " + clientChannel.getPort());

      clientChannel.setMaxIdleTime(30000);
      clientChannel.setRequestHeaderSize(8192);
      clientChannel.setName(CLIENT_CONNECTOR_NAME);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         clientChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
         logger.info("Stubby4j client was set to run on host " + clientChannel.getHost());
      }
      return clientChannel;
   }

   private ContextHandler createAdminContextHandler() {

      final ContextHandler adminContextHandler = new ContextHandler();
      adminContextHandler.setContextPath("/");
      adminContextHandler.setConnectorNames(new String[]{ADMIN_CONNECTOR_NAME});
      adminContextHandler.setHandler(new AdminHandler(dataStore, this));

      return adminContextHandler;
   }

   private ContextHandler createClientContextHandler() {

      final ContextHandler clientContextHandler = new ContextHandler();
      clientContextHandler.setContextPath("/");
      clientContextHandler.setConnectorNames(new String[]{CLIENT_CONNECTOR_NAME});
      clientContextHandler.setHandler(new ClientHandler(dataStore));

      return clientContextHandler;
   }

   private ContextHandler createSslContextHandler() {

      final ContextHandler sslContextHandler = new ContextHandler();
      sslContextHandler.setContextPath("/");
      sslContextHandler.setConnectorNames(new String[]{SSL_CONNECTOR_NAME});
      sslContextHandler.setHandler(new SslHandler(dataStore));

      return sslContextHandler;
   }

   private int getClientPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT)) {
         currentClientPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
         return currentClientPort;
      }
      return DEFAULT_CLIENT_PORT;
   }

   private int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADMINPORT)) {
         currentAdminPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_ADMINPORT));
         return currentAdminPort;
      }
      return DEFAULT_ADMIN_PORT;
   }

   public int getCurrentClientPort() {
      return currentClientPort;
   }

   public int getCurrentAdminPort() {
      return currentAdminPort;
   }

   public String getCurrentHost() {
      return currentHost;
   }

   public void registerStubData(final String yamlConfigurationContent) throws IOException {
      final List<StubHttpLifecycle> httpLifecycles = YamlConsumer.parseYamlContent(yamlConfigurationContent);
      dataStore.setStubHttpLifecycles(httpLifecycles);
   }
}