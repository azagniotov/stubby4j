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
