package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@GeneratedCodeCoverageExclusion
public final class StubbyClient {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private StubbyManager stubbyManager;

    public StubbyClient() {

    }

    /**
     * Starts stubby using default ports of Stubs (8882), Admin (8889) and TlsStubs portals (7443) on localhost.
     *
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */

    public void startJetty(final String yamlConfigurationFilename) throws Exception {
        startJetty(JettyFactory.DEFAULT_STUBS_PORT, JettyFactory.DEFAULT_SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, JettyFactory.DEFAULT_HOST, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using default ports of Admin (8889) and TlsStubs portals (7443), and given Stubs portal port  on localhost.
     *
     * @param stubsPort                 Stubs portal port
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */

    public void startJetty(final int stubsPort, final String yamlConfigurationFilename) throws Exception {
        startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, JettyFactory.DEFAULT_HOST, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using default port of TlsStubs (7443), and given Stubs and Admin portals ports  on localhost.
     *
     * @param stubsPort                 Stubs portal port
     * @param adminPort                 Admin portal port
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */

    public void startJetty(final int stubsPort, final int adminPort, final String yamlConfigurationFilename) throws Exception {
        startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, adminPort, JettyFactory.DEFAULT_HOST, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using given Stubs, TlsStubs and Admin portals ports on localhost.
     *
     * @param stubsPort                 Stubs portal port
     * @param tlsPort                   TLS Stubs portal port
     * @param adminPort                 Admin portal port
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */

    public void startJetty(final int stubsPort, final int tlsPort, final int adminPort, final String yamlConfigurationFilename) throws Exception {
        startJetty(stubsPort, tlsPort, adminPort, JettyFactory.DEFAULT_HOST, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using default port of TlsStubs (7443), and given Stubs and Admin portals ports on a given host address.
     *
     * @param stubsPort                 Stubs portal port
     * @param adminPort                 Admin portal port
     * @param addressToBind             Address to bind Jetty
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */

    public void startJetty(final int stubsPort, final int adminPort, final String addressToBind, final String yamlConfigurationFilename) throws Exception {
        startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, adminPort, addressToBind, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using given Stubs, TlsStubs, Admin portals ports and host address.
     *
     * @param stubsPort                 Stubs portal port
     * @param tlsPort                   TLS Stubs portal port
     * @param adminPort                 Admin portal port
     * @param addressToBind             Address to bind Jetty
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
     * @throws Exception
     */

    public void startJetty(final int stubsPort, final int tlsPort, final int adminPort, final String addressToBind, final String yamlConfigurationFilename) throws Exception {
        final String[] args = new String[]{"-m", "-l", addressToBind, "-s", String.valueOf(stubsPort), "-a", String.valueOf(adminPort), "-t", String.valueOf(tlsPort)};
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(args);

        final File configFile = new File(yamlConfigurationFilename);
        final CompletableFuture<YamlParseResultSet> stubLoadComputation = parseYamlAsync(configFile);

        stubbyManager = new StubbyManagerFactory().construct(configFile, commandLineInterpreter.getCommandlineParams(), stubLoadComputation);
        stubbyManager.startJetty();
    }

    /**
     * Starts stubby using given Stubs, TlsStubs, Admin portals ports and host address without YAML configuration file.
     *
     * @param stubsPort     Stubs portal port
     * @param tlsPort       TLS Stubs portal port
     * @param adminPort     Admin portal port
     * @param addressToBind Address to bind Jetty
     * @throws Exception
     */

    public void startJettyYamless(final int stubsPort, final int tlsPort, final int adminPort, final String addressToBind) throws Exception {
        final String[] args = new String[]{"-m", "-l", addressToBind, "-s", String.valueOf(stubsPort), "-a", String.valueOf(adminPort), "-t", String.valueOf(tlsPort)};
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(args);
        final URL url = StubbyClient.class.getResource("/yaml/empty-stub.yaml");

        final File configFile = new File(url.getFile());
        final CompletableFuture<YamlParseResultSet> stubLoadComputation = parseYamlAsync(configFile);

        stubbyManager = new StubbyManagerFactory().construct(configFile, commandLineInterpreter.getCommandlineParams(), stubLoadComputation);
        stubbyManager.startJetty();
    }

    /**
     * Stops Jetty if it is up
     *
     * @throws Exception
     */

    public void stopJetty() throws Exception {
        if (ObjectUtils.isNotNull(stubbyManager)) {
            stubbyManager.stopJetty();
        }
    }

    /**
     * Blocks until Jetty has finished
     *
     * @throws Exception
     */

    public void joinJetty() throws Exception {
        if (ObjectUtils.isNotNull(stubbyManager)) {
            stubbyManager.joinJetty();
        }
    }

    /**
     * Makes GET HTTP request to stubby
     *
     * @param host      host that stubby4j is running on
     * @param uri       URI for the HTTP request
     * @param stubsPort port that stubby4j Stubs is running on
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGet(final String host, final String uri, final int stubsPort) throws Exception {
        return doGet(host, uri, stubsPort, null);
    }

    /**
     * Makes GET HTTP request to stubby over TLS on stubby4j default TLS port: 7443
     *
     * @param host host that stubby4j is running on
     * @param uri  URI for the HTTP request
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGetOverSsl(final String host, final String uri) throws Exception {
        return doGetOverSsl(host, uri, JettyFactory.DEFAULT_SSL_PORT, null);
    }

    /**
     * Makes GET HTTP request to stubby over TLS on stubby4j
     *
     * @param host host that stubby4j is running on
     * @param uri  URI for the HTTP request
     * @param port TLS port
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGetOverSsl(final String host, final String uri, final int port) throws Exception {
        return doGetOverSsl(host, uri, port, null);
    }

    /**
     * Makes GET HTTP request to stubby over TLS on stubby4j default TLS port: 7443
     * Also sets basic authorisation HTTP header using provided encoded credentials.
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param port          TLS port
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGetOverSsl(final String host, final String uri, final int port, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTPS.asString().toLowerCase(Locale.US), HttpMethod.GET.asString(), uri, host, port, authorization);

        return makeRequest(stubbyRequest);
    }

    /**
     * Makes GET HTTP request to stubby
     * Also sets basic authorisation HTTP header using provided encoded credentials.
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param stubsPort     port that stubby4j Stubs is running on
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGet(final String host, final String uri, final int stubsPort, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTP.asString().toLowerCase(Locale.US), HttpMethod.GET.asString(), uri, host, stubsPort, authorization);

        return makeRequest(stubbyRequest);
    }


    /**
     * Makes GET HTTP request to stubby running on default host and port - localhost:8882
     *
     * @param uri URI for the HTTP request
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGetUsingDefaults(final String uri) throws Exception {
        return doGetUsingDefaults(uri, null);
    }

    /**
     * Makes GET HTTP request to stubby running on default host and port - localhost:8882.
     * Also sets basic authorisation HTTP header using provided encoded credentials.
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param uri           URI for the HTTP request
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doGetUsingDefaults(final String uri, final Authorization authorization) throws Exception {
        return doGet(JettyFactory.DEFAULT_HOST, uri, JettyFactory.DEFAULT_STUBS_PORT, authorization);
    }

    /**
     * Makes POST HTTP request to stubby
     *
     * @param host      host that stubby4j is running on
     * @param uri       URI for the HTTP request
     * @param stubsPort port that stubby4j Stubs is running on
     * @param payload   data to POST to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String payload) throws Exception {
        return doPost(host, uri, stubsPort, null, payload);
    }

    /**
     * Makes POST HTTP request to stubby
     * Also sets basic authorisation HTTP header using provided encoded credentials.
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param stubsPort     port that stubby4j Stubs is running on
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @param payload       data to POST to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTP.asString().toLowerCase(Locale.US), HttpMethod.POST.asString(), uri, host, stubsPort, authorization, payload);

        return makeRequest(stubbyRequest);
    }

    /**
     * Makes POST HTTP request to stubby running on default host and port - localhost:8882
     *
     * @param uri     URI for the HTTP request
     * @param payload data to POST to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPostUsingDefaults(final String uri, final String payload) throws Exception {
        return doPostUsingDefaults(uri, payload, null);
    }

    /**
     * Makes POST HTTP request to stubby running on default host and port - localhost:8882.
     * Also sets basic authorisation HTTP header using provided encoded credentials.
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param uri           URI for the HTTP request
     * @param payload       data to POST to the server
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPostUsingDefaults(final String uri, final String payload, final Authorization authorization) throws Exception {
        return doPost(JettyFactory.DEFAULT_HOST, uri, JettyFactory.DEFAULT_STUBS_PORT, authorization, payload);
    }

    /**
     * Makes PUT HTTP request to stubby
     * Also can set basic authorisation HTTP header using encoded credentials (if provided).
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param stubsPort     port that stubby4j Stubs is running on
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
     * @param payload       data to PUT to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPut(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTP.asString().toLowerCase(Locale.US), HttpMethod.PUT.asString(), uri, host, stubsPort, authorization, payload);

        return makeRequest(stubbyRequest);
    }

    /**
     * Makes PUT HTTP request to stubby over TLS on stubby4j default TLS port: 7443
     * <p></p>
     * Also can set basic authorisation HTTP header using encoded credentials (if provided).
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param stubsPort     port that stubby4j Stubs is running on
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
     * @param payload       data to PUT to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doPutOverSsl(final String host, final String uri, final int stubsPort, final Authorization authorization, final String payload) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTPS.asString().toLowerCase(Locale.US), HttpMethod.PUT.asString(), uri, host, stubsPort, authorization, payload);

        return makeRequest(stubbyRequest);
    }

    /**
     * Makes DELETE HTTP request to stubby
     * Also can set basic authorisation HTTP header using encoded credentials (if provided).
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param stubsPort     port that stubby4j Stubs is running on
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value (can be null)
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doDelete(final String host, final String uri, final int stubsPort, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTP.asString().toLowerCase(Locale.US), HttpMethod.DELETE.asString(), uri, host, stubsPort, authorization);

        return makeRequest(stubbyRequest);
    }

    /**
     * Makes DELETE HTTP request to stubby over TLS on stubby4j default TLS port: 7443
     * <p></p>
     * Also can set basic authorisation HTTP header using encoded credentials (if provided).
     * The credentials should be base-64 encoded using the following format - username:password
     *
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param port          TLS port
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse doDeleteOverSsl(final String host, final String uri, final int port, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(HttpScheme.HTTPS.asString().toLowerCase(Locale.US), HttpMethod.DELETE.asString(), uri, host, port, authorization);

        return makeRequest(stubbyRequest);
    }

    /**
     * Updated stubbed data with new data. This method creates an request to the provided URL.
     * This API meant to be used when making requests to Admin portal to update the stubby4j in-memory stubbed data.
     * <p>
     * For example: making POST to http://localhost:8889/ to re/create in-memory YAML config or
     * making a PUT to http://localhost:8889/proxy-config/some-unique-uuid.
     *
     * @param url       fully constructed URL which included HTTP scheme, host and port, e.g.: http://localhost:8889/some-unique-uuid
     * @param method    POST or PUT
     * @param stubsData data to post/put
     */
    public StubbyResponse updateStubbedData(final String url, HttpMethod method, final String stubsData) throws Exception {
        if (method != HttpMethod.PUT && method != HttpMethod.POST) {
            throw new IllegalArgumentException("Unexpected HTTP method: " + method.asString());
        }
        final URL adminUrl = new URL(url);

        return makeRequest(adminUrl.getProtocol(), method.asString(), adminUrl.getHost(), adminUrl.getPath(), adminUrl.getPort(), stubsData);
    }

    /**
     * Updated stubbed data with new data. This method creates an request to the provided URL.
     * This API meant to be used when making requests to Admin portal to create the stubby4j in-memory stubbed data.
     * <p>
     * For example: making POST to http://localhost:8889/.
     *
     * @param url       fully constructed URL which included HTTP scheme, host and port, e.g.: http://localhost:8889/
     * @param stubsData data to post
     */
    public StubbyResponse updateStubbedData(final String url, final String stubsData) throws Exception {
        final URL adminUrl = new URL(url);

        return makeRequest(adminUrl.getProtocol(), HttpMethod.POST.asString(), adminUrl.getHost(), adminUrl.getPath(), adminUrl.getPort(), stubsData);
    }

    /**
     * Makes HTTP request to stubby.
     *
     * @param scheme HTTP protocol scheme, HTTP or HTTPS
     * @param method HTTP method, currently supported: GET, HEAD, PUT, POST
     * @param host   host that stubby4j is running on
     * @param uri    URI for the HTTP request
     * @param port   port that stubby4j Stubs is running on
     * @param post   data to POST to the server
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse makeRequest(final String scheme,
                                      final String method,
                                      final String host,
                                      final String uri,
                                      final int port,
                                      final String post) throws Exception {
        return makeRequest(scheme, method, host, uri, port, post, null);
    }

    /**
     * Makes HTTP request to stubby.
     *
     * @param scheme        HTTP protocol scheme, HTTP or HTTPS
     * @param method        HTTP method, currently supported: GET, HEAD, PUT, POST
     * @param host          host that stubby4j is running on
     * @param uri           URI for the HTTP request
     * @param port          port that stubby4j Stubs is running on
     * @param post          data to POST to the server
     * @param authorization {@link Authorization} object holding the HTTP header authorization type and value
     * @return StubbyResponse with HTTP status code and message from the server
     * @throws Exception
     */

    public StubbyResponse makeRequest(final String scheme,
                                      final String method,
                                      final String host,
                                      final String uri,
                                      final int port,
                                      final String post,
                                      final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(scheme, method, uri, host, port, authorization, post);

        return makeRequest(stubbyRequest);
    }

    private StubbyResponse makeRequest(final StubbyRequest stubbyRequest) throws Exception {
        final Map<String, String> headers = new HashMap<>();

        if (ObjectUtils.isNotNull(stubbyRequest.getAuthorization())) {
            headers.put("Authorization", stubbyRequest.getAuthorization().asFullValue());
        }

        return new StubbyHttpTransport().request(
                stubbyRequest.getMethod(),
                stubbyRequest.constructFullUrl(),
                stubbyRequest.getPost(),
                headers,
                stubbyRequest.calculatePostLength());
    }

    private CompletableFuture<YamlParseResultSet> parseYamlAsync(final File configFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new YamlParser().parse(configFile.getParent(), configFile);
            } catch (IOException ioEx) {
                throw new UncheckedIOException(ioEx);
            }
        }, EXECUTOR_SERVICE);
    }
}
