package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.handlers.AdminPortalHandler;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

public class GetHandlingStrategy implements AdminResponseHandlingStrategy {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws IOException {

        final StringBuilder yamlAppender = new StringBuilder();

        final int contextPathLength = AdminPortalHandler.ADMIN_ROOT.length();
        final String lastUriPathSegment = request.getRequestURI().substring(contextPathLength);

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
        } else {
            yamlAppender.append(stubRepository.dumpCompleteYamlConfig());
        }

        response.setContentType("text/plain;charset=UTF-8");

        try (final OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(getBytesUtf8(yamlAppender.toString()));
            outputStream.flush();
        }
    }
}
