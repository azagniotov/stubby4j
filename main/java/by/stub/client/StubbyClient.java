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

package by.stub.client;

import by.stub.annotations.CoberturaIgnore;
import by.stub.cli.CommandLineInterpreter;
import by.stub.http.StubbyHttpTransport;
import by.stub.server.JettyFactory;
import by.stub.server.StubbyManager;
import by.stub.server.StubbyManagerFactory;
import by.stub.utils.ObjectUtils;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;

import java.net.URL;

public final class StubbyClient {

   private StubbyManager stubbyManager;

   public StubbyClient() {

   }

   /**
    * Starts stubby using default ports of Stubs (8882), Admin (8889) and TlsStubs portals (7443) on localhost.
    *
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file
    * @throws Exception
    */
   @CoberturaIgnore
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
   @CoberturaIgnore
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
   @CoberturaIgnore
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
   @CoberturaIgnore
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
   @CoberturaIgnore
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
      final String[] args = new String[]{"-m", "-l", addressToBind, "-s", String.valueOf(stubsPort), "-a", String.valueOf(adminPort), "-t", String.valueOf(tlsPort), "-d", yamlConfigurationFilename};
      final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
      commandLineInterpreter.parseCommandLine(args);
      stubbyManager = new StubbyManagerFactory().construct(yamlConfigurationFilename, commandLineInterpreter.getCommandlineParams());
      stubbyManager.startJetty();
   }

   /**
    * Stops Jetty if it is up
    *
    * @throws Exception
    */
   @CoberturaIgnore
   public void stopJetty() throws Exception {
      if (ObjectUtils.isNotNull(stubbyManager)) {
         stubbyManager.stopJetty();
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
   @CoberturaIgnore
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
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param port               TLS port
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port, final String encodedCredentials) throws Exception {
      final StubbyRequest stubbyRequest = new StubbyRequest(HttpSchemes.HTTPS, HttpMethods.GET, uri, host, port, encodedCredentials);

      return makeRequest(stubbyRequest);
   }

   /**
    * Makes GET HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param stubsPort          port that stubby4j Stubs is running on
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGet(final String host, final String uri, final int stubsPort, final String encodedCredentials) throws Exception {
      final StubbyRequest stubbyRequest = new StubbyRequest(HttpSchemes.HTTP, HttpMethods.GET, uri, host, stubsPort, encodedCredentials);

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
    * @param uri                URI for the HTTP request
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetUsingDefaults(final String uri, final String encodedCredentials) throws Exception {
      return doGet(JettyFactory.DEFAULT_HOST, uri, JettyFactory.DEFAULT_STUBS_PORT, encodedCredentials);
   }

   /**
    * Makes POST HTTP request to stubby
    *
    * @param host      host that stubby4j is running on
    * @param uri       URI for the HTTP request
    * @param stubsPort port that stubby4j Stubs is running on
    * @param post      data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String post) throws Exception {
      return doPost(host, uri, stubsPort, null, post);
   }

   /**
    * Makes POST HTTP request to stubby
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param stubsPort          port that stubby4j Stubs is running on
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @param post               data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPost(final String host, final String uri, final int stubsPort, final String encodedCredentials, final String post) throws Exception {
      final StubbyRequest stubbyRequest = new StubbyRequest(HttpSchemes.HTTP, HttpMethods.POST, uri, host, stubsPort, encodedCredentials, post);

      return makeRequest(stubbyRequest);
   }

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882
    *
    * @param uri  URI for the HTTP request
    * @param post data to POST to the server
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String post) throws Exception {
      return doPostUsingDefaults(uri, post, null);
   }

   /**
    * Makes POST HTTP request to stubby running on default host and port - localhost:8882.
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param uri                URI for the HTTP request
    * @param post               data to POST to the server
    * @param encodedCredentials Base 64 encoded username and password for the basic authorisation HTTP header
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doPostUsingDefaults(final String uri, final String post, final String encodedCredentials) throws Exception {
      return doPost(JettyFactory.DEFAULT_HOST, uri, JettyFactory.DEFAULT_STUBS_PORT, encodedCredentials, post);
   }

   /**
    * Updated stubbed data with new data. This method creates a POST request to Admin portal
    *
    * @param url       fully constructed URL which included HTTP scheme, host and port
    * @param stubsData data to post
    */
   public StubbyResponse updateStubbedData(final String url, final String stubsData) throws Exception {
      final URL adminUrl = new URL(url);

      return makeRequest(adminUrl.getProtocol(), HttpMethods.POST, adminUrl.getHost(), adminUrl.getPath(), adminUrl.getPort(), stubsData);
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
      final StubbyRequest stubbyRequest = new StubbyRequest(scheme, method, uri, host, port, null, post);

      return makeRequest(stubbyRequest);
   }

   private StubbyResponse makeRequest(final StubbyRequest stubbyRequest) throws Exception {
      return new StubbyHttpTransport().getResponse(
         stubbyRequest.getMethod(),
         stubbyRequest.constructFullUrl(),
         stubbyRequest.getPost(),
         stubbyRequest.getBase64encodedCredentials(),
         stubbyRequest.calculatePostLength());
   }
}