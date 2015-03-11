package by.stub.http;

import by.stub.cli.ANSITerminal;
import by.stub.client.StubbyResponse;
import by.stub.exception.Stubby4JException;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 11:03 AM
 */
public class StubbyHttpTransport {

   private static final Set<String> SUPPORTED_METHODS = new HashSet<String>() {{
      add(HttpMethod.GET.asString());
      add(HttpMethod.HEAD.asString());
      add(HttpMethod.TRACE.asString());
      add(HttpMethod.OPTIONS.asString());
      add(HttpMethod.POST.asString());
   }};

   private static final Set<String> POSTING_METHODS = new HashSet<String>() {{
      add(HttpMethod.PUT.asString());
      add(HttpMethod.POST.asString());
   }};

   public StubbyHttpTransport() {

   }

   public StubbyResponse fetchRecordableHTTPResponse(final StubRequest request, final String destinationToRecordUrl) throws IOException {
      final String method = request.getMethod().get(0);
      final String fullUrl = String.format("%s%s", destinationToRecordUrl, request.getUrl());
      if (!ANSITerminal.isMute()) {
         final String logMessage = String.format("[%s] -> Recording HTTP response using %s [%s]", ConsoleUtils.getTime(), method, fullUrl);
         ANSITerminal.incoming(logMessage);
      }
      return getResponse(method,
         fullUrl,
         request.getPostBody(),
         request.getHeaders(),
         StringUtils.calculateStringLength(request.getPostBody()));
   }

   public StubbyResponse getResponse(final String method,
                                     final String fullUrl,
                                     final String post,
                                     final Map<String, String> headers,
                                     final int postLength) throws IOException {

      if (!SUPPORTED_METHODS.contains(method)) {
         throw new Stubby4JException(String.format("HTTP method '%s' not supported when contacting stubby4j", method));
      }

      final URL url = new URL(fullUrl);
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(method);
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(false);
      setRequestHeaders(connection, headers, postLength);

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

   private void setRequestHeaders(final HttpURLConnection connection, final Map<String, String> headers, final int postLength) {

      final String encodedCredentials = headers.remove(StubRequest.AUTH_HEADER);
      connection.setRequestProperty("User-Agent", StringUtils.constructUserAgentName());
      if (StringUtils.isSet(encodedCredentials) && !encodedCredentials.startsWith(StringUtils.toLower("Basic"))) {
         connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
      } else if (StringUtils.isSet(encodedCredentials) && encodedCredentials.startsWith(StringUtils.toLower("Basic"))) {
         connection.setRequestProperty("Authorization", encodedCredentials);
      }

      final String requestMethod = connection.getRequestMethod();
      if (HttpMethod.POST.asString().equals(requestMethod) || HttpMethod.PUT.asString().equals(requestMethod)) {
         connection.setDoOutput(true);
         connection.setRequestProperty(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
         connection.setRequestProperty(HttpHeader.CONTENT_LANGUAGE.asString(), "en-US");
         connection.setRequestProperty(HttpHeader.CONTENT_ENCODING.asString(), StringUtils.UTF_8);

         connection.setRequestProperty(HttpHeader.CONTENT_LENGTH.asString(), Integer.toString(postLength));
         if (postLength > 0) {
            connection.setFixedLengthStreamingMode(postLength);
         } else {
            connection.setChunkedStreamingMode(0);
         }
      }

      for (Entry<String, String> entry : headers.entrySet()) {
         connection.setRequestProperty(entry.getKey(), entry.getValue());
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
