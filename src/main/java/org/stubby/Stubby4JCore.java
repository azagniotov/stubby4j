package org.stubby;

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

/**
 * @author Alexander Zagniotov
 * @since 7/4/12, 10:46 PM
 */
final class Stubby4JCore {

   private static final String URL_TEMPLATE = "http://%s:%s%s";
   private static final String UTF_8 = "UTF-8";

   private JettyOrchestrator jettyOrchestrator;
   private final String yamlConfigurationFilename;

   Stubby4JCore(final String yamlConfigurationFilename) {
      this.yamlConfigurationFilename = yamlConfigurationFilename;
   }

   void start() throws Exception {
      start(JettyOrchestrator.DEFAULT_CLIENT_PORT, JettyOrchestrator.DEFAULT_ADMIN_PORT);
   }

   void start(final int clientPort, final int adminPort) throws Exception {

      if (yamlConfigurationFilename == null) {
         throw new Stubby4JException("YAML configuration file is required before starting stubby4j");
      }

      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineIntepreter.OPTION_CLIENTPORT, String.format("%s", clientPort));
      params.put(CommandLineIntepreter.OPTION_ADMINPORT, String.format("%s", adminPort));

      jettyOrchestrator = JettyOrchestratorFactory.getInstance(yamlConfigurationFilename, params);
      jettyOrchestrator.startJetty();
   }

   void stop() throws Exception {
      jettyOrchestrator.stopJetty();
   }

   Map<String, String> doGetOnURI(final String uri) throws IOException {
      if (yamlConfigurationFilename == null) {
         throw new Stubby4JException("YAML configuration file is required before starting stubby4j");
      }
      return parseHttpResponse(constructHttpConnection(uri));
   }

   Map<String, String> doPostOnURI(final String uri, final String postData) throws IOException {
      if (yamlConfigurationFilename == null) {
         throw new Stubby4JException("YAML configuration file is required before starting stubby4j");
      }
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
      results.put(Stubby4J.KEY_STATUS, responseCode.toString());

      try {
         final String response = new Scanner(con.getInputStream(), UTF_8).useDelimiter("\\A").next();
         results.put(Stubby4J.KEY_RESPONSE, response.trim());
      } catch (Exception ex) {
         results.put(Stubby4J.KEY_RESPONSE, con.getResponseMessage().trim());
         //throw new Stubby4JException("aaaaaaaa");
      } finally {
         con.disconnect();
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
}
