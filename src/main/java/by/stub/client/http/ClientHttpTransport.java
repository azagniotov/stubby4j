package by.stub.client.http;

import by.stub.exception.Stubby4JException;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.MimeTypes;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 11:03 AM
 */
final class ClientHttpTransport {

   private static final String URL_TEMPLATE = "%s://%s:%s%s";

   private static final String[] SUPPORTED_METHODS = {
         HttpMethods.GET,
         HttpMethods.HEAD,
         HttpMethods.TRACE,
         HttpMethods.OPTIONS,
         HttpMethods.POST
   };

   static {
      Arrays.sort(SUPPORTED_METHODS);
   }

   private final ClientHttpRequest clientHttpRequest;

   ClientHttpTransport(final ClientHttpRequest clientHttpRequest) {
      this.clientHttpRequest = clientHttpRequest;
   }

   private boolean isMethodSupported(final String method) {
      return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
   }

   HttpURLConnection constructHttpConnection() throws IOException {

      if (!isMethodSupported(clientHttpRequest.getMethod())) {
         throw new Stubby4JException(String.format("HTTP method '%s' not supported when contacting stubby4j", clientHttpRequest.getMethod()));
      }

      final URL url = new URL(constructUrlFromClientRequest());
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      if (HttpSchemes.HTTPS.equals(clientHttpRequest.getScheme())) {
         final HttpsURLConnection sslConnection = (HttpsURLConnection) connection;
         sslConnection.setHostnameVerifier(new DefaultHostnameVerifier());
      }

      connection.setRequestMethod(clientHttpRequest.getMethod());
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(false);
      setRequestHeaders(connection);

      final String requestMethod = connection.getRequestMethod();
      if (HttpMethods.POST.equals(requestMethod) || HttpMethods.PUT.equals(requestMethod)) {
         writeOutputStream(connection);
      }

      return connection;
   }

   private void setRequestHeaders(final HttpURLConnection connection) {

      connection.setRequestProperty("User-Agent", constructUserAgentName());
      if (StringUtils.isSet(clientHttpRequest.getBase64encodedCredentials())) {
         connection.setRequestProperty("Authorization", "Basic " + clientHttpRequest.getBase64encodedCredentials());
      }

      final String requestMethod = connection.getRequestMethod();
      if (HttpMethods.POST.equals(requestMethod) || HttpMethods.PUT.equals(requestMethod)) {
         connection.setDoOutput(true);
         connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
         connection.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
         connection.setRequestProperty(HttpHeaders.CONTENT_ENCODING, StringUtils.UTF_8);

         final long contentLength = calculatePostLength();
         connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Long.toString(contentLength));
         if (contentLength >= 0 && contentLength <= Integer.MAX_VALUE) {
            connection.setFixedLengthStreamingMode((int) contentLength);
         } else {
            connection.setChunkedStreamingMode(0);
         }
      }
   }

   private void writeOutputStream(final HttpURLConnection connection) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
      try {
         dataOutputStream.writeBytes(clientHttpRequest.getPost());
         dataOutputStream.flush();
      } finally {
         dataOutputStream.close();
      }
   }

   private String constructUrlFromClientRequest() {
      final String scheme = clientHttpRequest.getScheme();
      final String uri = clientHttpRequest.getUri();
      final String host = clientHttpRequest.getHost();
      final int clientPort = clientHttpRequest.getClientPort();

      return String.format(URL_TEMPLATE, scheme, host, clientPort, uri);
   }

   private long calculatePostLength() {
      if (StringUtils.isSet(clientHttpRequest.getPost())) {
         final byte[] postDataBytes = clientHttpRequest.getPost().getBytes(StringUtils.utf8Charset());
         return (long) postDataBytes.length;
      }
      return (long) 0;
   }

   private String constructUserAgentName() {
      final Package pkg = this.getClass().getPackage();
      final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
            pkg.getImplementationVersion() : "x.x.x";

      final String implementationTitle = StringUtils.isSet(pkg.getImplementationTitle()) ?
            pkg.getImplementationTitle() : "HTTP stub client request";
      return String.format("stubby4j/%s (%s)", implementationVersion, implementationTitle);
   }

   private static final class DefaultHostnameVerifier implements HostnameVerifier {

      DefaultHostnameVerifier() {

      }

      @Override
      public boolean verify(final String s, final SSLSession sslSession) {
         return true;
      }
   }
}