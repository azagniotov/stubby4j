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

package org.stubby.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.stubby.cli.ANSITerminal;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.DataStore;
import org.stubby.handlers.PingHandler;
import org.stubby.handlers.SslHandler;
import org.stubby.handlers.StubsHandler;
import org.stubby.handlers.StubsRegistrationHandler;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.YamlParser;

import java.util.Locale;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 10/25/12, 5:17 PM
 */
public final class JettyFactory {

   public static final int DEFAULT_ADMIN_PORT = 8889;
   public static final int DEFAULT_STUBS_PORT = 8882;
   public static final int DEFAULT_SSL_PORT = 7443;
   public static final String DEFAULT_HOST = "localhost";

   static final String ADMIN_CONNECTOR_NAME = "stubbyAdminConnector";
   static final String STUBS_CONNECTOR_NAME = "stubsClientConnector";
   static final String SSL_CONNECTOR_NAME = "stubsSslConnector";

   private final Map<String, String> commandLineArgs;
   private final YamlParser yamlParser;
   private final DataStore dataStore;

   private String currentHost;
   private boolean isSsl;
   private int currentStubsPort;
   private int currentAdminPort;

   public JettyFactory(final Map<String, String> commandLineArgs, final DataStore dataStore, final YamlParser yamlParser) {
      this.commandLineArgs = commandLineArgs;
      this.dataStore = dataStore;
      this.yamlParser = yamlParser;
   }

   public Server construct(final DataStore dataStore, final YamlParser yamlParser) {

      synchronized (JettyFactory.class) {
         final Server server = new Server();
         server.setConnectors(buildConnectors());
         server.setHandler(constructHandlers());

         return server;
      }
   }

   private HandlerList constructHandlers() {

      final JettyContext jettyContext = new JettyContext(currentHost, isSsl, currentStubsPort, currentAdminPort);
      final HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[]
            {
                  constructHandler(STUBS_CONNECTOR_NAME, "/", new StubsHandler(dataStore)),
                  constructHandler(SSL_CONNECTOR_NAME, "/", new SslHandler(dataStore)),

                  constructHandler(ADMIN_CONNECTOR_NAME, "/stubdata/new", new StubsRegistrationHandler(dataStore, yamlParser)),
                  constructHandler(ADMIN_CONNECTOR_NAME, "/ping", new PingHandler(jettyContext, dataStore, yamlParser)),
                  constructHandler(ADMIN_CONNECTOR_NAME, "/", contextRootHandler("html/", "admin-index.html"))
            }
      );

      return handlers;
   }

   private ResourceHandler contextRootHandler(final String classPathResource, final String welcomeFile) {

      final ResourceHandler resourceHandler = new ResourceHandler();
      resourceHandler.setDirectoriesListed(true);
      resourceHandler.setWelcomeFiles(new String[]{welcomeFile});
      resourceHandler.setBaseResource(Resource.newClassPathResource(classPathResource));

      return resourceHandler;
   }

   private ContextHandler constructHandler(final String connectorName, final String pathInfo, final Handler handler) {

      final ContextHandler contextHandler = new ContextHandler();
      contextHandler.setContextPath(pathInfo);
      contextHandler.setAllowNullPathInfo(true);
      contextHandler.setConnectorNames(new String[]{connectorName});
      contextHandler.addLocaleEncoding(Locale.US.getDisplayName(), StringUtils.UTF_8);
      contextHandler.setHandler(handler);

      return contextHandler;
   }

   private Connector[] buildConnectors() {
      final Connector[] connectors = new Connector[]{buildStubsConnector(), buildAdminConnector()};

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYSTORE) &&
            commandLineArgs.containsKey(CommandLineIntepreter.OPTION_KEYPASS))
         connectors[0] = buildStubsSslConnector();

      return connectors;
   }

   private SelectChannelConnector buildAdminConnector() {
      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));

      adminChannel.setName(ADMIN_CONNECTOR_NAME);
      adminChannel.setHost(DEFAULT_HOST);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         adminChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
      }

      final String status = String.format("Admin portal configured at http://%s:%s", adminChannel.getHost(), adminChannel.getPort());
      ANSITerminal.status(status);

      currentHost = adminChannel.getHost();
      currentAdminPort = adminChannel.getPort();

      return adminChannel;
   }

   private SelectChannelConnector buildStubsConnector() {

      final SelectChannelConnector stubsChannel = new SelectChannelConnector();
      stubsChannel.setPort(getStubsPort(commandLineArgs));
      stubsChannel.setMaxIdleTime(30000);
      stubsChannel.setRequestHeaderSize(8192);
      stubsChannel.setName(STUBS_CONNECTOR_NAME);
      stubsChannel.setHost(DEFAULT_HOST);

      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADDRESS)) {
         stubsChannel.setHost(commandLineArgs.get(CommandLineIntepreter.OPTION_ADDRESS));
      }

      final String status = String.format("Stubs portal configured at http://%s:%s", stubsChannel.getHost(), stubsChannel.getPort());
      ANSITerminal.status(status);

      currentStubsPort = stubsChannel.getPort();

      return stubsChannel;
   }

   private SslSocketConnector buildStubsSslConnector() {
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

      final String status = String.format("Stubs portal with SSL configured at https://%s:%s", sslConnector.getHost(), sslConnector.getPort());
      ANSITerminal.status(status);

      isSsl = true;

      return sslConnector;
   }

   private int getSslPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT))
         return Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
      return DEFAULT_SSL_PORT;
   }

   private int getStubsPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_CLIENTPORT))
         return Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_CLIENTPORT));
      return DEFAULT_STUBS_PORT;
   }

   private int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineIntepreter.OPTION_ADMINPORT))
         return Integer.parseInt(commandLineArgs.get(CommandLineIntepreter.OPTION_ADMINPORT));
      return DEFAULT_ADMIN_PORT;
   }
}