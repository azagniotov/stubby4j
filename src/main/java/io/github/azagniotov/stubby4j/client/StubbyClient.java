/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import io.github.azagniotov.stubby4j.utils.CollectionUtils;
import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;

@GeneratedCodeClassCoverageExclusion
public final class StubbyClient {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private StubbyManager stubbyManager;

    public StubbyClient() {}

    /**
     * Starts stubby using default ports of Stubs (8882), Admin (8889) and TlsStubs portals (7443) on localhost.
     *
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */
    public void startJetty(final String yamlConfigurationFilename) throws Exception {
        startJetty(
                JettyFactory.DEFAULT_STUBS_PORT,
                JettyFactory.DEFAULT_SSL_PORT,
                JettyFactory.DEFAULT_ADMIN_PORT,
                JettyFactory.DEFAULT_HOST,
                yamlConfigurationFilename);
    }

    /**
     * Starts stubby using default ports of Admin (8889) and TlsStubs portals (7443), and given Stubs portal port  on localhost.
     *
     * @param stubsPort                 Stubs portal port
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */
    public void startJetty(final int stubsPort, final String yamlConfigurationFilename) throws Exception {
        startJetty(
                stubsPort,
                JettyFactory.DEFAULT_SSL_PORT,
                JettyFactory.DEFAULT_ADMIN_PORT,
                JettyFactory.DEFAULT_HOST,
                yamlConfigurationFilename);
    }

