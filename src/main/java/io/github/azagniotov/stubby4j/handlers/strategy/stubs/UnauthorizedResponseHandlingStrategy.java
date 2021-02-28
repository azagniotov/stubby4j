package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;


public final class UnauthorizedResponseHandlingStrategy implements StubResponseHandlingStrategy {

    @VisibleForTesting
    public static final String NO_AUTHORIZATION_HEADER = "You are not authorized to view this page without supplied 'Authorization' HTTP header";
    @VisibleForTesting
    public static final String WRONG_AUTHORIZATION_HEADER_TEMPLATE = "Unauthorized with supplied 'authorized' header value: '%s'";

    UnauthorizedResponseHandlingStrategy() {

    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        HandlerUtils.setResponseMainHeaders(response);
        final String authorizationHeader = assertionStubRequest.getRawHeaderAuthorization();
        if (!StringUtils.isSet(authorizationHeader)) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, NO_AUTHORIZATION_HEADER);
            return;
        }

        HandlerUtils.configureErrorResponse(response, HttpStatus.UNAUTHORIZED_401, String.format(WRONG_AUTHORIZATION_HEADER_TEMPLATE, authorizationHeader));
    }
}
