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

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;


public final class UnauthorizedResponseHandlingStrategy implements StubResponseHandlingStrategy {

    @VisibleForTesting
    public static final String NO_AUTHORIZATION_HEADER = "You are not authorized to view this page without supplied 'Authorization' HTTP header";
    @VisibleForTesting
    public static final String WRONG_AUTHORIZATION_HEADER_TEMPLATE = "Unauthorized with supplied 'authorized' header value: '%s'";

    UnauthorizedResponseHandlingStrategy() {

    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        HandlerUtils.setResponseMainHeaders(response);
        final String authorizationHeader = assertionStubRequest.getRawAuthorizationHttpHeader();
        if (!StringUtils.isSet(authorizationHeader)) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, NO_AUTHORIZATION_HEADER);
            return;
        }

        HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, String.format(WRONG_AUTHORIZATION_HEADER_TEMPLATE, authorizationHeader));
    }
}
