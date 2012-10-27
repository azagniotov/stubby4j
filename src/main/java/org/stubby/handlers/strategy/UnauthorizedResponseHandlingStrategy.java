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

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.http.HttpStatus;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;


public final class UnauthorizedResponseHandlingStrategy implements StubResponseHandlingStrategy {

   public UnauthorizedResponseHandlingStrategy() {

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
      final String expectedbase64decodedHeader = new String(Base64.decodeBase64(expectedbase64encodedHeader), StringUtils.utf8Charset());

      final String template = "Unauthorized with supplied encoded credentials: '%s' which decodes to '%s'";
      error = String.format(template, expectedbase64encodedHeader, expectedbase64decodedHeader);
      HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, error);
   }
}
