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

import static io.github.azagniotov.stubby4j.utils.HandlerUtils.getHtmlResourceByName;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

@GeneratedCodeClassCoverageExclusion
public class AjaxEndpointStatsHandler extends AbstractHandler implements AbstractHandlerExtension {

    private final StubRepository stubRepository;

    public AjaxEndpointStatsHandler(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public void handle(
            final String target,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {
        if (logAndCheckIsHandled("ajaxEndpoint", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);

        HandlerUtils.setResponseMainHeaders(response);
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);

        try {
            if (request.getRequestURI().contains("stats/check")) {
                response.getWriter().println(!stubRepository.getResourceStats().isEmpty());
            } else {
                ConsoleUtils.logIncomingRequest(request);
                final String popupStatsHtmlTemplate = getHtmlResourceByName("_popup_stats");
                final String htmlPopup = String.format(popupStatsHtmlTemplate, stubRepository.getResourceStatsAsCsv());
                response.getWriter().println(htmlPopup);
                ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
            }
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }
}
