package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
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
        final String lastUriPathSegment = request.getRequestURI().substring(contextPathLength);

        // We are trying to delete a stub by ID, e.g.: DELETE localhost:8889/8
        if (StringUtils.isNumeric(lastUriPathSegment)) {
            final int stubIndexToDelete = Integer.parseInt(lastUriPathSegment);

            if (!stubRepository.canMatchStubByIndex(stubIndexToDelete)) {
                final String errorMessage = String.format("Stub request index#%s does not exist, cannot delete", stubIndexToDelete);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                return;
            }

            stubRepository.deleteStubByIndex(stubIndexToDelete);
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(String.format("Stub request index#%s deleted successfully", stubIndexToDelete));
        } else {
            // We attempt to delete a stub by uuid as a fallback, e.g.: DELETE localhost:8889/9136d8b7-f7a7-478d-97a5-53292484aaf6
            if (!stubRepository.canMatchStubByUuid(lastUriPathSegment)) {
                final String errorMessage = String.format("Stub request uuid#%s does not exist, cannot delete", lastUriPathSegment);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                return;
            }

            stubRepository.deleteStubByUuid(lastUriPathSegment);
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(String.format("Stub request uuid#%s deleted successfully", lastUriPathSegment));
        }
    }
}
