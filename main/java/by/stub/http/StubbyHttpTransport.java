package by.stub.http;

import by.stub.client.StubbyResponse;
import by.stub.exception.Stubby4JException;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 11:03 AM
 */
public class StubbyHttpTransport {

   private static final Set<String> SUPPORTED_METHODS = new HashSet<String>() {{
      add(HttpMethods.GET);
      add(HttpMethods.HEAD);
      add(HttpMethods.TRACE);
      add(HttpMethods.OPTIONS);
      add(HttpMethods.POST);
   }};

   private static final Set<String> POSTING_METHODS = new HashSet<String>() {{
      add(HttpMethods.PUT);
      add(HttpMethods.POST);
   }};

   public StubbyHttpTransport() {

   }

   public StubbyResponse getResponse(final String method, final String fullUrl) throws IOException {
      return getResponse(method, fullUrl, null, null, -1);
   }

   public StubbyResponse getResponse(final String method,
                                     final String fullUrl,
                                     final String post,
                                     final String encodedCredentials,
                                     final int postLength) throws IOException {

      if (!SUPPORTED_METHODS.contains(method)) {
         throw new Stubby4JException(String.format("HTTP method '%s' not supported when contacting stubby4j", method));
      }

      final URL url = new URL(fullUrl);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(method);
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(false);
      setRequestHeaders(connection, encodedCredentials, postLength);

      if (POSTING_METHODS.contains(method)) {
         writeOutputStream(connection, post);
      }

      return buildStubbyResponse(connection);
   }

   private StubbyResponse buildStubbyResponse(final HttpURLConnection connection) throws IOException {
      try {
         connection.connect();
         final int responseCode = connection.getResponseCode();
         if (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.CREATED_201) {
            final InputStream inputStream = connection.getInputStream();
            final String responseContent = StringUtils.inputStreamToString(inputStream);
            inputStream.close();

            return new StubbyResponse(responseCode, responseContent);
         }
         return new StubbyResponse(responseCode, connection.getResponseMessage());
      } finally {
         connection.disconnect();
      }
   }

   private void setRequestHeaders(final HttpURLConnection connection, final String encodedCredentials, final int postLength) {

      connection.setRequestProperty("User-Agent", StringUtils.constructUserAgentName());
      if (StringUtils.isSet(encodedCredentials)) {
         connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
      }

      final String requestMethod = connection.getRequestMethod();
      if (HttpMethods.POST.equals(requestMethod) || HttpMethods.PUT.equals(requestMethod)) {
         connection.setDoOutput(true);
         connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
         connection.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
         connection.setRequestProperty(HttpHeaders.CONTENT_ENCODING, StringUtils.UTF_8);

         connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Integer.toString(postLength));
         if (postLength > 0) {
            connection.setFixedLengthStreamingMode(postLength);
         } else {
            connection.setChunkedStreamingMode(0);
         }
      }
   }

   private void writeOutputStream(final HttpURLConnection connection, final String post) throws IOException {
      final OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream(), StringUtils.charsetUTF8());
      try {
         streamWriter.write(post);
         streamWriter.flush();
      } finally {
         streamWriter.close();
      }
   }
}