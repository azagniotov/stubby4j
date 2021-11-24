package io.github.azagniotov.stubby4j.server;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.handlers.AjaxEndpointStatsHandler;
import io.github.azagniotov.stubby4j.handlers.AjaxResourceContentHandler;
import io.github.azagniotov.stubby4j.handlers.FaviconHandler;
import io.github.azagniotov.stubby4j.handlers.JsonErrorHandler;
import io.github.azagniotov.stubby4j.handlers.StatusPageHandler;
import io.github.azagniotov.stubby4j.handlers.StubDataRefreshActionHandler;
import io.github.azagniotov.stubby4j.handlers.StubsPortalHandler;
import io.github.azagniotov.stubby4j.server.ssl.SslUtils;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.TLS_v1_3;
import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.getSelfSignedKeyStorePath;
import static java.util.Arrays.asList;


@SuppressWarnings("serial")
@GeneratedCodeCoverageExclusion
public final class JettyFactory {

    public static final int DEFAULT_ADMIN_PORT = 8889;
    public static final int DEFAULT_STUBS_PORT = 8882;
    public static final int DEFAULT_SSL_PORT = 7443;
    public static final String DEFAULT_HOST = "localhost";

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyFactory.class);

    private static final int SERVER_CONNECTOR_IDLETIME_MILLIS = 45000;
    private static final String PROTOCOL_HTTP_1_1 = "HTTP/1.1";
    private static final String PROTOCOL_HTTP_2 = "h2";

    private static final String ADMIN_CONNECTOR_NAME = "AdminConnector";
    private static final String STUBS_CONNECTOR_NAME = "StubsConnector";
    private static final String SSL_CONNECTOR_NAME = "SslStubsConnector";
    private static final String ROOT_PATH_INFO = "/";
    private final Map<String, String> commandLineArgs;
    private final StubRepository stubRepository;
    private final List<String> statuses;
    private String currentHost;
    private int currentStubsPort;
    private int currentAdminPort;
    private int currentStubsSslPort;

    JettyFactory(final Map<String, String> commandLineArgs, final StubRepository stubRepository) {
        this.commandLineArgs = commandLineArgs;
        this.stubRepository = stubRepository;
        this.statuses = new LinkedList<>();
    }

    Server construct() throws IOException, ServletException {
        final Server server = new Server();
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);
        server.setConnectors(buildConnectors(server));

        // The WebSocketServerContainerInitializer.configureContext() requires knowledge about the Server that it will be run under.
        // Add the ServletContextHandler to the Server instance via its Server.setHandler(Handler) call before you attempt to configure the context.
        // https://stackoverflow.com/a/34044984
        // https://stackoverflow.com/questions/34007087/jetty-9-add-websockets-handler-to-handler-list
        final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/ws");
        server.setHandler(constructHandlers(servletContextHandler));

        // Configure specific websocket behavior
        NativeWebSocketServletContainerInitializer.configure(servletContextHandler, (servletContext, nativeWebSocketConfiguration) ->
        {
            // Configure default max size
            nativeWebSocketConfiguration.getPolicy().setMaxTextMessageBufferSize(65535);

            // Add websockets
            nativeWebSocketConfiguration.addMapping("/*", StubsJettyNativeWebSocket.class);
        });

        // Add generic filter that will accept WebSocket upgrade.
        WebSocketUpgradeFilter.configure(servletContextHandler);

        return server;
    }

    private ContextHandlerCollection constructHandlers(final ServletContextHandler webSocketServletContextHandler) {

        final JettyContext jettyContext = new JettyContext(currentHost, currentStubsPort, currentStubsSslPort, currentAdminPort);
        final ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]
                {
                        constructHandler(STUBS_CONNECTOR_NAME, "/favicon.ico", gzipHandler(new FaviconHandler())),
                        constructHandler(STUBS_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubRepository))),
                        constructHandler(SSL_CONNECTOR_NAME, "/favicon.ico", gzipHandler(new FaviconHandler())),
                        constructHandler(SSL_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubRepository))),
                        webSocketServletContextHandler,
                        constructHandler(ADMIN_CONNECTOR_NAME, "/status", gzipHandler(new StatusPageHandler(jettyContext, stubRepository))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/refresh", new StubDataRefreshActionHandler(stubRepository)),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/js/highlight", gzipHandler(staticResourceHandler("ui/js/highlight/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/js/minified", gzipHandler(staticResourceHandler("ui/js/minified/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/js/d3", gzipHandler(staticResourceHandler("ui/js/d3/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/js", gzipHandler(staticResourceHandler("ui/js/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/css", gzipHandler(staticResourceHandler("ui/css/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/images", gzipHandler(staticResourceHandler("ui/images/"))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/ajax/resource", gzipHandler(new AjaxResourceContentHandler(stubRepository))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/ajax/stats", gzipHandler(new AjaxEndpointStatsHandler(stubRepository))),
                        constructHandler(ADMIN_CONNECTOR_NAME, "/favicon.ico", gzipHandler(new FaviconHandler())),
                        constructHandler(ADMIN_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new AdminPortalHandler(stubRepository)))
                }
        );

        return handlers;
    }

    private ResourceHandler staticResourceHandler(final String classPathResource) {

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setBaseResource(Resource.newClassPathResource(classPathResource));

        return resourceHandler;
    }

    private GzipHandler gzipHandler(final AbstractHandler abstractHandler) {

        final GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.addIncludedMimeTypes(
                "text/html,",
                "text/plain,",
                "text/xml,",
                "application/xhtml,xml,",
                "application/json,",
                "text/css,",
                "application/javascript,",
                "application/x-javascript,",
                "image/svg,xml,",
                "image/x-icon,",
                "image/gif,",
                "image/jpg,",
                "image/jpeg,",
                "image/png");
        gzipHandler.setHandler(abstractHandler);

        return gzipHandler;
    }

    private ContextHandler constructHandler(final String connectorName, final String pathInfo, final Handler handler) {

        final ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath(pathInfo);
        contextHandler.setAllowNullPathInfo(true);
        // We prefix the name with an '@' because this is the way Jetty v9 finds a named connector
        contextHandler.setVirtualHosts(new String[]{"@" + connectorName});
        contextHandler.addLocaleEncoding(Locale.US.getDisplayName(), StringUtils.UTF_8);
        contextHandler.setHandler(handler);

        contextHandler.setErrorHandler(new JsonErrorHandler());

        final MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.setMimeMap(new HashMap<>());
        contextHandler.setMimeTypes(mimeTypes);

        return contextHandler;
    }

    private Connector[] buildConnectors(final Server server) throws IOException {

        final List<Connector> connectors = new ArrayList<>();

        if (!commandLineArgs.containsKey(CommandLineInterpreter.OPTION_DISABLE_ADMIN)) {
            connectors.add(buildAdminConnector(server));
        }
        connectors.add(buildStubsConnector(server));
        if (!commandLineArgs.containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL)) {
            connectors.add(buildStubsSslConnector(server));
        }

        final Connector[] connectorArray = new Connector[connectors.size()];
        return connectors.toArray(connectorArray);
    }

    private ServerConnector buildAdminConnector(final Server server) {

        final HttpConfiguration httpConfiguration = constructHttpConfiguration();
        final ServerConnector adminChannel = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
        adminChannel.setPort(getAdminPort(commandLineArgs));

        adminChannel.setName(ADMIN_CONNECTOR_NAME);
        adminChannel.setHost(DEFAULT_HOST);
        adminChannel.setIdleTimeout(SERVER_CONNECTOR_IDLETIME_MILLIS);

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
            adminChannel.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
        }

        final String configured = String.format("Admin portal configured at http://%s:%s",
                adminChannel.getHost(), adminChannel.getPort());
        statuses.add(configured);
        final String status = String.format("Admin portal status enabled at http://%s:%s/status",
                adminChannel.getHost(), adminChannel.getPort());
        statuses.add(status);

        currentHost = adminChannel.getHost();
        currentAdminPort = adminChannel.getPort();

        return adminChannel;
    }

    private ServerConnector buildStubsConnector(final Server server) {

        final HttpConfiguration httpConfiguration = constructHttpConfiguration();
        final ServerConnector stubsChannel = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
        stubsChannel.setPort(getStubsPort(commandLineArgs));

        stubsChannel.setName(STUBS_CONNECTOR_NAME);
        stubsChannel.setHost(DEFAULT_HOST);
        stubsChannel.setIdleTimeout(SERVER_CONNECTOR_IDLETIME_MILLIS);

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
            stubsChannel.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
        }

        final String status = String.format("Stubs portal configured at http://%s:%s",
                stubsChannel.getHost(), stubsChannel.getPort());
        statuses.add(status);

        currentStubsPort = stubsChannel.getPort();

        return stubsChannel;
    }

    private ServerConnector buildStubsSslConnector(final Server server) throws IOException {
        final boolean enableAlpnAndHttp2 = commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        if (enableAlpnAndHttp2) {
            // See SslUtils static { ... }
            System.setProperty("overrideDisabledAlgorithms", "false");
        }

        SslUtils.initStatic();

        final String keystorePath = commandLineArgs.getOrDefault(CommandLineInterpreter.OPTION_KEYSTORE, null);
        final String keystorePassword = commandLineArgs.getOrDefault(CommandLineInterpreter.OPTION_KEYPASS, null);

        if ((ObjectUtils.isNull(keystorePath) && ObjectUtils.isNotNull(keystorePassword)) ||
                (ObjectUtils.isNotNull(keystorePath) && ObjectUtils.isNull(keystorePassword))) {
            final String misConfigMsg = String.format("When provided, both flags must be set, got: %s=%s and %s=%s",
                    CommandLineInterpreter.OPTION_KEYSTORE,
                    keystorePath,
                    CommandLineInterpreter.OPTION_KEYPASS,
                    keystorePassword);
            ANSITerminal.warn(misConfigMsg);
            LOGGER.warn(misConfigMsg);
        }

        HttpConfiguration httpConfiguration = constructHttpConfiguration();
        httpConfiguration.setSecureScheme(HttpScheme.HTTPS.asString());
        httpConfiguration.setSecurePort(getStubsSslPort(commandLineArgs));
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());

        final SslContextFactory sslContextFactory = constructSslContextFactory(keystorePassword, keystorePath);
        final ServerConnector sslConnector = enableAlpnAndHttp2 ?
                buildSslConnectorWithHttp2Alpn(server, httpConfiguration, sslContextFactory) :
                buildSslConnectorWithHttp1(server, httpConfiguration, sslContextFactory);

        sslConnector.setPort(getStubsSslPort(commandLineArgs));
        sslConnector.setHost(DEFAULT_HOST);
        sslConnector.setName(SSL_CONNECTOR_NAME);
        sslConnector.setIdleTimeout(SERVER_CONNECTOR_IDLETIME_MILLIS);

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
            sslConnector.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
        }

        final HashSet<String> supportedTlsProtocals = new HashSet<>(Arrays.asList(sslContextFactory.getIncludeProtocols()));
        final String status = String.format("Stubs portal configured with TLS at https://%s:%s using %s",
                sslConnector.getHost(), sslConnector.getPort(), (ObjectUtils.isNull(keystorePath) ? "internal self-signed certificate" : "provided " + keystorePath));
        statuses.add(status);

        final String tlsStatus = String.format("Stubs portal supports TLS protocol versions: %s", supportedTlsProtocals);
        statuses.add(tlsStatus + (enableAlpnAndHttp2 ? " with ALPN extension on HTTP/2" : ""));

        if (!new HashSet<>(asList(SslUtils.enabledProtocols())).contains(TLS_v1_3)) {
            final String noTls13Msg = String.format("TLSv1.3 is not supported in JDK v%s, %s",
                    System.getProperty("java.runtime.version"),
                    System.getProperty("java.vendor"));
            statuses.add(noTls13Msg);
        }

        currentStubsSslPort = sslConnector.getPort();

        return sslConnector;
    }

    private SslContextFactory constructSslContextFactory(final String keystorePassword, final String keystorePath) throws IOException {

        // https://www.eclipse.org/jetty/documentation/jetty-9/index.html#configuring-ssl

        // https://github.com/eclipse/jetty.project/issues/860
        // https://github.com/eclipse/jetty.project/issues/2807
        // https://github.com/eclipse/jetty.project/issues/3773
        // https://github.com/eclipse/jetty.project/issues/5039
        final SslContextFactory sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setExcludeProtocols();
        sslContextFactory.setIncludeProtocols(SslUtils.enabledProtocols());

        sslContextFactory.setExcludeCipherSuites();
        sslContextFactory.setIncludeCipherSuites(SslUtils.includedCipherSuites());

        if (ObjectUtils.isNull(keystorePath)) {
            // Commands used to generate the following self-signed certificate:
            // See src/main/resources/ssl/stubby4j.self.signed.v3.commands.txt
            final URL keyURL = this.getClass().getResource(getSelfSignedKeyStorePath());
            try (final Resource keyStoreResource = Resource.newResource(keyURL)) {
                // Usually, we'll use a keystore when we are a server and want to use HTTPS.
                // During an SSL handshake, the server looks up the private key from the
                // keystore and presents its corresponding public key and certificate to the client.
                sslContextFactory.setKeyStoreResource(keyStoreResource);
                sslContextFactory.setKeyStoreType("PKCS12");
                sslContextFactory.setKeyStorePassword("stubby4j");
                sslContextFactory.setKeyManagerPassword("stubby4j");

                return sslContextFactory;
            }
        }

        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keystorePassword);
        sslContextFactory.setKeyStorePath(keystorePath);

        return sslContextFactory;
    }

    private HttpConfiguration constructHttpConfiguration() {
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendServerVersion(true);
        httpConfiguration.setSendXPoweredBy(true);
        httpConfiguration.setOutputBufferSize(32768);
        httpConfiguration.setRequestHeaderSize(8192);
        httpConfiguration.setResponseHeaderSize(8192);

        return httpConfiguration;
    }

    private ServerConnector buildSslConnectorWithHttp1(final Server server,
                                                       final HttpConfiguration httpConfiguration,
                                                       final SslContextFactory sslContextFactory) {
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, PROTOCOL_HTTP_1_1);
        return new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpConfiguration));
    }

    private ServerConnector buildSslConnectorWithHttp2Alpn(final Server server,
                                                           final HttpConfiguration httpConfiguration,
                                                           final SslContextFactory sslContextFactory) {
        // https://www.eclipse.org/jetty/documentation/jetty-9/index.html#alpn-chapter

        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        final ALPNServerConnectionFactory alpnServerConnectionFactory = new ALPNServerConnectionFactory(PROTOCOL_HTTP_2);

        final HTTP2ServerConnectionFactory http2ServerConnectionFactory = new HTTP2ServerConnectionFactory(httpConfiguration);

        // Annoying cURL notice in response: Connection state changed (MAX_CONCURRENT_STREAMS == N)!
        // https://github.com/curl/curl/blob/63c76681827b5ae9017f6c981003cd75e5f127de/lib/http2.h#L32
        http2ServerConnectionFactory.setMaxConcurrentStreams(100);
        return new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, alpnServerConnectionFactory.getProtocol()),
                alpnServerConnectionFactory,
                http2ServerConnectionFactory);
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

    List<String> getStatuses() {
        return statuses;
    }
}
