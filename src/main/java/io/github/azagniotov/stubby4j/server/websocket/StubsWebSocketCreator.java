package io.github.azagniotov.stubby4j.server.websocket;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class StubsWebSocketCreator implements WebSocketCreator {

    private final StubRepository stubRepository;

    public StubsWebSocketCreator(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public Object createWebSocket(final ServletUpgradeRequest servletUpgradeRequest,
                                  final ServletUpgradeResponse servletUpgradeResponse) {

        final StubWebSocketConfig stubWebSocketConfig = this.stubRepository.matchWebSocketConfigByUrl(servletUpgradeRequest.getRequestPath());
        final StubsServerWebSocket stubsServerWebSocket = new StubsServerWebSocket(stubWebSocketConfig);
        stubsServerWebSocket.checkAndSetAcceptedProtocols(servletUpgradeRequest, servletUpgradeResponse);
        return stubsServerWebSocket;
    }
}
