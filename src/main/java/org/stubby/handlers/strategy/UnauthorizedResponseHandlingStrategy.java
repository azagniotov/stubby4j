package org.stubby.handlers.strategy;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpStatus;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 10:47 AM
 */
public final class UnauthorizedResponseHandlingStrategy implements StubResponseHandlingStrategy {

   public UnauthorizedResponseHandlingStrategy(final StubResponse foundStubResponse) {

   }

   @Override
   public void handle(final HttpServletResponse response, final HttpRequestInfo httpRequestInfo) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      final String authorizationHeader = httpRequestInfo.getHeaders().get(HttpRequestInfo.AUTH_HEADER);
      String error = "";
      if (authorizationHeader == null) {
         error = "You are not authorized to view this page without supplied 'Authorization' HTTP header";
         HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, error);
         return;
      }
      final String expectedbase64encodedHeader = authorizationHeader.substring("Basic ".length());
      final String expectedbase64decodedHeader = new String(Base64.decodeBase64(expectedbase64encodedHeader), Charset.forName("UTF-8"));

      final String template = "Unauthorized with supplied encoded credentials: '%s' which decodes to '%s'";
      error = String.format(template, expectedbase64encodedHeader, expectedbase64decodedHeader);
      HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, error);
   }
}
