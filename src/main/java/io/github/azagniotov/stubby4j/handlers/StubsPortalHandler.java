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

package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.database.StubbedDataManager;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        if (response.isCommitted() || baseRequest.isHandled()) {
            ConsoleUtils.logIncomingRequestError(request, "stubs", "HTTP response was committed or base request was handled, aborting..");
            return;
        }
        baseRequest.setHandled(true);

        final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(request);
        final StubResponse foundStubResponse = stubbedDataManager.findStubResponseFor(assertionStubRequest);
        final StubResponseHandlingStrategy strategyStubResponse = StubsResponseHandlingStrategyFactory.getStrategy(foundStubResponse);

        try {
            strategyStubResponse.handle(response, assertionStubRequest);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }

        ConsoleUtils.logOutgoingResponse(assertionStubRequest.getUrl(), response);
    }
}