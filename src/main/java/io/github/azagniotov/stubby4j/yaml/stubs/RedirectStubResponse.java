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

package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.utils.ObjectUtils;

import java.io.File;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class RedirectStubResponse extends StubResponse {

    public RedirectStubResponse(final String status,
                                final String body,
                                final File file,
                                final String latency,
                                final Map<String, String> headers) {
        super(status, body, file, latency, headers);
    }

    @Override
    public StubResponseTypes getStubResponseType() {
        return StubResponseTypes.REDIRECT;
    }

    public static RedirectStubResponse newRedirectStubResponse(final StubResponse stubResponse) {
        if (ObjectUtils.isNull(stubResponse)) {
            return new RedirectStubResponse(null, null, null, null, null);
        }
        final RedirectStubResponse redirectStubResponse = new RedirectStubResponse(
                stubResponse.getStatus(),
                stubResponse.getBody(),
                stubResponse.getRawFile(),
                stubResponse.getLatency(),
                stubResponse.getHeaders()
        );

        return redirectStubResponse;
    }
}