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

import by.stub.cli.CommandLineInterpreter;
import by.stub.server.JettyFactory;
import by.stub.server.JettyManager;
import by.stub.server.JettyManagerFactory;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class StubbyClient {

   private JettyManager jettyManager;

   public StubbyClient() {

   }

   /**
    * Starts stubby using default ports of Stubs (8882), Admin (8889) and SslStubs portals (7443)
    *
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final String yamlConfigurationFilename) throws Exception {
      startJetty(JettyFactory.DEFAULT_STUBS_PORT, JettyFactory.DEFAULT_SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, yamlConfigurationFilename);
   }

   /**
    * Starts stubby using default ports of Admin (8889) and SslStubs portals (7443), and given Stubs portal port
    *
    * @param stubsPort                 Stubs portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final String yamlConfigurationFilename) throws Exception {
      startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, JettyFactory.DEFAULT_ADMIN_PORT, yamlConfigurationFilename);
   }

   /**
    * Starts stubby using default port of SslStubs (7443), and given Stubs and Admin portals ports
    *
    * @param stubsPort                 Stubs portal port
    * @param adminPort                 Admin portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int adminPort, final String yamlConfigurationFilename) throws Exception {
      startJetty(stubsPort, JettyFactory.DEFAULT_SSL_PORT, adminPort, yamlConfigurationFilename);
   }

   /**
    * Starts stubby using given Stubs, SslStubs and Admin portals ports
    *
    * @param stubsPort                 Stubs portal port
    * @param sslPort                   SSL Stubs portal port
    * @param adminPort                 Admin portal port
    * @param yamlConfigurationFilename an absolute or relative file path for YAML stubs configuration file.
    * @throws Exception
    */
   public void startJetty(final int stubsPort, final int sslPort, final int adminPort, final String yamlConfigurationFilename) throws Exception {
      final String clientPortString = String.format("%s", stubsPort);
      final String sslPortString = String.format("%s", sslPort);
      final String adminPortString = String.format("%s", adminPort);

      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineInterpreter.OPTION_CLIENTPORT, clientPortString);
      params.put(CommandLineInterpreter.OPTION_SSLPORT, sslPortString);
      params.put(CommandLineInterpreter.OPTION_ADMINPORT, adminPortString);

      final String[] args = new String[]{"-m", "-s", clientPortString, "-a", adminPortString, "-t", sslPortString, "-d", yamlConfigurationFilename};
      new CommandLineInterpreter().parseCommandLine(args);

      jettyManager = new JettyManagerFactory().construct(yamlConfigurationFilename, params);
      jettyManager.startJetty();
   }

   /**
    * Stops Jetty if it is up
    *
    * @throws Exception
    */
   public void stopJetty() throws Exception {
      if (jettyManager != null && jettyManager.isJettyUp()) {
         jettyManager.stopJetty();
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
    * Makes GET HTTP request to stubby over SSL on stubby4j default SSL port: 7443
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
    * Makes GET HTTP request to stubby over SSL on stubby4j
    *
    * @param host host that stubby4j is running on
    * @param uri  URI for the HTTP request
    * @param port SSL port
    * @return StubbyResponse with HTTP status code and message from the server
    * @throws Exception
    */
   public StubbyResponse doGetOverSsl(final String host, final String uri, final int port) throws Exception {
      return doGetOverSsl(host, uri, port, null);
   }

   /**
    * Makes GET HTTP request to stubby over SSL on stubby4j default SSL port: 7443
    * Also sets basic authorisation HTTP header using provided encoded credentials.
    * The credentials should be base-64 encoded using the following format - username:password
    *
    * @param host               host that stubby4j is running on
    * @param uri                URI for the HTTP request
    * @param port               SSL port
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
      final StubbyHttpTransport stubbyHttpTransport = new StubbyHttpTransport(stubbyRequest);
      final HttpURLConnection connection = stubbyHttpTransport.constructHttpConnection();

      try {
         connection.connect();
         final StubbyResponseFactory responseFactory = new StubbyResponseFactory(connection);

         return responseFactory.construct();
      } finally {
         connection.disconnect();
      }
   }

}