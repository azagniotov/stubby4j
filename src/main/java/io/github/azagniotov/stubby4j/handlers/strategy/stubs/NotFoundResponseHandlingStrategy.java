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

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

public final class NotFoundResponseHandlingStrategy implements StubResponseHandlingStrategy {

    NotFoundResponseHandlingStrategy() {}

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {

        HandlerUtils.setResponseMainHeaders(response);

        final String reason = String.format(
                "(404) Nothing found for %s request at URI %s",
                assertionStubRequest.getMethod().get(0), assertionStubRequest.getUrl());

        final JSONObject json404Response = new JSONObject();
        json404Response.put("reason", reason);
        json404Response.put("method", assertionStubRequest.getMethod().get(0));
        json404Response.put("url", assertionStubRequest.getUrl());

        if (assertionStubRequest.hasQuery()) {
            json404Response.put("query", new JSONObject(assertionStubRequest.getQuery()));
        }

        if (assertionStubRequest.hasHeaders()) {
            json404Response.put("headers", new JSONObject(assertionStubRequest.getHeaders()));
        }

        if (assertionStubRequest.hasPostBody()) {
            json404Response.put("post", assertionStubRequest.getPostBody());
        }
        HandlerUtils.configureErrorResponse(response, HttpStatus.NOT_FOUND_404, json404Response.toString());
    }
}
