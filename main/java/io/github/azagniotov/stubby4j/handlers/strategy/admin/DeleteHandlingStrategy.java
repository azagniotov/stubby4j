package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteHandlingStrategy implements AdminResponseHandlingStrategy {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws IOException {

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            stubRepository.deleteAllStubs();
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println("Stub requests deleted successfully");
            return;
        }

        final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
        final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);
        final int stubIndexToDelete = Integer.parseInt(pathInfoNoHeadingSlash);

        if (!stubRepository.canMatchStubByIndex(stubIndexToDelete)) {
            final String errorMessage = String.format("Stub request index#%s does not exist, cannot delete", stubIndexToDelete);
            HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
            return;
        }

        stubRepository.deleteStubByIndex(stubIndexToDelete);
        response.setStatus(HttpStatus.OK_200);
        response.getWriter().println(String.format("Stub request index#%s deleted successfully", stubIndexToDelete));
    }
}
