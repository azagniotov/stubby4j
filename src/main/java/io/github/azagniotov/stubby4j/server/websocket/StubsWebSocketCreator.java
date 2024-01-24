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

package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class StubsWebSocketCreator implements WebSocketCreator {

    private final StubRepository stubRepository;

    public StubsWebSocketCreator(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public Object createWebSocket(
            final ServletUpgradeRequest servletUpgradeRequest, final ServletUpgradeResponse servletUpgradeResponse) {

        final StubWebSocketConfig stubWebSocketConfig =
                this.stubRepository.matchWebSocketConfigByUrl(servletUpgradeRequest.getRequestPath());

        // Renders HTTP error response if client requested an invalid URL
        checkAndHandleNotFound(stubWebSocketConfig, servletUpgradeRequest, servletUpgradeResponse);
        // Renders HTTP error response if client requested sub-protocol does not match the stubbed ones
        checkAndSetAcceptedProtocols(stubWebSocketConfig, servletUpgradeRequest, servletUpgradeResponse);

        return new StubsServerWebSocket(stubWebSocketConfig, Executors.newScheduledThreadPool(10));
    }

    private void checkAndHandleNotFound(
            final StubWebSocketConfig stubWebSocketConfig,
            ServletUpgradeRequest servletUpgradeRequest,
            ServletUpgradeResponse servletUpgradeResponse) {
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

    private void checkAndSetAcceptedProtocols(
            final StubWebSocketConfig stubWebSocketConfig,
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
