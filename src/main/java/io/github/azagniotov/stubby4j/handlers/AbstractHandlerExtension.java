package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AbstractHandlerExtension {

    default boolean logAndCheckIsHandled(final String handlerName,
                                         final Request baseRequest,
                                         final HttpServletRequest request,
                                         final HttpServletResponse response) {
        ConsoleUtils.logIncomingRequest(request);
        if (baseRequest.isHandled() || response.isCommitted()) {
            ConsoleUtils.logIncomingRequestError(request, handlerName, "HTTP response was committed or base request was handled, aborting..");
            return true;
        }

        return false;
    }
}
