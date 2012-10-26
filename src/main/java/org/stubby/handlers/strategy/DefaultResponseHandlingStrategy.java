package org.stubby.handlers.strategy;

import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 10:48 AM
 */
public final class DefaultResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public DefaultResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponse response, final HttpRequestInfo httpRequestInfo) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      setStubResponseHeaders(foundStubResponse, response);

      if (foundStubResponse.getLatency() != null) {
         try {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         }
      }
      response.setStatus(Integer.parseInt(foundStubResponse.getStatus()));
      response.getWriter().println(foundStubResponse.getResponseBody());
   }

   private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
      response.setCharacterEncoding("UTF-8");
      for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
         response.setHeader(entry.getKey(), entry.getValue());
      }
   }
}
