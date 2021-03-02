package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AbstractHandlerExtension {

    @GeneratedCodeCoverageExclusion
    default boolean logAndCheckIsHandled(final String handlerName,
                                         final Request baseRequest,
                                         final HttpServletRequest request,
                                         final HttpServletResponse response) {
        ConsoleUtils.logIncomingRequest(request);
        if (response.isCommitted() || baseRequest.isHandled()) {
            ConsoleUtils.logIncomingRequestError(request, handlerName, "HTTP response was committed or base request was handled, aborting..");
            return true;
        }

        return false;
    }
}
