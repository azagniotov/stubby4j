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

package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@GeneratedCodeClassCoverageExclusion
public final class StubDataRefreshActionHandler extends AbstractHandler implements AbstractHandlerExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubDataRefreshActionHandler.class);

    private final StubRepository stubRepository;

    public StubDataRefreshActionHandler(final StubRepository newStubRepository) {
        this.stubRepository = newStubRepository;
    }

    @Override
    public void handle(
            final String target,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {
        if (logAndCheckIsHandled("stubData", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        response.setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());

        try {
            stubRepository.refreshStubsFromYamlConfig(new YamlParser());
            final String successMessage = String.format(
                    "Successfully performed live refresh of main YAML from: %s on [" + DateTimeUtils.systemDefault()
                            + "]",
                    stubRepository.getYamlConfig());
            response.getWriter().println(successMessage);
            ANSITerminal.ok(successMessage);
            LOGGER.info("Successfully performed live refresh of main YAML from {}.", stubRepository.getYamlConfig());
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }
}
