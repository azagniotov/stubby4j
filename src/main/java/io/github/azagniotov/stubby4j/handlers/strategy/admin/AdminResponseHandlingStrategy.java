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

import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AdminResponseHandlingStrategy {

    Logger LOGGER = LoggerFactory.getLogger(AdminResponseHandlingStrategy.class);
    Pattern REGEX_PROXY_CONFIG = Pattern.compile("^(proxy-config)$");

    void handle(
            final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository)
            throws Exception;

    default void writeResponseOutputStream(final HttpServletResponse response, final String payload) {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        try (final OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(getBytesUtf8(payload));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Error writing to output stream: ", e);
        }
    }

    default String[] splitRequestURI(final HttpServletRequest request) {
        // e.g.: http://localhost:8889/<NUMERIC_ID>
        // e.g.: http://localhost:8889/<ALPHA_NUMERIC_UUID_STRING>
        // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
        return Arrays.stream(request.getRequestURI().split("/"))
                .filter(uriPath -> !uriPath.trim().isEmpty())
                .map(String::trim)
                .toArray(String[]::new);
    }

    default Optional<String> extractRequestBodyWithOptionalError(
            final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String payload = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
        if (!StringUtils.isSet(payload)) {
            final String errorMessage =
                    String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
            HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            return Optional.empty();
        }

        return Optional.ofNullable(payload);
    }
}
