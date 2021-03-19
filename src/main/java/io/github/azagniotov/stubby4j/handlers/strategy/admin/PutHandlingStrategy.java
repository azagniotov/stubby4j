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
import java.util.Optional;

public class PutHandlingStrategy implements AdminResponseHandlingStrategy {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception {

        if (request.getRequestURI().equals(AdminPortalHandler.ADMIN_ROOT)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            return;
        }

        final String[] uriFragments = splitRequestURI(request);
        if (uriFragments.length == 1) {
            final String lastUriPathSegment = uriFragments[0];

            // We are trying to update a stub by ID, e.g.: PUT localhost:8889/8
            if (StringUtils.isNumeric(lastUriPathSegment)) {
                final int stubIndexToUpdate = Integer.parseInt(lastUriPathSegment);

                if (!stubRepository.canMatchStubByIndex(stubIndexToUpdate)) {
                    final String errorMessage = String.format("Stub request index#%s does not exist, cannot update", stubIndexToUpdate);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }

                final Optional<String> payloadOptional = extractRequestBodyWithOptionalError(request, response);
                if (payloadOptional.isPresent()) {
                    final String updatedCycleUrl = stubRepository.refreshStubByIndex(new YamlParser(), payloadOptional.get(), stubIndexToUpdate);
                    response.setStatus(HttpStatus.CREATED_201);
                    response.addHeader(HttpHeader.LOCATION.asString(), updatedCycleUrl);
                    final String successfulMessage = String.format("Stub request index#%s updated successfully", stubIndexToUpdate);
                    response.getWriter().println(successfulMessage);
                }
            } else {
                // We attempt to update a stub by uuid as a fallback, e.g.: UPDATE localhost:8889/9136d8b7-f7a7-478d-97a5-53292484aaf6
                if (!stubRepository.canMatchStubByUuid(lastUriPathSegment)) {
                    final String errorMessage = String.format("Stub request uuid#%s does not exist, cannot update", lastUriPathSegment);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }

                final Optional<String> payloadOptional = extractRequestBodyWithOptionalError(request, response);
                if (payloadOptional.isPresent()) {
                    final String updatedCycleUrl = stubRepository.refreshStubByUuid(new YamlParser(), payloadOptional.get(), lastUriPathSegment);
                    response.setStatus(HttpStatus.CREATED_201);
                    response.addHeader(HttpHeader.LOCATION.asString(), updatedCycleUrl);
                    final String successfulMessage = String.format("Stub request uuid#%s updated successfully", lastUriPathSegment);
                    response.getWriter().println(successfulMessage);
                }
            }
        } else if (uriFragments.length == 2) {
            // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
            final String maybeProxyConfig = uriFragments[0];

            if (REGEX_PROXY_CONFIG.matcher(maybeProxyConfig).matches()) {
                final String proxyConfigUuid = uriFragments[uriFragments.length - 1];

                // We attempt to update a proxy config by uuid, e.g.: PUT localhost:8889/proxy-config/9136d8b7-f7a7-478d-97a5-53292484aaf6
                if (!stubRepository.canMatchProxyConfigByUuid(proxyConfigUuid)) {
                    final String errorMessage = String.format("Proxy config uuid#%s does not exist, cannot update", proxyConfigUuid);
                    HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
                    return;
                }

                final Optional<String> payloadOptional = extractRequestBodyWithOptionalError(request, response);
                if (payloadOptional.isPresent()) {
                    final String proxyEndpointUrl = stubRepository.refreshProxyConfigByUuid(new YamlParser(), payloadOptional.get(), proxyConfigUuid);
                    response.setStatus(HttpStatus.CREATED_201);
                    response.addHeader(HttpHeader.LOCATION.asString(), proxyEndpointUrl);
                    final String successfulMessage = String.format("Proxy config uuid#%s updated successfully", proxyConfigUuid);
                    response.getWriter().println(successfulMessage);
                }

            } else {
                final String errorMessage = String.format("Invalid URI path requested: %s", maybeProxyConfig);
                HandlerUtils.configureErrorResponse(response, HttpStatus.BAD_REQUEST_400, errorMessage);
            }
        }
    }
}
