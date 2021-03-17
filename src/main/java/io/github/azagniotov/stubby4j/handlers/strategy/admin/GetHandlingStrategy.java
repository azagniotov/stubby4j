package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws IOException {

        final StringBuilder yamlAppender = new StringBuilder();

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            yamlAppender.append(stubRepository.dumpCompleteYamlConfig());
            writeResponseOutputStream(response, yamlAppender.toString());
            return;
        }

        final String[] uriFragments = splitRequestURI(request);
        if (uriFragments.length == 1) {
            final String lastUriPathSegment = uriFragments[0];

            // We are trying to get a stub by ID, e.g.: GET localhost:8889/8
            if (StringUtils.isNumeric(lastUriPathSegment)) {

                final int targetHttpStubCycleIndex = Integer.parseInt(lastUriPathSegment);
                if (!stubRepository.canMatchStubByIndex(targetHttpStubCycleIndex)) {
                    final String errorMessage = String.format("Stub request index#%s does not exist, cannot display", targetHttpStubCycleIndex);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }
                yamlAppender.append(stubRepository.getStubYamlByIndex(targetHttpStubCycleIndex));

            } else {
                // We attempt to get a stub by uuid as a fallback, e.g.: GET localhost:8889/9136d8b7-f7a7-478d-97a5-53292484aaf6
                if (!stubRepository.canMatchStubByUuid(lastUriPathSegment)) {
                    final String errorMessage = String.format("Stub request uuid#%s does not exist, cannot display", lastUriPathSegment);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }
                yamlAppender.append(stubRepository.getStubYamlByUuid(lastUriPathSegment));
            }

            writeResponseOutputStream(response, yamlAppender.toString());

        } else if (uriFragments.length == 2) {
            // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
            final String maybeProxyConfig = uriFragments[0];

            if (REGEX_PROXY_CONFIG.matcher(maybeProxyConfig).matches()) {
                final String proxyConfigUuid = uriFragments[uriFragments.length - 1];

                // We attempt to get a proxy config by uuid, e.g.: GET localhost:8889/proxy-config/9136d8b7-f7a7-478d-97a5-53292484aaf6
                if (!stubRepository.canMatchProxyConfigByUuid(proxyConfigUuid)) {
                    final String errorMessage = String.format("Proxy config uuid#%s does not exist, cannot display", proxyConfigUuid);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }

                yamlAppender.append(stubRepository.getProxyConfigYamlByUuid(proxyConfigUuid));
                writeResponseOutputStream(response, yamlAppender.toString());

            } else {
                final String errorMessage = String.format("Invalid URI path requested: %s", maybeProxyConfig);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            }
        }
    }
}
