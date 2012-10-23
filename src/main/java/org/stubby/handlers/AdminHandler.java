/*
A Java-based HTTP stub server

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

package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.handlers.strategy.admin.AdminRequestHandlingStrategy;
import org.stubby.handlers.strategy.admin.AdminRequestHandlingStrategyFactory;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public final class AdminHandler extends AbstractHandler {

   private static final String NAME = "admin";

   //Do not remove this constant without changing the example in documentation
   public static final String RESOURCE_STUBDATA_NEW = "/stubdata/new";

   private final JettyOrchestrator jettyOrchestrator;

   public AdminHandler(final JettyOrchestrator jettyOrchestrator) {
      this.jettyOrchestrator = jettyOrchestrator;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {
      HandlerUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      final AdminRequestHandlingStrategy adminRequestHandlingStrategy =
            AdminRequestHandlingStrategyFactory.identifyHandlingStrategyFor(request.getPathInfo(), jettyOrchestrator);

      try {
         adminRequestHandlingStrategy.handle(request, response);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      HandlerUtils.logOutgoingResponse(request, response, NAME);
   }
}