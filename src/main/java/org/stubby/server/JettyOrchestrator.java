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
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;
import org.stubby.handlers.AdminHandler;
import org.stubby.handlers.ClientHandler;

import java.util.Map;
import java.util.logging.Logger;


/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:36 PM
 */
public final class JettyOrchestrator {

   private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

   protected static final int DEFAULT_ADMIN_PORT = 8889;
   protected static final int DEFAULT_CLIENT_PORT = 8882;

   protected static final String ADMIN_CONNECTOR_NAME = "adminConnector";
   protected static final String CLIENT_CONNECTOR_NAME = "clientConnector";

   public static int currentClientPort = DEFAULT_CLIENT_PORT;
   public static int currentAdminPort = DEFAULT_ADMIN_PORT;
   public static String currentHost = "localhost";

   private JettyOrchestrator() {

   }

   public static void startJetty(final Repository repository, final Map<String, String> commandLineArgs) throws Exception {
      final Server server = new Server();

      final Connector[] connectors = buildConnectorList(CLIENT_CONNECTOR_NAME, ADMIN_CONNECTOR_NAME, commandLineArgs);
      server.setConnectors(connectors);

      final HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[]{createClientContextHandler(repository), createAdminContextHandler(repository)});
      server.setHandler(handlers);

      server.start();
      server.join();

   }

   private static Connector[] buildConnectorList(final String clientConnectorName, final String adminConnectorName, final Map<String, String> commandLineArgs) {


      final SelectChannelConnector clientChannel = new SelectChannelConnector();
      clientChannel.setPort(getClientPort(commandLineArgs));
      logger.info("Stubby4j client was set to listen on port " + clientChannel.getPort());

      clientChannel.setMaxIdleTime(30000);
      clientChannel.setRequestHeaderSize(8192);
      clientChannel.setName(clientConnectorName);

      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));
      logger.info("Stubby4j admin was set to listen on port " + adminChannel.getPort());

      adminChannel.setName(adminConnectorName);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         adminChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
         clientChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
         currentHost = adminChannel.getHost();
         logger.info("Stubby4j client and admin were set to run on host " + adminChannel.getHost());
      }

      return new Connector[]{clientChannel, adminChannel};
   }

   protected static ContextHandler createAdminContextHandler(final Repository repository) {

      final ContextHandler adminContextHandler = new ContextHandler();
      adminContextHandler.setContextPath("/");
      adminContextHandler.setConnectorNames(new String[]{ADMIN_CONNECTOR_NAME});

      adminContextHandler.setHandler(new AdminHandler(repository));
      return adminContextHandler;
   }

   protected static ContextHandler createClientContextHandler(final Repository repository) {

      final ContextHandler clientContextHandler = new ContextHandler();

      clientContextHandler.setContextPath("/");
      clientContextHandler.setConnectorNames(new String[]{CLIENT_CONNECTOR_NAME});

      clientContextHandler.setHandler(new ClientHandler(repository));

      return clientContextHandler;
   }

   protected static int getClientPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT)) {
         currentClientPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
         return currentClientPort;
      }
      return DEFAULT_CLIENT_PORT;
   }

   protected static int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADMINPORT)) {
         currentAdminPort = Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_ADMINPORT));
         return currentAdminPort;
      }
      return DEFAULT_ADMIN_PORT;
   }
}
