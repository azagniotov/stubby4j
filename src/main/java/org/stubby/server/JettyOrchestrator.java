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
import org.stubby.cli.ANSITerminal;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.DataStore;
import org.stubby.handlers.AdminHandler;
import org.stubby.handlers.StubsHandler;
import org.stubby.handlers.SslHandler;
import org.stubby.yaml.YamlParser;

import java.util.Map;


/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:36 PM
 */
public class JettyOrchestrator {
   public static final int DEFAULT_ADMIN_PORT = 8889;
   public static final int DEFAULT_STUBS_PORT = 8882;
   public static final int DEFAULT_SSL_PORT = 7443;

   public static final String DEFAULT_HOST = "localhost";

   private static final String ADMIN_CONNECTOR_NAME = "adminConnector";
   private static final String CLIENT_CONNECTOR_NAME = "clientConnector";
   private static final String SSL_CONNECTOR_NAME = "sslConnector";

   private int currentStubsPort = DEFAULT_STUBS_PORT;
   private int currentAdminPort = DEFAULT_ADMIN_PORT;
   private String currentHost = DEFAULT_HOST;

   private final Server server;
   private final YamlParser yamlParser;
   private final DataStore dataStore;
   private final Map<String, String> commandLineArgs;

   public JettyOrchestrator(final YamlParser yamlParser, final Server server, final DataStore dataStore, final Map<String, String> commandLineArgs) {
      this.yamlParser = yamlParser;
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
         ANSITerminal.error("Could not start Jetty - it has been already started, how so?!");
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
            ANSITerminal.status("Shutting down the server...");
            stopServer();
            ANSITerminal.status("Server has stopped.");
         }
      } catch (Exception ex) {
         ANSITerminal.error("Error when stopping Jetty server: " + ex.getMessage());
      }
   }

   private void stopServer() throws Exception {
      server.setGracefulShutdown(250);
      server.setStopAtShutdown(true);
      server.stop();
   }

   private Connector[] buildConnectorList() {
      final Connector[] connectors = new Connector[]{null, buildAdminConnector()};
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYSTORE) &&
            commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYPASS)) {
         connectors[0] = buildSslConnector();
      } else {
         connectors[0] = buildClientConnector();
      }

      return connectors;
   }

   private SslSocketConnector buildSslConnector() {
      final String password = commandLineArgs.get(CommandLineIntepreter.OPTION_KEYPASS);
      final String keystorePath = commandLineArgs.get(CommandLineIntepreter.OPTION_KEYSTORE);
      final int port = getSslPort(commandLineArgs);
      final SslSocketConnector sslConnector = new SslSocketConnector();

      sslConnector.setPort(port);

      sslConnector.setName(SSL_CONNECTOR_NAME);

      sslConnector.getSslContextFactory().setKeyStorePassword(password);
      sslConnector.getSslContextFactory().setTrustStorePassword(password);
      sslConnector.getSslContextFactory().setKeyManagerPassword(password);
      sslConnector.getSslContextFactory().setKeyStorePath(keystorePath);

      ANSITerminal.status("Stubs portal running at https://" + currentHost + ":" + sslConnector.getPort());
      return sslConnector;
   }

   private SelectChannelConnector buildAdminConnector() {
      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));

      adminChannel.setName(ADMIN_CONNECTOR_NAME);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         adminChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
         currentHost = adminChannel.getHost();
      }

      ANSITerminal.status("Admin portal running at http://" + currentHost + ":" + adminChannel.getPort());
      return adminChannel;
   }

   private SelectChannelConnector buildClientConnector() {
      final SelectChannelConnector stubsChannel = new SelectChannelConnector();
      stubsChannel.setPort(getStubsPort(commandLineArgs));
      stubsChannel.setMaxIdleTime(30000);
      stubsChannel.setRequestHeaderSize(8192);
      stubsChannel.setName(CLIENT_CONNECTOR_NAME);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         stubsChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
      }

      ANSITerminal.status("Stubs portal running at http://" + currentHost + ":" + stubsChannel.getPort());
      return stubsChannel;
   }

   private ContextHandler createAdminContextHandler() {

      final ContextHandler adminContextHandler = new ContextHandler();
      adminContextHandler.setContextPath("/");
      adminContextHandler.setConnectorNames(new String[]{ADMIN_CONNECTOR_NAME});
      adminContextHandler.setHandler(new AdminHandler(this));

      return adminContextHandler;
   }

   private ContextHandler createClientContextHandler() {

      final ContextHandler clientContextHandler = new ContextHandler();
      clientContextHandler.setContextPath("/");
      clientContextHandler.setConnectorNames(new String[]{CLIENT_CONNECTOR_NAME});
      clientContextHandler.setHandler(new StubsHandler(dataStore));

      return clientContextHandler;
   }

   private ContextHandler createSslContextHandler() {

      final ContextHandler sslContextHandler = new ContextHandler();
      sslContextHandler.setContextPath("/");
      sslContextHandler.setConnectorNames(new String[]{SSL_CONNECTOR_NAME});
      sslContextHandler.setHandler(new SslHandler(dataStore));

      return sslContextHandler;
   }

   private int getSslPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT)) {
         currentStubsPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
         return currentStubsPort;
      }
      return DEFAULT_SSL_PORT;
   }

   private int getStubsPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT)) {
         currentStubsPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
         return currentStubsPort;
      }
      return DEFAULT_STUBS_PORT;
   }

   private int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADMINPORT)) {
         currentAdminPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_ADMINPORT));
         return currentAdminPort;
      }
      return DEFAULT_ADMIN_PORT;
   }

   public int getCurrentStubsPort() {
      return currentStubsPort;
   }

   public int getCurrentAdminPort() {
      return currentAdminPort;
   }

   public String getCurrentHost() {
      return currentHost;
   }

   public DataStore getDataStore() {
      return dataStore;
   }

   public YamlParser getYamlParser() {
      return yamlParser;
   }
}