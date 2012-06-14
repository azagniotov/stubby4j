package org.stubby.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.stubby.database.Repository;
import org.stubby.servlets.admin.PingServlet;
import org.stubby.servlets.admin.WelcomeServlet;
import org.stubby.servlets.client.ConsumerServlet;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:36 PM
 */
public final class JettyOrchestrator {

   private static final int DEFAULT_ADMIN_PORT = 8889;
   private static final int DEFAULT_CLIENT_PORT = 8882;

   private static final String ADMIN_CONNECTOR_NAME = "adminConnector";
   private static final String CLIENT_CONNECTOR_NAME = "clientConnector";

   private JettyOrchestrator() {

   }

   public static void startJetty(final Repository repository, final String[] commandLineArgs) throws Exception {
      final Server server = new Server();

      final Connector[] connectors = buildConnectorList(CLIENT_CONNECTOR_NAME, ADMIN_CONNECTOR_NAME, commandLineArgs);
      final ContextHandlerCollection contextHandlerCollection = buildContextHandlerCollection(repository);

      server.setConnectors(connectors);
      server.setHandler(contextHandlerCollection);

      server.start();
      server.join();
   }

   private static Connector[] buildConnectorList(final String clientConnectorName, final String adminConnectorName, final String[] commandLineArgs) {


      final SelectChannelConnector clientChannel = new SelectChannelConnector();
      clientChannel.setPort(getClientPort(commandLineArgs));
      clientChannel.setMaxIdleTime(30000);
      clientChannel.setRequestHeaderSize(8192);
      clientChannel.setName(clientConnectorName);

      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));
      adminChannel.setName(adminConnectorName);

      return new Connector[]{clientChannel, adminChannel};
   }

   private static ContextHandlerCollection buildContextHandlerCollection(final Repository repository) {

      final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
      contextHandlerCollection.setHandlers(new Handler[]{createClientContextHandler(repository), createAdminContextHandler(repository)});
      return contextHandlerCollection;
   }

   private static ContextHandler createAdminContextHandler(final Repository repository) {

      final ServletContextHandler adminContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
      adminContextHandler.setContextPath("/");
      adminContextHandler.setConnectorNames(new String[]{ADMIN_CONNECTOR_NAME});

      adminContextHandler.addServlet(new ServletHolder(new WelcomeServlet()), "/*");
      adminContextHandler.addServlet(new ServletHolder(new PingServlet(repository)), "/ping");

      return adminContextHandler;
   }

   private static ContextHandler createClientContextHandler(final Repository repository) {

      final ServletContextHandler clientContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

      clientContextHandler.setContextPath("/");
      clientContextHandler.setConnectorNames(new String[]{CLIENT_CONNECTOR_NAME});

      clientContextHandler.addServlet(new ServletHolder(new ConsumerServlet(repository)), "/*");

      return clientContextHandler;
   }

   private static int getClientPort(final String[] commandLineArgs) {
      if (commandLineArgs.length == 2 || commandLineArgs.length == 3) {
         return Integer.parseInt(commandLineArgs[1]);
      }
      return DEFAULT_CLIENT_PORT;
   }

   private static int getAdminPort(final String[] commandLineArgs) {
      if (commandLineArgs.length == 3) {
         return Integer.parseInt(commandLineArgs[2]);
      }
      return DEFAULT_ADMIN_PORT;
   }
}
