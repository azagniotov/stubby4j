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

import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;

public final class StubsResponseHandlingStrategyFactory {

    private StubsResponseHandlingStrategyFactory() {

    }

    public static StubResponseHandlingStrategy getStrategy(final StubResponse foundStubResponse) {

        final HttpStatus.Code httpStatusCode = foundStubResponse.getHttpStatusCode();
        switch (httpStatusCode) {
            case NOT_FOUND:
                return new NotFoundResponseHandlingStrategy();

            case UNAUTHORIZED:
                return new UnauthorizedResponseHandlingStrategy();

            case MOVED_PERMANENTLY:
                /*
                  This is an example of industry practice contradicting the standard.
                  The HTTP/1.0 specification (RFC 1945) required the client to perform a temporary redirect
                  (the original describing phrase was "Moved Temporarily"), but popular browsers implemented 302 with
                  the functionality of a 303 See Other. Therefore, HTTP/1.1 added status codes 303 and 307 to
                  distinguish between the two behaviours. However, some Web applications and frameworks use the 302
                  status code as if it were the 303.
                 */
            case FOUND:
            case MOVED_TEMPORARILY:
            case SEE_OTHER:
            case TEMPORARY_REDIRECT:
                /*
                  The request and all future requests should be repeated using another URI. 307 and 308 parallel the
                  behaviors of 302 and 301, but do not allow the HTTP method to change. So, for example, submitting a
                  form to a permanently redirected resource may continue smoothly.
                 */
            case PERMANET_REDIRECT:
                return new RedirectResponseHandlingStrategy(foundStubResponse);

            default:
                return new DefaultResponseHandlingStrategy(foundStubResponse);

        }
    }
}
