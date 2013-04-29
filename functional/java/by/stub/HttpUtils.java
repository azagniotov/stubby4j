package by.stub;

import by.stub.utils.StringUtils;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;

/**
 * @author: Alexander Zagniotov
 * Created: 4/28/13 8:53 PM
 */
public final class HttpUtils {

   private static final HttpRequestFactory WEB_CLIENT;

   static {
      WEB_CLIENT = new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
         @Override
         public void initialize(final HttpRequest request) {
            request.setThrowExceptionOnExecuteError(false);
            request.setReadTimeout(45000);
            request.setConnectTimeout(45000);
         }
      });
   }

   private HttpUtils() {

   }

   public static HttpRequest constructHttpRequest(final String method, final String targetUrl) throws IOException {

      return WEB_CLIENT.buildRequest(method,
         new GenericUrl(targetUrl),
         null);
   }

   public static HttpRequest constructHttpRequest(final String method, final String targetUrl, final String content) throws IOException {

      return WEB_CLIENT.buildRequest(method,
         new GenericUrl(targetUrl),
         new ByteArrayContent(null, StringUtils.getBytesUtf8(content)));
   }
}
