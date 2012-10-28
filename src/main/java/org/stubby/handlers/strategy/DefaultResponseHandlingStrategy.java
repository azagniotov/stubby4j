/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.stubby.handlers.strategy;

import org.stubby.exception.Stubby4JException;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DefaultResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public DefaultResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      setStubResponseHeaders(foundStubResponse, response);

      if (StringUtils.isSet(foundStubResponse.getLatency())) {
         try {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
         } catch (final InterruptedException e) {
            throw new Stubby4JException(e);
         }
      }
      response.setStatus(Integer.parseInt(foundStubResponse.getStatus()));
      response.getWriter().println(foundStubResponse.getResponseBody());
   }

   private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
      response.setCharacterEncoding(StringUtils.UTF_8);
      for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
         response.setHeader(entry.getKey(), entry.getValue());
      }
   }
}
