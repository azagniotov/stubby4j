package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class PostHandlingStrategy implements AdminResponseHandlingStrategy {

    private static final int NUM_OF_STUBS_THRESHOLD = 1;

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception {

        if (!request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            return;
        }

        final Optional<String> payloadOptional = extractRequestBodyWithOptionalError(request, response);
        if (payloadOptional.isPresent()) {
            try {
                stubRepository.refreshStubsByPost(new YamlParser(), payloadOptional.get());
                if (stubRepository.getStubs().size() == NUM_OF_STUBS_THRESHOLD) {
                    response.addHeader(HttpHeader.LOCATION.asString(), stubRepository.getOnlyStubRequestUrl());
                }

                response.setStatus(HttpStatus.CREATED_201);
                response.getWriter().println("Configuration created successfully");
            } catch (IOException a) {
                // Thrown by YamlParser if there are duplicate UUID keys or un-parseable YAML
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, a.getMessage());
            }
        }
    }
}
