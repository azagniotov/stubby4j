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

package org.stubby.client;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.server.JettyFactory;
import org.stubby.server.JettyManager;
import org.stubby.server.JettyManagerFactory;
import org.stubby.utils.StringUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class Stubby4JClient {

   private static final String URL_TEMPLATE = "http://%s:%s%s";

   private JettyManager jettyManager;
   private String yamlConfigurationFilename;

   private Stubby4JClient() {

   }

   public Stubby4JClient(final String yamlConfigurationFilename) {
      this.yamlConfigurationFilename = yamlConfigurationFilename;
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
      if (jettyManager != null)
         jettyManager.stopJetty();
   }


   public Stubby4JResponse makeRequestWith(final ClientRequestInfo clientRequest) throws IOException {
      final HttpURLConnection con = constructClientHttpConnection(clientRequest);

      if (StringUtils.isSet(clientRequest.getPostBody())) {
         prepareConnectionForPOST(con, clientRequest.getPostBody());
         writePostBytes(con, clientRequest.getPostBody());
      }

      return parseHttpResponse(con);
   }

   private HttpURLConnection constructClientHttpConnection(final ClientRequestInfo clientRequest) throws IOException {

      final URL url = new URL(constructUrl(clientRequest));
      final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestMethod(clientRequest.getMethod());

      if (StringUtils.isSet(clientRequest.getBase64encodedCredentials())) {
         httpURLConnection.setRequestProperty("Authorization", "Basic " + clientRequest.getBase64encodedCredentials());
      }

      return httpURLConnection;
   }

   private String constructUrl(final ClientRequestInfo clientRequest) {
      final String uri = clientRequest.getUri();
      final String host = clientRequest.getHost();
      final int clientPort = clientRequest.getClientPort();

      return String.format(URL_TEMPLATE, host, clientPort, (StringUtils.isSet(uri) ? uri : ""));
   }

   private Stubby4JResponse parseHttpResponse(final HttpURLConnection con) {
      try {
         final String response = getResponseAsString(con);

         return new Stubby4JResponse(con.getResponseCode(), response);
      } catch (final Exception ex) {
         return new Stubby4JResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      } finally {
         con.disconnect();
      }
   }

   private String getResponseAsString(final HttpURLConnection con) {
      try {
         return new Scanner(con.getInputStream(), StringUtils.UTF_8).useDelimiter("\\A").next().trim();
      } catch (final Exception ex) {
         try {
            return con.getResponseMessage().trim();
         } catch (final IOException ex2) {
            return String.format("%s->%s", ex.toString(), ex2.toString());
         }
      }
   }

   private void prepareConnectionForPOST(final HttpURLConnection con, final String postData) throws ProtocolException {
      con.setRequestMethod(HttpMethods.POST);
      con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
      con.setRequestProperty(HttpHeaders.CONTENT_LENGTH, calculatePostDataLength(postData));
      con.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
      con.setRequestProperty(HttpHeaders.CONTENT_ENCODING, StringUtils.UTF_8);
      con.setUseCaches(false);
      con.setDoInput(true);
      con.setDoOutput(true);
   }

   private String calculatePostDataLength(final String postData) {
      return (postData != null ? Integer.toString(postData.getBytes(StringUtils.utf8Charset()).length) : "0");
   }

   private void writePostBytes(final HttpURLConnection con, final String postData) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(con.getOutputStream());
      try {
         dataOutputStream.writeBytes((StringUtils.isSet(postData) ? postData : ""));
         dataOutputStream.flush();
      } catch (final IOException ex) {
         throw new IOException(ex);
      } finally {
         dataOutputStream.close();
      }
   }
}
