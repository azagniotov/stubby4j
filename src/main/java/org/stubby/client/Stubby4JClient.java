package org.stubby.client;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.server.JettyManagerFactory;
import org.stubby.server.JettyFactory;
import org.stubby.server.JettyManager;

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
public final class Stubby4JClient {

   private static final String URL_TEMPLATE = "http://%s:%s%s";
   private static final String UTF_8 = "UTF-8";

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
      jettyManager.stopJetty();
   }


   public Stubby4JResponse makeRequestWith(final ClientRequestInfo clientRequest) throws IOException {
      final HttpURLConnection con = constructClientHttpConnection(clientRequest);

      if (clientRequest.getPostBody() != null) {
         prepareConnectionForPOST(con, clientRequest.getPostBody());
         writePostBytes(con, clientRequest.getPostBody());
      }

      return parseHttpResponse(con);
   }

   private HttpURLConnection constructClientHttpConnection(final ClientRequestInfo clientRequest) throws IOException {

      final URL url = new URL(constructUrl(clientRequest));
      final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestMethod(clientRequest.getMethod());

      if (clientRequest.getBase64encodedCredentials() != null) {
         httpURLConnection.setRequestProperty("Authorization", "Basic " + clientRequest.getBase64encodedCredentials());
      }

      return httpURLConnection;
   }

   private String constructUrl(final ClientRequestInfo clientRequest) {
      final String uri = clientRequest.getUri();
      final String host = clientRequest.getHost();
      final int clientPort = clientRequest.getClientPort();

      return String.format(URL_TEMPLATE, host, clientPort, uri != null ? uri : "");
   }

   private Stubby4JResponse parseHttpResponse(final HttpURLConnection con) throws IOException {

      final int responseCode = con.getResponseCode();

      try {
         final String response = new Scanner(con.getInputStream(), UTF_8).useDelimiter("\\A").next().trim();

         return new Stubby4JResponse(responseCode, response);
      } catch (final Exception ex) {
         final String response = con.getResponseMessage().trim();

         return new Stubby4JResponse(responseCode, response);
      } finally {
         con.disconnect();
      }
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
