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
import java.io.OutputStream;
import java.util.Arrays;

import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws IOException {

        final StringBuilder yamlAppender = new StringBuilder();

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            yamlAppender.append(stubRepository.dumpCompleteYamlConfig());

            response.setStatus(HttpStatus.OK_200);
            response.setContentType("text/plain;charset=UTF-8");

            try (final OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(getBytesUtf8(yamlAppender.toString()));
                outputStream.flush();
            }
            return;
        }

        // e.g.: http://localhost:8889/<NUMERIC_ID>
        // e.g.: http://localhost:8889/<ALPHA_NUMERIC_UUID_STRING>
        // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
        final String[] uriFragments = Arrays.stream(request.getRequestURI().split("/"))
                .filter(uriPath -> !uriPath.trim().isEmpty())
                .map(String::trim)
                .toArray(String[]::new);

        if (uriFragments.length == 1) {
            final String lastUriPathSegment = uriFragments[0];
            if (StringUtils.isSet(lastUriPathSegment)) {

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
            }

            response.setContentType("text/plain;charset=UTF-8");

            try (final OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(getBytesUtf8(yamlAppender.toString()));
                outputStream.flush();
            }
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

                response.setContentType("text/plain;charset=UTF-8");

                try (final OutputStream outputStream = response.getOutputStream()) {
                    outputStream.write(getBytesUtf8(yamlAppender.toString()));
                    outputStream.flush();
                }

            } else {
                final String errorMessage = String.format("Invalid URI path requested: %s", maybeProxyConfig);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            }
        }
    }
}
