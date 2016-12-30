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

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class RedirectResponseHandlingStrategy implements StubResponseHandlingStrategy {

    private final StubResponse foundStubResponse;

    RedirectResponseHandlingStrategy(final StubResponse foundStubResponse) {
        this.foundStubResponse = foundStubResponse;
    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        HandlerUtils.setResponseMainHeaders(response);

        if (StringUtils.isSet(foundStubResponse.getLatency())) {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
        }

        response.setStatus(foundStubResponse.getHttpStatusCode().getCode());
        response.setHeader(HttpHeader.LOCATION.asString(), foundStubResponse.getHeaders().get("location"));
        response.setHeader(HttpHeader.CONNECTION.asString(), "close");
    }
}
