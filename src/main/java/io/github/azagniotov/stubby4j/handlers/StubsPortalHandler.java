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

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubSearchResult;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory.getStrategy;

public class StubsPortalHandler extends AbstractHandler {

    private final StubRepository stubRepository;

    public StubsPortalHandler(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
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

        try {
            final StubSearchResult stubSearchResult = stubRepository.search(request);
            final StubResponseHandlingStrategy strategyStubResponse = getStrategy(stubSearchResult.getMatch());

            strategyStubResponse.handle(response, stubSearchResult.getInvariant());
            ConsoleUtils.logOutgoingResponse(stubSearchResult.getInvariant().getUrl(), response);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }
}
