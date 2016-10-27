package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.database.StubbedDataManager;
import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteHandlingStrategy implements AdminResponseHandlingStrategy {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubbedDataManager stubbedDataManager) throws IOException {

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.getWriter().println("Method DELETE is not allowed on URI " + request.getRequestURI());
            return;
        }

        final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
        final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);
        final int stubIndexToDelete = Integer.parseInt(pathInfoNoHeadingSlash);

        if (!stubbedDataManager.canMatchStubByIndex(stubIndexToDelete)) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot delete", stubIndexToDelete);
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
        }

        stubbedDataManager.deleteStubByIndex(stubIndexToDelete);
        response.setStatus(HttpStatus.OK_200);
        response.getWriter().println(String.format("Stub request index#%s deleted successfully", stubIndexToDelete));
    }
}
