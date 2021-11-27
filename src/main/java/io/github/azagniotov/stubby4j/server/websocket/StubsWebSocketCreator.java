package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_HTTP_ERROR_REAL_REASON;

public class StubsWebSocketCreator implements WebSocketCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StubsWebSocketCreator.class);

    private final StubRepository stubRepository;

    public StubsWebSocketCreator(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                                  final ServletUpgradeResponse servletUpgradeResponse) {

        final StubWebSocketConfig stubWebSocketConfig = this.stubRepository.matchWebSocketConfigByUrl(servletUpgradeRequest.getRequestPath());
        // The client made request to a non-existent URL
        if (stubWebSocketConfig == null) {
            try {
                servletUpgradeResponse.setStatusCode(HttpStatus.NOT_FOUND_404);
                // Setting custom error message will no longer work in Jetty versions > 9.4.20, see:
                // https://github.com/eclipse/jetty.project/issues/4154
                // response.sendError(httpStatus, message);
                //
                // using header as a medium to pass an error message to JsonErrorHandler. This is a workaround as a result of the above
                final String notFoundMessage = "Not found " + servletUpgradeRequest.getRequestPath();
                servletUpgradeResponse.setHeader(HEADER_X_STUBBY_HTTP_ERROR_REAL_REASON, notFoundMessage);

                servletUpgradeResponse.sendError(HttpStatus.NOT_FOUND_404, HttpStatus.Code.NOT_FOUND.getMessage());

                ANSITerminal.error(notFoundMessage);
                LOGGER.error(notFoundMessage);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        final StubsServerWebSocket stubsServerWebSocket = new StubsServerWebSocket(stubWebSocketConfig);
        stubsServerWebSocket.checkAndSetAcceptedProtocols(servletUpgradeRequest, servletUpgradeResponse);
        return stubsServerWebSocket;
    }
}
