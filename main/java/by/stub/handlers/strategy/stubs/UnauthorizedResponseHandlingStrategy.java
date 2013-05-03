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
import by.stub.repackaged.org.apache.commons.codec.binary.Base64;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;


public final class UnauthorizedResponseHandlingStrategy implements StubResponseHandlingStrategy {

   public UnauthorizedResponseHandlingStrategy() {

   }

   @Override
   public void handle(final HttpServletResponseWithGetStatus response, final StubRequest assertionStubRequest) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      final String authorizationHeader = assertionStubRequest.getHeaders().get(StubRequest.AUTH_HEADER);
      String error;
      if (authorizationHeader == null) {
         error = "You are not authorized to view this page without supplied 'Authorization' HTTP header";
         HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, error);
         return;
      }
      final String expectedbase64encodedHeader = authorizationHeader.substring("Basic ".length());
      final byte[] decodedHeaderBytes = Base64.decodeBase64(expectedbase64encodedHeader);
      final String expectedbase64decodedHeader = StringUtils.newStringUtf8(decodedHeaderBytes);

      final String template = "Unauthorized with supplied encoded credentials: '%s' which decodes to '%s'";
      error = String.format(template, expectedbase64encodedHeader, expectedbase64decodedHeader);
      HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, error);
   }
}
