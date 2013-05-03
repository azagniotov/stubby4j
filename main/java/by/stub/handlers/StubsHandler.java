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

package by.stub.handlers;

import by.stub.database.DataStore;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StubsHandler extends AbstractHandler {

   private static final String NAME = "stubs";
   private final DataStore dataStore;

   public StubsHandler(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);

      final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(request);
      final StubResponse foundStubResponse = dataStore.findStubResponseFor(assertionStubRequest);
      final StubResponseHandlingStrategy strategyStubResponse = StubsResponseHandlingStrategyFactory.getStrategy(foundStubResponse);
      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      try {
         strategyStubResponse.handle(wrapper, assertionStubRequest);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(assertionStubRequest.getUrl(), wrapper, "stubs");

   }
}