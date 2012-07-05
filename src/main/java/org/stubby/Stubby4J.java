/*
A Java-based HTTP stub server

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

package org.stubby;

import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.exception.Stubby4JException;
import org.stubby.server.JettyOrchestrator;
import org.stubby.server.JettyOrchestratorFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class Stubby4J {

   private static final String URL_TEMPLATE = "http://%s:%s%s";
   private static final String UTF_8 = "UTF-8";
   public static final String KEY_STATUS = "status";
   public static final String KEY_RESPONSE = "response";

   private JettyOrchestrator jettyOrchestrator;
   private final String yamlConfigurationFilename;

   public Stubby4J(final String yamlConfigurationFilename) {
      this.yamlConfigurationFilename = yamlConfigurationFilename;
   }

   public void start() throws Exception {
      start(JettyOrchestrator.DEFAULT_CLIENT_PORT, JettyOrchestrator.DEFAULT_ADMIN_PORT);
   }

   public void start(final int clientPort, final int adminPort) throws Exception {

      if (yamlConfigurationFilename == null) {
         throw new Stubby4JException("YAML configuration file is required before starting stubby4j");
      }

      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineIntepreter.OPTION_CLIENTPORT, String.format("%s", clientPort));
      params.put(CommandLineIntepreter.OPTION_ADMINPORT, String.format("%s", adminPort));

      jettyOrchestrator = JettyOrchestratorFactory.getInstance(yamlConfigurationFilename, params);
      jettyOrchestrator.startJetty();
   }

   public void stop() throws Exception {
      jettyOrchestrator.stopJetty();
   }

   public Map<String, String> doGetOnURI(final String uri) throws IOException {
      return parseHttpResponse(constructHttpConnection(uri));
   }

   public Map<String, String> doPostOnURI(final String uri, final String postData) throws IOException {
      final HttpURLConnection con = constructHttpConnection(uri);
      prepareConnectionForPOST(con, postData);
      writePostBytes(con, postData);

      return parseHttpResponse(con);
   }

   private HttpURLConnection constructHttpConnection(final String uri) throws IOException {
      final String urlString = String.format(URL_TEMPLATE,
            jettyOrchestrator.getCurrentHost(),
            jettyOrchestrator.getCurrentClientPort(), uri != null ? uri : "");
      final URL url = new URL(urlString);
      return (HttpURLConnection) url.openConnection();
   }

   private static Map<String, String> parseHttpResponse(final HttpURLConnection con) throws IOException {
      final Map<String, String> results = new HashMap<String, String>();

      final Integer responseCode = con.getResponseCode();
      results.put(KEY_STATUS, responseCode.toString());

      try {
         final String response = new Scanner(con.getInputStream(), UTF_8).useDelimiter("\\A").next();
         results.put(KEY_RESPONSE, response.trim());
      } catch (Exception ex) {
         results.put(KEY_RESPONSE, con.getResponseMessage().trim());
         //throw new Stubby4JException("aaaaaaaa");
      } finally {
         if (con != null) {
            con.disconnect();
         }
      }
      return results;
   }

   private static void prepareConnectionForPOST(final HttpURLConnection con, final String postData) throws ProtocolException {
      con.setRequestMethod(HttpMethods.POST);
      con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
      con.setRequestProperty(HttpHeaders.CONTENT_LENGTH, calculatePostDataLength(postData));
      con.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
      con.setRequestProperty(HttpHeaders.CONTENT_ENCODING, UTF_8);
      con.setUseCaches(false);
      con.setDoInput(true);
      con.setDoOutput(true);
   }

   private static String calculatePostDataLength(final String postData) {
      return (postData != null ? Integer.toString(postData.getBytes().length) : "0");
   }

   private static void writePostBytes(final HttpURLConnection con, final String postData) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(con.getOutputStream());
      dataOutputStream.writeBytes((postData != null ? postData : ""));
      dataOutputStream.flush();
      dataOutputStream.close();
   }


   public static void main(final String[] args) {

      try {
         CommandLineIntepreter.parseCommandLine(args);
      } catch (ParseException e) {
         e.printStackTrace();
         System.exit(1);
      }
      if (CommandLineIntepreter.isHelp()) {
         CommandLineIntepreter.printHelp(Stubby4J.class);

      } else if (!CommandLineIntepreter.isYamlProvided()) {
         System.err.println("\n\nYAML configuration was not provided using command line option '-f' or '--config'.\nPlease run again with option '--help'\n\n");
         System.exit(1);

      } else {

         try {
            final Map<String, String> commandLineArgs = CommandLineIntepreter.getCommandlineParams();
            final String yamlConfigFilename = commandLineArgs.get(CommandLineIntepreter.OPTION_CONFIG);

            JettyOrchestratorFactory.getInstance(yamlConfigFilename, commandLineArgs).startJetty();

         } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }
   }
}