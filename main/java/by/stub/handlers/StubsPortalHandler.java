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

//import static org.fest.assertions.api.Assertions.assertThat;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import by.stub.database.StubbedDataManager;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import by.stub.handlers.strategy.stubs.callback.StubCallbackHandlingStrategy;
import by.stub.handlers.strategy.stubs.callback.StubsCallbackHandlingStrategyFactory;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubCallback;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;

public class StubsPortalHandler extends AbstractHandler {

   private final StubbedDataManager stubbedDataManager;

   public StubsPortalHandler(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);

      baseRequest.setHandled(true);

      final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(request);
      final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertionStubRequest);
      final StubResponseHandlingStrategy strategyStubResponse = StubsResponseHandlingStrategyFactory.getStrategy(foundStubResponse);
      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      try {
			strategyStubResponse.handle(wrapper, assertionStubRequest);
			if (foundStubResponse.isContainsCallback()) {
				StubCallback foundStubCallback = foundStubResponse.getCallback();
				StubCallbackHandlingStrategy strategyStubCallback = StubsCallbackHandlingStrategyFactory.getStrategy(foundStubCallback);
				strategyStubCallback.handle(foundStubCallback, assertionStubRequest);							
			}
		} catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(assertionStubRequest.getUrl(), wrapper);

   }
}