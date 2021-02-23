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

package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.handlers.strategy.admin.AdminResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.admin.AdminResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminPortalHandler extends AbstractHandler {

   public static final String NAME = "admin";

   //Do not remove this constant without changing the example in documentation
   public static final String ADMIN_ROOT = "/";
   private final StubRepository stubRepository;

   public AdminPortalHandler(final StubRepository stubRepository) {
      this.stubRepository = stubRepository;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);
      if (response.isCommitted() || baseRequest.isHandled()) {
         ConsoleUtils.logIncomingRequestError(request, NAME, "HTTP response was committed or base request was handled, aborting..");
         return;
      }
      baseRequest.setHandled(true);

      HandlerUtils.setResponseMainHeaders(response);
      response.setStatus(HttpStatus.OK_200);

      final AdminResponseHandlingStrategy strategyStubResponse = AdminResponseHandlingStrategyFactory.getStrategy(request);
      try {
         strategyStubResponse.handle(request, response, stubRepository);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "Problem handling request in Admin handler: " + ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
   }
}
