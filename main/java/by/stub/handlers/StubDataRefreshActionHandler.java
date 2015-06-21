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

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.server.JettyContext;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@SuppressWarnings("serial")
public final class StubDataRefreshActionHandler extends AbstractHandler {

   private final StubbedDataManager stubbedDataManager;
   private final JettyContext jettyContext;

   public StubDataRefreshActionHandler(final JettyContext newContext, final StubbedDataManager newStubbedDataManager) {
      this.jettyContext = newContext;
      this.stubbedDataManager = newStubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);
      if (response.isCommitted() || baseRequest.isHandled()) {
         ConsoleUtils.logIncomingRequestError(request, "stubData", "HTTP response was committed or base request was handled, aborting..");
         return;
      }
      baseRequest.setHandled(true);
      response.setContentType("text/plain;charset=UTF-8");
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());

      try {
         stubbedDataManager.refreshStubbedData(new YamlParser());
         final String successMessage = String.format("Successfully performed live refresh of main YAML from: %s on [" + new Date().toString().trim() + "]",
            stubbedDataManager.getDataYaml());
         response.getWriter().println(successMessage);
         ANSITerminal.ok(successMessage);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
   }
}