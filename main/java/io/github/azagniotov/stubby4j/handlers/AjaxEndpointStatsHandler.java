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

import static io.github.azagniotov.stubby4j.utils.HandlerUtils.getHtmlResourceByName;

public class AjaxEndpointStatsHandler extends AbstractHandler {

   private final StubRepository stubRepository;

   public AjaxEndpointStatsHandler(final StubRepository stubRepository) {
      this.stubRepository = stubRepository;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      if (response.isCommitted() || baseRequest.isHandled()) {
         ConsoleUtils.logIncomingRequestError(request, "ajaxEndpoint", "HTTP response was committed or base request was handled, aborting..");
         return;
      }
      baseRequest.setHandled(true);

      HandlerUtils.setResponseMainHeaders(response);
      response.setContentType("text/plain;charset=UTF-8");
      response.setStatus(HttpStatus.OK_200);

      try {
         if (request.getRequestURI().contains("stats/check")) {
            response.getWriter().println(!stubRepository.getResourceStats().isEmpty());
         } else {
            ConsoleUtils.logIncomingRequest(request);
            final String popupStatsHtmlTemplate = getHtmlResourceByName("_popup_stats");
            final String htmlPopup = String.format(popupStatsHtmlTemplate, stubRepository.getResourceStatsAsCsv());
            response.getWriter().println(htmlPopup);
            ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
         }
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }
   }
}
