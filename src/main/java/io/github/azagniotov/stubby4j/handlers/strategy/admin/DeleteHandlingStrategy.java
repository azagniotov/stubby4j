package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
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
            stubRepository.clear();
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println("All in-memory YAML config was deleted successfully");
            return;
        }

        final String[] uriFragments = splitRequestURI(request);
        if (uriFragments.length == 1) {
            final String lastUriPathSegment = uriFragments[0];
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
        } else if (uriFragments.length == 2) {
            // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
            final String maybeProxyConfig = uriFragments[0];

            if (REGEX_PROXY_CONFIG.matcher(maybeProxyConfig).matches()) {
                final String proxyConfigUuid = uriFragments[uriFragments.length - 1];

                // We attempt to delete a proxy config by uuid, e.g.: DELETE localhost:8889/proxy-config/9136d8b7-f7a7-478d-97a5-53292484aaf6
                if (!stubRepository.canMatchProxyConfigByUuid(proxyConfigUuid)) {
                    final String errorMessage = String.format("Proxy config uuid#%s does not exist, cannot delete", proxyConfigUuid);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }

                if (proxyConfigUuid.equals(StubProxyConfig.Builder.DEFAULT_UUID)) {
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, "Proxy config cannot be deleted");
                    return;
                }

                stubRepository.deleteProxyConfigByUuid(proxyConfigUuid);
                response.setStatus(HttpStatus.OK_200);
                response.getWriter().println(String.format("Proxy config uuid#%s deleted successfully", proxyConfigUuid));
            } else {
                final String errorMessage = String.format("Invalid URI path requested: %s", maybeProxyConfig);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            }
        }
    }
}
