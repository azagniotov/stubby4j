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

package by.stub.handlers.strategy.stubs;

import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

public final class NotFoundResponseHandlingStrategy implements StubResponseHandlingStrategy {

   public NotFoundResponseHandlingStrategy() {

   }

   @Override
   public void handle(final HttpServletResponseWithGetStatus response, final StubRequest assertion) throws IOException {

      HandlerUtils.setResponseMainHeaders(response);

      final String postMessage = assertion.hasPostBody() ? String.format("\n\t%s%s", "With post data: ", assertion.getPostBody()) : "";
      final String headersMessage = assertion.hasHeaders() ? String.format("\n\t%s%s", "With headers: ", assertion.getHeaders()) : "";
      final String queryMessage = (assertion.hasQuery() ? String.format("\n\t%s%s", "With query params: ", assertion.getQuery()) : "");

      final String error = String.format("No data found for %s request at URI %s%s%s%s", assertion.getMethod().get(0), assertion.getUrl(), postMessage, headersMessage, queryMessage);

      HandlerUtils.configureErrorResponse(response, HttpStatus.NOT_FOUND_404, error);
   }
}
