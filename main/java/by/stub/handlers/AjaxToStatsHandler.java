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

package by.stub.handlers;

import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxToStatsHandler extends AbstractHandler {

   private static final String POPUP_STATS_HTML_TEMPLATE = HandlerUtils.getHtmlResourceByName("popup-stats");
   private final StubbedDataManager stubbedDataManager;

   public AjaxToStatsHandler(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);

      baseRequest.setHandled(true);

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);
      HandlerUtils.setResponseMainHeaders(wrapper);
      wrapper.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
      wrapper.setStatus(HttpStatus.OK_200);

      try {
         final String htmlPopup = String.format(POPUP_STATS_HTML_TEMPLATE, stubbedDataManager.getResourceStatsAsCsv());
         wrapper.getWriter().println(htmlPopup);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper);
   }
}
