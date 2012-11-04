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

package by.stub.client.http;

import by.stub.cli.CommandLineIntepreter;
import by.stub.server.JettyFactory;
import by.stub.server.JettyManager;
import by.stub.server.JettyManagerFactory;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public final class StubbyClient {

   private JettyManager jettyManager;
   private String yamlConfigurationFilename;

   private StubbyClient() {

   }

   public StubbyClient(final String newYamlConfigurationFilename) {
      this.yamlConfigurationFilename = newYamlConfigurationFilename;
   }

   public void startJetty() throws Exception {
      startJetty(JettyFactory.DEFAULT_STUBS_PORT, JettyFactory.DEFAULT_ADMIN_PORT);
   }

   public void startJetty(final int clientPort, final int adminPort) throws Exception {

      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineIntepreter.OPTION_CLIENTPORT, String.format("%s", clientPort));
      params.put(CommandLineIntepreter.OPTION_ADMINPORT, String.format("%s", adminPort));

      jettyManager = new JettyManagerFactory().construct(yamlConfigurationFilename, params);
      jettyManager.startJetty();
   }

   public void stopJetty() throws Exception {
      if (jettyManager != null) {
         jettyManager.stopJetty();
      }
   }

   public ClientHttpResponse makeGetRequest(final String host, final String uri, final int stubsPort) throws IOException {
      return makeGetRequest(host, uri, stubsPort, null);
   }

   public ClientHttpResponse makeGetRequest(final String host, final String uri, final int stubsPort, final String basicAuth) throws IOException {
      final ClientHttpRequest clientHttpRequest = new ClientHttpRequest(HttpSchemes.HTTP, HttpMethods.GET, uri, host, stubsPort, basicAuth);

      return makeRequest(clientHttpRequest);
   }

   public ClientHttpResponse makePostRequest(final String host, final String uri, final int stubsPort, final String post) throws IOException {
      return makePostRequest(host, uri, stubsPort, null, post);
   }

   public ClientHttpResponse makePostRequest(final String host, final String uri, final int stubsPort, final String basicAuth, final String post) throws IOException {
      final ClientHttpRequest clientHttpRequest = new ClientHttpRequest(HttpSchemes.HTTP, HttpMethods.POST, uri, host, stubsPort, basicAuth, post);

      return makeRequest(clientHttpRequest);
   }

   private ClientHttpResponse makeRequest(final ClientHttpRequest clientHttpRequest) throws IOException {
      final ClientHttpTransport clientHttpTransport = new ClientHttpTransport(clientHttpRequest);
      final HttpURLConnection connection = clientHttpTransport.constructHttpConnection();

      try {
         connection.connect();
         final ClientHttpResponseFactory responseFactory = new ClientHttpResponseFactory(connection);

         return responseFactory.construct();
      } finally {
         connection.disconnect();
      }
   }
}