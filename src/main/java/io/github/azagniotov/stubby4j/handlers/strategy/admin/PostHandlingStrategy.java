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

package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

public class PostHandlingStrategy implements AdminResponseHandlingStrategy {

    private static final int NUM_OF_STUBS_THRESHOLD = 1;

    @Override
    public void handle(
            final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository)
            throws Exception {

        if (!request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            return;
        }

        final Optional<String> payloadOptional = extractRequestBodyWithOptionalError(request, response);
        if (payloadOptional.isPresent()) {
            try {
                stubRepository.refreshStubsByPost(new YamlParser(), payloadOptional.get());
                if (stubRepository.getStubs().size() == NUM_OF_STUBS_THRESHOLD) {
                    response.addHeader(HttpHeader.LOCATION.asString(), stubRepository.getOnlyStubRequestUrl());
                }

                response.setStatus(HttpStatus.CREATED_201);
                response.getWriter().println("Configuration created successfully");
            } catch (IOException a) {
                // Thrown by YamlParser if there are duplicate UUID keys or un-parseable YAML
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, a.getMessage());
            }
        }
    }
}
