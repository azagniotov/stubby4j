package io.github.azagniotov.stubby4j.stubs.websocket;

import io.github.azagniotov.stubby4j.stubs.ReflectableStub;

public class StubWebSocketOnMessageLifeCycle implements ReflectableStub {

    private final StubWebSocketClientRequest clientRequest;
    private final StubWebSocketServerResponse serverResponse;
    private final String completeYAML;

    public StubWebSocketOnMessageLifeCycle(
            final StubWebSocketClientRequest clientRequest,
            final StubWebSocketServerResponse serverResponse,
            final String completeYAML) {
        this.clientRequest = clientRequest;
        this.serverResponse = serverResponse;
        this.completeYAML = completeYAML;
    }

    public StubWebSocketClientRequest getClientRequest() {
        return clientRequest;
    }

    public StubWebSocketServerResponse getServerResponse() {
        return serverResponse;
    }

    public String getCompleteYAML() {
        return completeYAML;
    }
}
