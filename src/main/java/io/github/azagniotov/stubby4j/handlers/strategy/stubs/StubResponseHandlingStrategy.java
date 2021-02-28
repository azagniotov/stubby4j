package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import io.github.azagniotov.stubby4j.stubs.StubRequest;

import javax.servlet.http.HttpServletResponse;

public interface StubResponseHandlingStrategy {
    void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception;
}
