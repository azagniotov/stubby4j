package by.stub.client.http;

import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 12:27 PM
 */
final class ClientHttpResponseFactory {

   private final HttpURLConnection connection;

   ClientHttpResponseFactory(final HttpURLConnection connection) {
      this.connection = connection;
   }

   ClientHttpResponse construct() throws IOException {
      final int responseCode = connection.getResponseCode();

      if (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.CREATED_201) {
         final InputStream inputStream = connection.getInputStream();
         return new ClientHttpResponse(responseCode, StringUtils.inputStreamToString(inputStream));
      }

      //final InputStream errorStream = connection.getErrorStream();
      return new ClientHttpResponse(responseCode, connection.getResponseMessage());
   }
}
