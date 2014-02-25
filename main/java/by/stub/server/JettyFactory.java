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
import by.stub.cli.CommandLineInterpreter;
import by.stub.database.StubbedDataManager;
import by.stub.exception.Stubby4JException;
import by.stub.handlers.AdminPortalHandler;
import by.stub.handlers.AjaxEndpointStatsHandler;
import by.stub.handlers.AjaxResourceContentHandler;
import by.stub.handlers.StatusPageHandler;
import by.stub.handlers.StubDataRefreshActionHandler;
import by.stub.handlers.StubsPortalHandler;
import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.GzipHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 10/25/12, 5:17 PM
 */
@SuppressWarnings("serial")
public final class JettyFactory {

   public static final int DEFAULT_ADMIN_PORT = 8889;
   public static final int DEFAULT_STUBS_PORT = 8882;
   public static final int DEFAULT_SSL_PORT = 7443;
   public static final String DEFAULT_HOST = "0.0.0.0";
   static final String ADMIN_CONNECTOR_NAME = "stubbyAdminConnector";
   static final String STUBS_CONNECTOR_NAME = "stubsClientConnector";
   static final String SSL_CONNECTOR_NAME = "stubsSslConnector";
   private static final String ROOT_PATH_INFO = "/";
   private final Map<String, String> commandLineArgs;
   private final StubbedDataManager stubbedDataManager;
   private String currentHost;
   private int currentStubsPort;
   private int currentAdminPort;
   private int currentStubsSslPort;

   public JettyFactory(final Map<String, String> commandLineArgs, final StubbedDataManager stubbedDataManager) {
      this.commandLineArgs = commandLineArgs;
      this.stubbedDataManager = stubbedDataManager;
   }

   public Server construct() throws IOException {

      final Server server = new Server();
      server.setConnectors(buildConnectors());
      server.setHandler(constructHandlers());

      return server;
   }

   private HandlerList constructHandlers() {

      final JettyContext jettyContext = new JettyContext(currentHost, currentStubsPort, currentStubsSslPort, currentAdminPort);
      final HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[]
         {
            constructHandler(STUBS_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(staticResourceHandler("ui/html/", "default404.html"))),
            constructHandler(STUBS_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(staticResourceHandler("ui/images/", "favicon.ico"))),
            constructHandler(STUBS_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubbedDataManager))),

