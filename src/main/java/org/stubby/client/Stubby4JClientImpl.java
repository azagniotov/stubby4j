package org.stubby.client;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.handlers.AdminHandler;
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
final class Stubby4JClientImpl implements Stubby4JClient {

   private static final String URL_TEMPLATE = "http://%s:%s%s";
   private static final String UTF_8 = "UTF-8";

   private JettyOrchestrator jettyOrchestrator;
   private String yamlConfigurationFilename;

   public Stubby4JClientImpl() {

   }

   public Stubby4JClientImpl(final String yamlConfigurationFilename) {
      this.yamlConfigurationFilename = yamlConfigurationFilename;
   }

   @Override
   public Stubby4JResponse registerStubData(final String yamlConfigurationContent, final String host, final int adminPort) throws Exception {
      final HttpURLConnection con = constructClientHttpConnection(AdminHandler.RESOURCE_STUBDATA_NEW, host, adminPort);
      prepareConnectionForPOST(con, yamlConfigurationContent);
      writePostBytes(con, yamlConfigurationContent);

      return parseHttpResponse(con);
   }

   @Override
   public void start() throws Exception {
      start(JettyOrchestrator.DEFAULT_CLIENT_PORT, JettyOrchestrator.DEFAULT_ADMIN_PORT);
   }

   @Override
   public void start(final int clientPort, final int adminPort) throws Exception {

      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineIntepreter.OPTION_CLIENTPORT, String.format("%s", clientPort));
      params.put(CommandLineIntepreter.OPTION_ADMINPORT, String.format("%s", adminPort));

      jettyOrchestrator = JettyOrchestratorFactory.getInstance(yamlConfigurationFilename, params);
      jettyOrchestrator.startJetty();
   }

   @Override
   public void stop() throws Exception {
      jettyOrchestrator.stopJetty();
      JettyOrchestratorFactory.cleanUp();
   }

   @Override
   public Stubby4JResponse doGetOnURI(final String uri, final String host, final int clientPort) throws IOException {
      return parseHttpResponse(constructClientHttpConnection(uri, host, clientPort));
   }

   @Override
   public Stubby4JResponse doPostOnURI(final String uri, final String postData, final String host, final int clientPort) throws IOException {
      final HttpURLConnection con = constructClientHttpConnection(uri, host, clientPort);
      prepareConnectionForPOST(con, postData);
      writePostBytes(con, postData);

      return parseHttpResponse(con);
   }

   private HttpURLConnection constructClientHttpConnection(final String uri, final String host, final int port) throws IOException {
      final String urlString = String.format(URL_TEMPLATE, host, port, uri != null ? uri : "");
      final URL url = new URL(urlString);
      return (HttpURLConnection) url.openConnection();
   }

   private Stubby4JResponse parseHttpResponse(final HttpURLConnection con) throws IOException {

      String response;
      final int responseCode = con.getResponseCode();

      try {
         response = new Scanner(con.getInputStream(), UTF_8).useDelimiter("\\A").next().trim();
      } catch (Exception ex) {
         response = con.getResponseMessage().trim();
      } finally {
         con.disconnect();
      }
      return new Stubby4JResponse(responseCode, response);
   }

   private void prepareConnectionForPOST(final HttpURLConnection con, final String postData) throws ProtocolException {
      con.setRequestMethod(HttpMethods.POST);
      con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
      con.setRequestProperty(HttpHeaders.CONTENT_LENGTH, calculatePostDataLength(postData));
      con.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
      con.setRequestProperty(HttpHeaders.CONTENT_ENCODING, UTF_8);
      con.setUseCaches(false);
      con.setDoInput(true);
      con.setDoOutput(true);
   }

   private String calculatePostDataLength(final String postData) {
      return (postData != null ? Integer.toString(postData.getBytes().length) : "0");
   }

   private void writePostBytes(final HttpURLConnection con, final String postData) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(con.getOutputStream());
      dataOutputStream.writeBytes((postData != null ? postData : ""));
      dataOutputStream.flush();
      dataOutputStream.close();
   }
}
