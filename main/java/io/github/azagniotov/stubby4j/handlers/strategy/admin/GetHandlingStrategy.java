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
        final String pathInfoNoHeadingSlash = request.getRequestURI().substring(contextPathLength);

        if (StringUtils.isSet(pathInfoNoHeadingSlash)) {
            final int targetHttpStubCycleIndex = Integer.parseInt(pathInfoNoHeadingSlash);

            if (!stubRepository.canMatchStubByIndex(targetHttpStubCycleIndex)) {
                final String errorMessage = String.format("Stub request index#%s does not exist, cannot display", targetHttpStubCycleIndex);
                HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
                return;
            }

            yamlAppender.append(stubRepository.getStubYAMLByIndex(targetHttpStubCycleIndex));
        } else {
            yamlAppender.append(stubRepository.getStubYAML());
        }

        response.setContentType("text/plain;charset=UTF-8");

        try (final OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(getBytesUtf8(yamlAppender.toString()));
            outputStream.flush();
        }
    }
}