            constructHandler(SSL_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(staticResourceHandler("ui/html/", "default404.html"))),
            constructHandler(SSL_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(staticResourceHandler("ui/images/", "favicon.ico"))),
            constructHandler(SSL_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubbedDataManager))),

            constructHandler(ADMIN_CONNECTOR_NAME, "/status", gzipHandler(new StatusPageHandler(jettyContext, stubbedDataManager))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/refresh", new StubDataRefreshActionHandler(jettyContext, stubbedDataManager)),
            constructHandler(ADMIN_CONNECTOR_NAME, "/js/highlight", gzipHandler(staticResourceHandler("ui/js/highlight/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/js/minified", gzipHandler(staticResourceHandler("ui/js/minified/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/js/d3", gzipHandler(staticResourceHandler("ui/js/d3/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/js", gzipHandler(staticResourceHandler("ui/js/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/css", gzipHandler(staticResourceHandler("ui/css/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/images", gzipHandler(staticResourceHandler("ui/images/"))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/ajax/resource", gzipHandler(new AjaxResourceContentHandler(stubbedDataManager))),
            constructHandler(ADMIN_CONNECTOR_NAME, "/ajax/stats", gzipHandler(new AjaxEndpointStatsHandler(stubbedDataManager))),
            constructHandler(ADMIN_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new AdminPortalHandler(stubbedDataManager)))
         }
      );

      return handlers;
   }

   private ResourceHandler staticResourceHandler(final String classPathResource, final String... staticResources) {

      final ResourceHandler resourceHandler = new ResourceHandler();
      resourceHandler.setDirectoriesListed(true);
      resourceHandler.setWelcomeFiles(staticResources);
      resourceHandler.setBaseResource(Resource.newClassPathResource(classPathResource));

      return resourceHandler;
   }

   private GzipHandler gzipHandler(final AbstractHandler abstractHandler) {

      final GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.setMimeTypes(
            "text/html," +
            "text/plain," +
            "text/xml," +
            "application/xhtml+xml," +
            "application/json," +
            "text/css," +
            "application/javascript," +
            "application/x-javascript," +
            "image/svg+xml," +
            "image/gif," +
            "image/jpg," +
            "image/jpeg," +
            "image/png");
      gzipHandler.setHandler(abstractHandler);

      return gzipHandler;
   }

   private ContextHandler constructHandler(final String connectorName, final String pathInfo, final Handler handler) {

      final ContextHandler contextHandler = new ContextHandler();
      contextHandler.setContextPath(pathInfo);
      contextHandler.setAllowNullPathInfo(true);
      contextHandler.setConnectorNames(new String[]{connectorName});
      contextHandler.addLocaleEncoding(Locale.US.getDisplayName(), StringUtils.UTF_8);
      contextHandler.setHandler(handler);

      final MimeTypes mimeTypes = new MimeTypes();
      mimeTypes.setMimeMap(new HashMap());
      contextHandler.setMimeTypes(mimeTypes);

      return contextHandler;
   }

   private Connector[] buildConnectors() throws IOException {

      final List<Connector> connectors = new ArrayList<Connector>();

      connectors.add(buildAdminConnector());
      connectors.add(buildStubsConnector());
      connectors.add(buildStubsSslConnector());

      return connectors.toArray(new Connector[connectors.size()]);
   }

   private SelectChannelConnector buildAdminConnector() {
      final SelectChannelConnector adminChannel = new SelectChannelConnector();
      adminChannel.setPort(getAdminPort(commandLineArgs));

      adminChannel.setName(ADMIN_CONNECTOR_NAME);
      adminChannel.setHost(DEFAULT_HOST);

      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
         adminChannel.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
      }

      final String configured = String.format("Admin portal configured at http://%s:%s",
         adminChannel.getHost(), adminChannel.getPort());
      ANSITerminal.status(configured);

      final String status = String.format("Admin portal status enabled at http://%s:%s/status",
         adminChannel.getHost(), adminChannel.getPort());
      ANSITerminal.status(status);

      currentHost = adminChannel.getHost();
      currentAdminPort = adminChannel.getPort();

      return adminChannel;
   }

   private SelectChannelConnector buildStubsConnector() {

      final SelectChannelConnector stubsChannel = new SelectChannelConnector();
      stubsChannel.setPort(getStubsPort(commandLineArgs));
      final int idleTimeInMilliseconds = 45000;
      stubsChannel.setMaxIdleTime(idleTimeInMilliseconds);
      stubsChannel.setRequestHeaderSize(8192);
      stubsChannel.setName(STUBS_CONNECTOR_NAME);
      stubsChannel.setHost(DEFAULT_HOST);

      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
         stubsChannel.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
      }

      final String status = String.format("Stubs portal configured at http://%s:%s",
         stubsChannel.getHost(), stubsChannel.getPort());
      ANSITerminal.status(status);

      currentStubsPort = stubsChannel.getPort();

      return stubsChannel;
   }

   private SslSocketConnector buildStubsSslConnector() throws IOException {

      String keystorePath = null;
      String password = "password";
      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_KEYSTORE)
         && commandLineArgs.containsKey(CommandLineInterpreter.OPTION_KEYPASS)) {
         password = commandLineArgs.get(CommandLineInterpreter.OPTION_KEYPASS);
         keystorePath = commandLineArgs.get(CommandLineInterpreter.OPTION_KEYSTORE);
      }

      final SslContextFactory sslContextFactory = constructSslContextFactory(password, keystorePath);
      final SslSocketConnector sslConnector = new SslSocketConnector(sslContextFactory);
      sslConnector.setPort(getStubsSslPort(commandLineArgs));
      sslConnector.setName(SSL_CONNECTOR_NAME);
      sslConnector.setHost(DEFAULT_HOST);

      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
         sslConnector.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
      }

      final String status = String.format("Stubs portal configured with TLS at https://%s:%s using %s keystore",
         sslConnector.getHost(), sslConnector.getPort(), (ObjectUtils.isNull(keystorePath) ? "internal" : "provided " + keystorePath));
      ANSITerminal.status(status);

      currentStubsSslPort = sslConnector.getPort();

      return sslConnector;
   }

   private SslContextFactory constructSslContextFactory(final String password, final String keystorePath) throws IOException {

      final SslContextFactory sslFactory = new SslContextFactory();
      sslFactory.setKeyStorePassword(password);
      sslFactory.setKeyManagerPassword(password);

      relaxSslTrustManager();

      if (ObjectUtils.isNull(keystorePath)) {
         final URL keyURL = this.getClass().getResource("/ssl/localhost.jks");
         final Resource keyStoreResource = Resource.newResource(keyURL);
         sslFactory.setKeyStoreResource(keyStoreResource);

         return sslFactory;
      }

      sslFactory.setKeyStorePath(keystorePath);

      return sslFactory;
   }

   private void relaxSslTrustManager() {
      try {
         new FakeX509TrustManager().allowAllSSL();
      } catch (final Exception ex) {
         throw new Stubby4JException(ex.toString(), ex);
      }
   }

   private int getStubsPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_CLIENTPORT)) {
         return Integer.parseInt(commandLineArgs.get(CommandLineInterpreter.OPTION_CLIENTPORT));
      }
      return DEFAULT_STUBS_PORT;
   }

   private int getStubsSslPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_TLSPORT)) {
         return Integer.parseInt(commandLineArgs.get(CommandLineInterpreter.OPTION_TLSPORT));
      }
      return DEFAULT_SSL_PORT;
   }

   private int getAdminPort(final Map<String, String> commandLineArgs) {
      if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADMINPORT)) {
         return Integer.parseInt(commandLineArgs.get(CommandLineInterpreter.OPTION_ADMINPORT));
      }
      return DEFAULT_ADMIN_PORT;
   }
}