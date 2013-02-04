package by.stub.client;

import by.stub.exception.Stubby4JException;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 11:03 AM
 */
final class StubbyHttpTransport {

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

   private final StubbyRequest stubbyRequest;

   StubbyHttpTransport(final StubbyRequest stubbyRequest) {
      this.stubbyRequest = stubbyRequest;
   }

   HttpURLConnection constructHttpConnection() throws IOException {

      if (!SUPPORTED_METHODS.contains(stubbyRequest.getMethod())) {
         throw new Stubby4JException(String.format("HTTP method '%s' not supported when contacting stubby4j", stubbyRequest.getMethod()));
      }

      final URL url = new URL(stubbyRequest.constructFullUrl());
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(stubbyRequest.getMethod());
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(false);
      setRequestHeaders(connection);

      final String requestMethod = connection.getRequestMethod();
      if (POSTING_METHODS.contains(requestMethod)) {
         writeOutputStream(connection);
      }

      return connection;
   }

   private void setRequestHeaders(final HttpURLConnection connection) {

      connection.setRequestProperty("User-Agent", StringUtils.constructUserAgentName());
      if (StringUtils.isSet(stubbyRequest.getBase64encodedCredentials())) {
         connection.setRequestProperty("Authorization", "Basic " + stubbyRequest.getBase64encodedCredentials());
      }

      final String requestMethod = connection.getRequestMethod();
      if (HttpMethods.POST.equals(requestMethod) || HttpMethods.PUT.equals(requestMethod)) {
         connection.setDoOutput(true);
         connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MimeTypes.FORM_ENCODED);
         connection.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, "en-US");
         connection.setRequestProperty(HttpHeaders.CONTENT_ENCODING, StringUtils.UTF_8);

         final int contentLength = stubbyRequest.calculatePostLength();
         connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Integer.toString(contentLength));
         if (contentLength > 0) {
            connection.setFixedLengthStreamingMode(contentLength);
         } else {
            connection.setChunkedStreamingMode(0);
         }
      }
   }

   private void writeOutputStream(final HttpURLConnection connection) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
      try {
         dataOutputStream.writeBytes(stubbyRequest.getPost());
         dataOutputStream.flush();
      } finally {
         dataOutputStream.close();
      }
   }
}