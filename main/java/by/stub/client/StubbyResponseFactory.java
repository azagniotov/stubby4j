package by.stub.client;

import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 12:27 PM
 */
final class StubbyResponseFactory {

   private final HttpURLConnection connection;

   StubbyResponseFactory(final HttpURLConnection connection) {
      this.connection = connection;
   }

   StubbyResponse construct() throws IOException {
      final int responseCode = connection.getResponseCode();

      if (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.CREATED_201) {
         final InputStream inputStream = connection.getInputStream();
         final String responseContent = StringUtils.inputStreamToString(inputStream);
         inputStream.close();

         return new StubbyResponse(responseCode, responseContent);
      }

      //final InputStream errorStream = connection.getErrorStream();
      return new StubbyResponse(responseCode, connection.getResponseMessage());
   }
}