    /**
     * Starts stubby using default port of TlsStubs (7443), and given Stubs and Admin portals ports  on localhost.
     *
     * @param stubsPort                 Stubs portal port
     * @param adminPort                 Admin portal port
     * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
     * @throws Exception
     */
    public void startJetty(final int stubsPort, final int adminPort, final String yamlConfigurationFilename)
            throws Exception {
        startJetty(
                stubsPort,
                JettyFactory.DEFAULT_SSL_PORT,
                adminPort,
                JettyFactory.DEFAULT_HOST,
                yamlConfigurationFilename);
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
    public void startJetty(
            final int stubsPort, final int tlsPort, final int adminPort, final String yamlConfigurationFilename)
            throws Exception {
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
    public void startJetty(
            final int stubsPort,
            final int adminPort,
            final String addressToBind,
            final String yamlConfigurationFilename)
            throws Exception {
        startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, adminPort, addressToBind, yamlConfigurationFilename);
    }

    /**
     * Starts stubby using given Stubs, TlsStubs, Admin portals ports and host address.
     *
     * @param stubsPort                 Stubs portal port
     * @param tlsPort                   TLS Stubs portal port
     * @param adminPort                 Admin portal port
     * @param addressToBind             Address to bind Jetty
     * @param yamlConfigurationFilename An absolute or relative file path for YAML stubs configuration file.
     * @param flags                     Optional additional stubby4j command line arguments.
     * @throws Exception
     */
    public void startJetty(
            final int stubsPort,
            final int tlsPort,
            final int adminPort,
            final String addressToBind,
            final String yamlConfigurationFilename,
            final String... flags)
            throws Exception {
        final String[] defaultFlags = new String[] {
            "-m",
            "-l",
            addressToBind,
            "-s",
            String.valueOf(stubsPort),
            "-a",
            String.valueOf(adminPort),
            "-t",
            String.valueOf(tlsPort)
        };
        final String[] args = CollectionUtils.concatWithArrayCopy(defaultFlags, flags);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(args);

        final File configFile = new File(yamlConfigurationFilename);
        final CompletableFuture<YamlParseResultSet> stubLoadComputation = parseYamlAsync(configFile);

        stubbyManager = new StubbyManagerFactory()
                .construct(configFile, commandLineInterpreter.getCommandlineParams(), stubLoadComputation);
        stubbyManager.startJetty();
    }

    /**
     * Binds stubby4j to the provided address and Stubs, Stubs on TLD & Admin portal ports.
     * <p>
     * This convenience method allows to start stubby4j without providing a path to YAML in the local filesystem.
     * It is the responsibility of consumers of this API to provide stubs YAML configuration payload of type String.
     * <p>
     * Please note: the provided stubs YAML string payload, must be a standalone YAML. Stubs YAML config payload in the
     * split format (see https://stubby4j.com/docs/http_endpoint_configuration_howto.html#splitting-main-yaml-config)
     * is NOT supported by this API.
     * <p>
     * Consumers can leverage the {@link NetworkPortUtils#findAvailableTcpPort()} class to start stubby4j
     * on a random port and get the port number at runtime.
     *
     * @param stubsYamlConfigurationData Stubs YAML configuration as a string.
     * @param stubsPort                  Stubs portal port, e.g.: 8882
     * @param tlsPort                    TLS Stubs portal port, e.g.: 7443
     * @param adminPort                  Admin portal port, e.g.: 8889
     * @param addressToBind              Address to bind Jetty, e.g.: 127.0.0.1, localhost or 192.168.0.2
     * @param additionalFlags            Optional additional stubby4j command line arguments. See https://stubby4j.com/#command-line-switches
     * @throws Exception
     * @see io.github.azagniotov.stubby4j.utils.NetworkPortUtils
     */
    public void startJettyYamless(
            final String stubsYamlConfigurationData,
            final int stubsPort,
            final int tlsPort,
            final int adminPort,
            final String addressToBind,
            final String... additionalFlags)
            throws Exception {
        final String[] defaultFlags = new String[] {
            "-l",
            addressToBind,
            "-s",
            String.valueOf(stubsPort),
            "-a",
            String.valueOf(adminPort),
            "-t",
            String.valueOf(tlsPort)
        };
        final String[] args = CollectionUtils.concatWithArrayCopy(defaultFlags, additionalFlags);

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(args);

        final String emptyYaml = "- request:\n" + "    method: [GET]\n"
                + "    url: /stubby4j/default/placeholder/stub\n"
                + "\n"
                + "  response:\n"
                + "    status: 302";

        final File tempTargetFile =
                Files.createTempFile("stubby4jTempYamlConfig_", ".yaml").toFile();
        try (final InputStream inputStream = new ByteArrayInputStream(StringUtils.getBytesUtf8(emptyYaml))) {
            Files.copy(inputStream, tempTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        tempTargetFile.deleteOnExit();

        final CompletableFuture<YamlParseResultSet> stubLoadComputation = parseYamlAsync(tempTargetFile);

        stubbyManager = new StubbyManagerFactory()
                .construct(tempTargetFile, commandLineInterpreter.getCommandlineParams(), stubLoadComputation);
        stubbyManager.startJetty();

        final String adminUrl = String.format("http://%s:%s", addressToBind, adminPort);
        final StubbyResponse adminPortalResponse = updateStubbedData(adminUrl, stubsYamlConfigurationData);

        assert adminPortalResponse.statusCode() == HttpStatus.CREATED_201;
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
    public StubbyResponse doGetOverSsl(
            final String host, final String uri, final int port, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTPS.asString().toLowerCase(Locale.US),
                HttpMethod.GET.asString(),
                uri,
                host,
                port,
                authorization);

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
    public StubbyResponse doGet(
            final String host, final String uri, final int stubsPort, final Authorization authorization)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTP.asString().toLowerCase(Locale.US),
                HttpMethod.GET.asString(),
                uri,
                host,
                stubsPort,
                authorization);

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
    public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String payload)
            throws Exception {
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
    public StubbyResponse doPost(
            final String host,
            final String uri,
            final int stubsPort,
            final Authorization authorization,
            final String payload)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTP.asString().toLowerCase(Locale.US),
                HttpMethod.POST.asString(),
                uri,
                host,
                stubsPort,
                authorization,
                payload);

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
    public StubbyResponse doPostUsingDefaults(final String uri, final String payload, final Authorization authorization)
            throws Exception {
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
    public StubbyResponse doPut(
            final String host,
            final String uri,
            final int stubsPort,
            final Authorization authorization,
            final String payload)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTP.asString().toLowerCase(Locale.US),
                HttpMethod.PUT.asString(),
                uri,
                host,
                stubsPort,
                authorization,
                payload);

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
    public StubbyResponse doPutOverSsl(
            final String host,
            final String uri,
            final int stubsPort,
            final Authorization authorization,
            final String payload)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTPS.asString().toLowerCase(Locale.US),
                HttpMethod.PUT.asString(),
                uri,
                host,
                stubsPort,
                authorization,
                payload);

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
    public StubbyResponse doDelete(
            final String host, final String uri, final int stubsPort, final Authorization authorization)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTP.asString().toLowerCase(Locale.US),
                HttpMethod.DELETE.asString(),
                uri,
                host,
                stubsPort,
                authorization);

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
    public StubbyResponse doDeleteOverSsl(
            final String host, final String uri, final int port, final Authorization authorization) throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(
                HttpScheme.HTTPS.asString().toLowerCase(Locale.US),
                HttpMethod.DELETE.asString(),
                uri,
                host,
                port,
                authorization);

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
    public StubbyResponse updateStubbedData(final String url, HttpMethod method, final String stubsData)
            throws Exception {
        if (method != HttpMethod.PUT && method != HttpMethod.POST) {
            throw new IllegalArgumentException("Unexpected HTTP method: " + method.asString());
        }
        final URL adminUrl = new URL(url);

        return makeRequest(
                adminUrl.getProtocol(),
                method.asString(),
                adminUrl.getHost(),
                adminUrl.getPath(),
                adminUrl.getPort(),
                stubsData);
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

        return makeRequest(
                adminUrl.getProtocol(),
                HttpMethod.POST.asString(),
                adminUrl.getHost(),
                adminUrl.getPath(),
                adminUrl.getPort(),
                stubsData);
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
    public StubbyResponse makeRequest(
            final String scheme,
            final String method,
            final String host,
            final String uri,
            final int port,
            final String post)
            throws Exception {
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
    public StubbyResponse makeRequest(
            final String scheme,
            final String method,
            final String host,
            final String uri,
            final int port,
            final String post,
            final Authorization authorization)
            throws Exception {
        final StubbyRequest stubbyRequest = new StubbyRequest(scheme, method, uri, host, port, authorization, post);

        return makeRequest(stubbyRequest);
    }

    private StubbyResponse makeRequest(final StubbyRequest stubbyRequest) throws Exception {
        final Map<String, String> headers = new HashMap<>();

        if (ObjectUtils.isNotNull(stubbyRequest.getAuthorization())) {
            headers.put("Authorization", stubbyRequest.getAuthorization().asFullValue());
        }

        return new StubbyHttpTransport()
                .request(
                        stubbyRequest.getMethod(),
                        stubbyRequest.constructFullUrl(),
                        stubbyRequest.getPost(),
                        headers,
                        stubbyRequest.calculatePostLength());
    }

    private CompletableFuture<YamlParseResultSet> parseYamlAsync(final File configFile) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return new YamlParser().parse(configFile.getParent(), configFile);
                    } catch (IOException ioEx) {
                        throw new UncheckedIOException(ioEx);
                    }
                },
                EXECUTOR_SERVICE);
    }
}
