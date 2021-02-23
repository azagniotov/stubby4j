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

package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubTypes;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.azagniotov.stubby4j.utils.HandlerUtils.getHtmlResourceByName;

public class AjaxResourceContentHandler extends AbstractHandler {

    private static final Pattern REGEX_REQUEST = Pattern.compile("^(request)$");
    private static final Pattern REGEX_RESPONSE = Pattern.compile("^(response)$");
    private static final Pattern REGEX_HTTPLIFECYCLE = Pattern.compile("^(httplifecycle)$");
    private static final Pattern REGEX_NUMERIC = Pattern.compile("^[0-9]+$");

    private final StubRepository stubRepository;

    public AjaxResourceContentHandler(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        ConsoleUtils.logIncomingRequest(request);
        if (response.isCommitted() || baseRequest.isHandled()) {
            ConsoleUtils.logIncomingRequestError(request, "ajaxResource", "HTTP response was committed or base request was handled, aborting..");
            return;
        }
        baseRequest.setHandled(true);

        HandlerUtils.setResponseMainHeaders(response);
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);

        final String[] uriFragments = request.getRequestURI().split("/");
        final int urlFragmentsLength = uriFragments.length;
        final String targetFieldName = uriFragments[urlFragmentsLength - 1];
        final String stubType = uriFragments[urlFragmentsLength - 2];

        if (REGEX_NUMERIC.matcher(stubType).matches()) {
            final int sequencedResponseId = Integer.parseInt(stubType);
            final int resourceIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 4]);
            final StubHttpLifecycle foundStub = throwErrorOnNonExistentResourceIndex(response, resourceIndex);
            renderAjaxResponseContent(response, sequencedResponseId, targetFieldName, foundStub);
        } else {

            final int resourceIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 3]);
            final StubHttpLifecycle foundStub = throwErrorOnNonExistentResourceIndex(response, resourceIndex);
            if (REGEX_REQUEST.matcher(stubType).matches()) {
                renderAjaxResponseContent(response, StubTypes.REQUEST, targetFieldName, foundStub);
            } else if (REGEX_RESPONSE.matcher(stubType).matches()) {
                renderAjaxResponseContent(response, StubTypes.RESPONSE, targetFieldName, foundStub);
            } else if (REGEX_HTTPLIFECYCLE.matcher(stubType).matches()) {
                renderAjaxResponseContent(response, StubTypes.HTTPLIFECYCLE, targetFieldName, foundStub);
            } else {
                response.getWriter().println(String.format("Could not fetch the content for stub type: %s", stubType));
            }
        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }

    @VisibleForTesting
    void renderAjaxResponseContent(final HttpServletResponse response, final StubTypes stubType, final String targetFieldName, final StubHttpLifecycle foundStub) throws IOException {
        try {
            final String ajaxResponse = foundStub.getAjaxResponseContent(stubType, targetFieldName);
            final String popupHtmlTemplate = getHtmlResourceByName("_popup_generic");
            final String htmlPopup = String.format(popupHtmlTemplate, foundStub.getResourceId(), foundStub.getUUID(), ajaxResponse);
            response.getWriter().println(htmlPopup);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    @VisibleForTesting
    void renderAjaxResponseContent(final HttpServletResponse response, final int sequencedResponseId, final String targetFieldName, final StubHttpLifecycle foundStub) throws IOException {
        try {
            final String ajaxResponse = foundStub.getAjaxResponseContent(targetFieldName, sequencedResponseId);
            final String popupHtmlTemplate = getHtmlResourceByName("_popup_generic");
            final String htmlPopup = String.format(popupHtmlTemplate, foundStub.getResourceId(), foundStub.getUUID(), ajaxResponse);
            response.getWriter().println(htmlPopup);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    @VisibleForTesting
    StubHttpLifecycle throwErrorOnNonExistentResourceIndex(final HttpServletResponse response, final int resourceIndex) throws IOException {
        final Optional<StubHttpLifecycle> foundStubOptional = stubRepository.matchStubByIndex(resourceIndex);
        if (!foundStubOptional.isPresent()) {
            final String error = "Resource does not exist for ID: " + resourceIndex;
            response.getWriter().println(error);
            throw new IOException(error);
        }
        return foundStubOptional.get();
    }
}
