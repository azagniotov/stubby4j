package org.stubby.handlers.strategy;

import org.eclipse.jetty.http.HttpStatus;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 10:47 AM
 */
public final class NotFoundResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public NotFoundResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponse response, final HttpRequestInfo httpRequestInfo) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      final String postMessage = (httpRequestInfo.getPostBody() != null ? " for post data: " + httpRequestInfo.getPostBody() : "");
      final String error = "No data found for " + httpRequestInfo.getMethod() + " request at URI " + httpRequestInfo.getUrl() + postMessage;
      HandlerUtils.configureErrorResponse(response, HttpStatus.NOT_FOUND_404, error);
   }
}
