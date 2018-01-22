package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostHandlingStrategy implements AdminResponseHandlingStrategy {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception {

        if (!request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.getWriter().println("Method POST is not allowed on URI " + request.getRequestURI());
            return;
        }

        final String post = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
        if (!StringUtils.isSet(post)) {
            final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
        }

        stubRepository.refreshStubsByPost(new YamlParser(), post);

        if (stubRepository.getStubs().size() == 1) {
            response.addHeader(HttpHeader.LOCATION.asString(), stubRepository.getOnlyStubRequestUrl());
        }

        response.setStatus(HttpStatus.CREATED_201);
        response.getWriter().println("Configuration created successfully");
    }
}