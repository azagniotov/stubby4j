package org.stubby.handlers.strategy.client;

import org.eclipse.jetty.http.HttpHeaders;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 6:54 PM
 */
public class RedirectResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public RedirectResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponse response, final HttpRequestInfo httpRequestInfo) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);

      if (foundStubResponse.getLatency() != null) {
         try {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         }
      }

      response.setStatus(Integer.parseInt(foundStubResponse.getStatus()));
      response.setHeader(HttpHeaders.LOCATION, foundStubResponse.getHeaders().get("location"));
      response.setHeader(HttpHeaders.CONNECTION, "close");
   }
}
