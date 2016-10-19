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

import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;

public final class StubsResponseHandlingStrategyFactory {

    private StubsResponseHandlingStrategyFactory() {

    }

    public static StubResponseHandlingStrategy getStrategy(final StubResponse foundStubResponse) {

        switch (foundStubResponse.getStubResponseType()) {
            case NOTFOUND:
                return new NotFoundResponseHandlingStrategy();

            case UNAUTHORIZED:
                return new UnauthorizedResponseHandlingStrategy();

            case REDIRECT:
                return new RedirectResponseHandlingStrategy(foundStubResponse);

            default:
                return new DefaultResponseHandlingStrategy(foundStubResponse);

        }
    }
}
