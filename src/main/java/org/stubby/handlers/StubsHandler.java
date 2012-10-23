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

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.handlers.strategy.client.HandlingStrategyFactory;
import org.stubby.handlers.strategy.client.StubResponseHandlingStrategy;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public class StubsHandler extends AbstractHandler {
   private static final String NAME = "stubs";

   public static final String BAD_POST_REQUEST_MESSAGE = "Oh oh :( Bad request, POST body is missing";
   private final DataStore dataStore;

   public StubsHandler(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {
      HandlerUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);

      final String postBody = HandlerUtils.extractPostRequestBody(request, NAME);
      final HttpRequestInfo httpRequestInfo = new HttpRequestInfo(request, postBody);
      final StubResponse foundStubResponse = dataStore.findStubResponseFor(httpRequestInfo);
      final StubResponseHandlingStrategy strategyStubResponse = HandlingStrategyFactory.identifyHandlingStrategyFor(foundStubResponse);

      try {
         strategyStubResponse.handle(response, httpRequestInfo);
      } catch (Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      if (System.getProperty(AdminHandler.STUBBY4J_NO_LOG_PROPERTY) == null) {
         HandlerUtils.logOutgoingResponse(request, response, NAME);
      }
   }
}