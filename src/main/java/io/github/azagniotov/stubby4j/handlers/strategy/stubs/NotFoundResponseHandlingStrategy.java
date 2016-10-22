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

package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

public final class NotFoundResponseHandlingStrategy implements StubResponseHandlingStrategy {

    public NotFoundResponseHandlingStrategy() {

    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {

        HandlerUtils.setResponseMainHeaders(response);

        final String postMessage = assertionStubRequest.hasPostBody() ? String.format(BR + "\t%s%s", "With post data: ", assertionStubRequest.getPostBody()) : "";
        final String headersMessage = assertionStubRequest.hasHeaders() ? String.format(BR + "\t%s%s", "With headers: ", assertionStubRequest.getHeaders()) : "";
        final String queryMessage = (assertionStubRequest.hasQuery() ? String.format(BR + "\t%s%s", "With query params: ", assertionStubRequest.getQuery()) : "");

        final String error = String.format("(404) Nothing found for %s request at URI %s%s%s%s", assertionStubRequest.getMethod().get(0), assertionStubRequest.getUrl(), postMessage, headersMessage, queryMessage);

        HandlerUtils.configureErrorResponse(response, HttpStatus.NOT_FOUND_404, error);
    }
}
