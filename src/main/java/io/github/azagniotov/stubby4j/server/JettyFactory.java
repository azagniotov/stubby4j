package io.github.azagniotov.stubby4j.server;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.handlers.AjaxEndpointStatsHandler;
import io.github.azagniotov.stubby4j.handlers.AjaxResourceContentHandler;
import io.github.azagniotov.stubby4j.handlers.FaviconHandler;
import io.github.azagniotov.stubby4j.handlers.JsonErrorHandler;
import io.github.azagniotov.stubby4j.handlers.StatusPageHandler;
import io.github.azagniotov.stubby4j.handlers.StubDataRefreshActionHandler;
import io.github.azagniotov.stubby4j.handlers.StubsPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.MimeTypes;
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
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@SuppressWarnings("serial")
@GeneratedCodeCoverageExclusion
public final class JettyFactory {

    public static final int DEFAULT_ADMIN_PORT = 8889;
    public static final int DEFAULT_STUBS_PORT = 8882;
    public static final int DEFAULT_SSL_PORT = 7443;
    public static final String DEFAULT_HOST = "localhost";
    private static final int SERVER_CONNECTOR_IDLETIME_MILLIS = 45000;
    private static final String PROTOCOL_HTTP_1_1 = "HTTP/1.1";
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

    Server construct() throws IOException {
        final Server server = new Server();
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);

        server.setConnectors(buildConnectors(server));
        server.setHandler(constructHandlers());

        return server;
    }

    private ContextHandlerCollection constructHandlers() {

        final JettyContext jettyContext = new JettyContext(currentHost, currentStubsPort, currentStubsSslPort, currentAdminPort);
        final ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]
                {
                        constructHandler(STUBS_CONNECTOR_NAME, "/favicon.ico", gzipHandler(new FaviconHandler())),
                        constructHandler(STUBS_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubRepository))),

                        constructHandler(SSL_CONNECTOR_NAME, "/favicon.ico", gzipHandler(new FaviconHandler())),
                        constructHandler(SSL_CONNECTOR_NAME, ROOT_PATH_INFO, gzipHandler(new StubsPortalHandler(stubRepository))),

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

        String keystorePath = null;
        String password = "stubby4j";
        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_KEYSTORE)
                && commandLineArgs.containsKey(CommandLineInterpreter.OPTION_KEYPASS)) {
            password = commandLineArgs.get(CommandLineInterpreter.OPTION_KEYPASS);
            keystorePath = commandLineArgs.get(CommandLineInterpreter.OPTION_KEYSTORE);
        }

        HttpConfiguration httpConfiguration = constructHttpConfiguration();
        httpConfiguration.setSecureScheme(HttpScheme.HTTPS.asString());
        httpConfiguration.setSecurePort(getStubsSslPort(commandLineArgs));
        httpConfiguration.addCustomizer(new SecureRequestCustomizer());

        final SslContextFactory sslContextFactory = constructSslContextFactory(password, keystorePath);

        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, PROTOCOL_HTTP_1_1),
                new HttpConnectionFactory(httpConfiguration));
        sslConnector.setPort(getStubsSslPort(commandLineArgs));

        sslConnector.setHost(DEFAULT_HOST);
        sslConnector.setName(SSL_CONNECTOR_NAME);
        sslConnector.setIdleTimeout(SERVER_CONNECTOR_IDLETIME_MILLIS);

        if (commandLineArgs.containsKey(CommandLineInterpreter.OPTION_ADDRESS)) {
            sslConnector.setHost(commandLineArgs.get(CommandLineInterpreter.OPTION_ADDRESS));
        }

        final String status = String.format("Stubs portal configured with TLS at https://%s:%s using %s keystore",
                sslConnector.getHost(), sslConnector.getPort(), (ObjectUtils.isNull(keystorePath) ? "internal" : "provided " + keystorePath));
        statuses.add(status);

        currentStubsSslPort = sslConnector.getPort();

        return sslConnector;
    }

    private SslContextFactory constructSslContextFactory(final String password, final String keystorePath) throws IOException {

        // https://www.eclipse.org/jetty/documentation/jetty-9/index.html#configuring-ssl

        // https://github.com/eclipse/jetty.project/issues/860
        // https://github.com/eclipse/jetty.project/issues/2807
        // https://github.com/eclipse/jetty.project/issues/3773
        // https://github.com/eclipse/jetty.project/issues/5039
        final SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePassword(password);
        sslContextFactory.setKeyManagerPassword(password);
        sslContextFactory.setIncludeProtocols(SslUtils.TLS_v1, SslUtils.TLS_v1_1, SslUtils.TLS_v1_2, SslUtils.TLS_v1_3);
        sslContextFactory.setExcludeCipherSuites();

        sslContextFactory.setIncludeCipherSuites(SslUtils.includedCipherSuites());

        relaxSslTrustManager();

        if (ObjectUtils.isNull(keystorePath)) {
            // Commands used to generate the following self-signed certificate:
            // 1. openssl req -x509 -newkey rsa:4096 -keyout stubby4j.key.pem -out stubby4j.crt.pem -sha256 -subj "/C=AU/ST=Victoria/L=Melbourne/O=stubby4j/OU=Org/CN=localhost" -days 10950
            // 2. openssl pkcs12 -inkey stubby4j.key.pem -in stubby4j.crt.pem -export -out stubby4j.pkcs12
            final URL keyURL = this.getClass().getResource("/ssl/stubby4j.pkcs12");
            try (final Resource keyStoreResource = Resource.newResource(keyURL)) {
                // Usually, we'll use a keystore when we are a server and want to use HTTPS.
                // During an SSL handshake, the server looks up the private key from the
                // keystore and presents its corresponding public key and certificate to the client.
                sslContextFactory.setKeyStoreResource(keyStoreResource);
                sslContextFactory.setKeyStoreType("PKCS12");

                return sslContextFactory;
            }
        }

        sslContextFactory.setKeyStorePath(keystorePath);

        return sslContextFactory;
    }

    private void relaxSslTrustManager() {
        try {
            new FakeX509TrustManager().allowAllSSL();
        } catch (final Exception ex) {
            throw new RuntimeException(ex.toString(), ex);
        }
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
