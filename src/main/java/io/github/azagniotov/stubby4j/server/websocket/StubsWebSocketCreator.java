package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class StubsWebSocketCreator implements WebSocketCreator {

    private final StubRepository stubRepository;

    public StubsWebSocketCreator(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                                  final ServletUpgradeResponse servletUpgradeResponse) {

        final StubWebSocketConfig stubWebSocketConfig = this.stubRepository.matchWebSocketConfigByUrl(servletUpgradeRequest.getRequestPath());

        // Renders HTTP error response if client requested an invalid URL
        checkAndHandleNotFound(stubWebSocketConfig, servletUpgradeRequest, servletUpgradeResponse);
        // Renders HTTP error response if client requested sub-protocol does not match the stubbed ones
        checkAndSetAcceptedProtocols(stubWebSocketConfig, servletUpgradeRequest, servletUpgradeResponse);

        return new StubsServerWebSocket(stubWebSocketConfig, Executors.newScheduledThreadPool(10));
    }

    private void checkAndHandleNotFound(final StubWebSocketConfig stubWebSocketConfig, ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        // The client made request to a non-existent URL
        if (stubWebSocketConfig == null) {
            try {
                servletUpgradeResponse.setStatusCode(HttpStatus.NOT_FOUND_404);
                servletUpgradeResponse.sendError(HttpStatus.NOT_FOUND_404, HttpStatus.Code.NOT_FOUND.getMessage());

                ConsoleUtils.logOutgoingWebSocketResponse(servletUpgradeResponse);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void checkAndSetAcceptedProtocols(final StubWebSocketConfig stubWebSocketConfig,
                                              final ServletUpgradeRequest servletUpgradeRequest,
                                              final ServletUpgradeResponse servletUpgradeResponse) {

        // We have configured sub-protocols, so the client must conform to contract
        if (!stubWebSocketConfig.getSubProtocols().isEmpty()) {

            final Set<String> requestClientSubProtocolsCopy = new HashSet<>(servletUpgradeRequest.getSubProtocols());
            requestClientSubProtocolsCopy.retainAll(stubWebSocketConfig.getSubProtocols());

            if (requestClientSubProtocolsCopy.isEmpty()) {
                try {
                    servletUpgradeResponse.setStatusCode(HttpStatus.FORBIDDEN_403);
                    servletUpgradeResponse.sendError(HttpStatus.FORBIDDEN_403, HttpStatus.Code.FORBIDDEN.getMessage());

                    ConsoleUtils.logOutgoingWebSocketResponse(servletUpgradeResponse);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                final String acceptedSubProtocol = String.join(",", requestClientSubProtocolsCopy);
                servletUpgradeResponse.setAcceptedSubProtocol(acceptedSubProtocol);
            }
        }
    }
}
