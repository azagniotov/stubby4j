package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import org.eclipse.jetty.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NullHandlingStrategy implements AdminResponseHandlingStrategy {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws IOException {
        response.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
        response.getWriter().println(String.format("Method %s is not implemented on URI %s", request.getMethod(), request.getRequestURI()));

    }
}
