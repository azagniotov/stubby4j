package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.stubs.StubRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AdminResponseHandlingStrategy {
    void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception;
}
