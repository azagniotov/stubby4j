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
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;
import org.stubby.servlets.admin.PingServlet;
import org.stubby.servlets.admin.WelcomeServlet;
import org.stubby.servlets.client.ConsumerServlet;

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

   protected static final String GLOBAL_CONTEXT_PATH = "/*";
   protected static final String ADMIN_PING_CONTEXT_PATH = "/ping";

   private JettyOrchestrator() {

   }

   public static void startJetty(final Repository repository, final Map<String, String> commandLineArgs) throws Exception {
      final Server server = new Server();

      final Connector[] connectors = buildConnectorList(CLIENT_CONNECTOR_NAME, ADMIN_CONNECTOR_NAME, commandLineArgs);
      final ContextHandlerCollection contextHandlerCollection = buildContextHandlerCollection(repository);

      server.setConnectors(connectors);
      server.setHandler(contextHandlerCollection);

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
         logger.info("Stubby4j client and admin were set to run on host " + adminChannel.getHost());
      }

      return new Connector[]{clientChannel, adminChannel};
   }

   private static ContextHandlerCollection buildContextHandlerCollection(final Repository repository) {

      final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
      contextHandlerCollection.setHandlers(new Handler[]{createClientContextHandler(repository), createAdminContextHandler(repository)});
      return contextHandlerCollection;
   }

   protected static ContextHandler createAdminContextHandler(final Repository repository) {

      final ServletContextHandler adminContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
      adminContextHandler.setContextPath("/");
      adminContextHandler.setConnectorNames(new String[]{ADMIN_CONNECTOR_NAME});

      final ServletHolder welcomeHolder = new ServletHolder(WelcomeServlet.class.getSimpleName(), new WelcomeServlet());
      adminContextHandler.addServlet(welcomeHolder, GLOBAL_CONTEXT_PATH);

      final ServletHolder pingHolder = new ServletHolder(PingServlet.class.getSimpleName(), new PingServlet(repository));
      adminContextHandler.addServlet(pingHolder, ADMIN_PING_CONTEXT_PATH);

      return adminContextHandler;
   }

   protected static ContextHandler createClientContextHandler(final Repository repository) {

      final ServletContextHandler clientContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

      clientContextHandler.setContextPath("/");
      clientContextHandler.setConnectorNames(new String[]{CLIENT_CONNECTOR_NAME});

      final ServletHolder servletHolder = new ServletHolder(ConsumerServlet.class.getSimpleName(), new ConsumerServlet(repository));
      clientContextHandler.addServlet(servletHolder, GLOBAL_CONTEXT_PATH);

      return clientContextHandler;
   }

   protected static int getClientPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT)) {
         return Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
      }
      return DEFAULT_CLIENT_PORT;
   }

   protected static int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADMINPORT)) {
         return Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_ADMINPORT));
      }
      return DEFAULT_ADMIN_PORT;
   }
}
