/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import static io.github.azagniotov.stubby4j.utils.StringUtils.isTokenized;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokensInString;

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;

public class RedirectResponseHandlingStrategy implements StubResponseHandlingStrategy {

    private final StubResponse foundStubResponse;

    RedirectResponseHandlingStrategy(final StubResponse foundStubResponse) {
        this.foundStubResponse = foundStubResponse;
    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        HandlerUtils.setResponseMainHeaders(response);
        final Map<String, String> regexGroups = assertionStubRequest.getRegexGroups();

        if (StringUtils.isSet(foundStubResponse.getLatency())) {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
        }

        final String headerLocation = foundStubResponse.getHeaders().get("location");
        if (isTokenized(headerLocation)) {
            response.setHeader(HttpHeader.LOCATION.asString(), replaceTokensInString(headerLocation, regexGroups));
        } else {
            response.setHeader(HttpHeader.LOCATION.asString(), headerLocation);
        }

        response.setStatus(foundStubResponse.getHttpStatusCode().getCode());
        response.setHeader(HttpHeader.CONNECTION.asString(), "close");
    }
}
