package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutHandlingStrategy implements AdminResponseHandlingStrategy {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception {

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.getWriter().println("Method PUT is not allowed on URI " + request.getRequestURI());
            return;
        }

        final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
        final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);
        final int stubIndexToUpdate = Integer.parseInt(pathInfoNoHeadingSlash);

        if (!stubRepository.canMatchStubByIndex(stubIndexToUpdate)) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot update", stubIndexToUpdate);
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
        }

        final String put = HandlerUtils.extractPostRequestBody(request, AdminPortalHandler.NAME);
        if (!StringUtils.isSet(put)) {
            final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getRequestURI());
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
        }

        final String updatedCycleUrl = stubRepository.refreshStubByIndex(new YAMLParser(), put, stubIndexToUpdate);

        response.setStatus(HttpStatus.CREATED_201);
        response.addHeader(HttpHeader.LOCATION.asString(), updatedCycleUrl);
        final String successfulMessage = String.format("Stub request index#%s updated successfully", stubIndexToUpdate);
        response.getWriter().println(successfulMessage);
    }
}
