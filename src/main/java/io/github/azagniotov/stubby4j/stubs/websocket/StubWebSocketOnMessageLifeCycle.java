package io.github.azagniotov.stubby4j.stubs.websocket;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeMethodCoverageExclusion;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;

import java.util.Objects;

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

    @Override
    @GeneratedCodeMethodCoverageExclusion
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubWebSocketOnMessageLifeCycle that = (StubWebSocketOnMessageLifeCycle) o;
        return clientRequest.equals(that.clientRequest) && serverResponse.equals(that.serverResponse);
    }

    @Override
    @GeneratedCodeMethodCoverageExclusion
    public int hashCode() {
        return Objects.hash(clientRequest, serverResponse);
    }
}
