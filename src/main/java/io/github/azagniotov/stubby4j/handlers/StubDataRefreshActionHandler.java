package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("serial")
@GeneratedCodeCoverageExclusion
public final class StubDataRefreshActionHandler extends AbstractHandler implements AbstractHandlerExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubDataRefreshActionHandler.class);

    private final StubRepository stubRepository;

    public StubDataRefreshActionHandler(final StubRepository newStubRepository) {
        this.stubRepository = newStubRepository;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        if (logAndCheckIsHandled("stubData", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        response.setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());

        try {
            stubRepository.refreshStubsFromYamlConfig(new YamlParser());
            final String successMessage = String.format("Successfully performed live refresh of main YAML from: %s on [" + DateTimeUtils.systemDefault() + "]",
                    stubRepository.getYamlConfig());
            response.getWriter().println(successMessage);
            ANSITerminal.ok(successMessage);
            LOGGER.info("Successfully performed live refresh of main YAML from {}.", stubRepository.getYamlConfig());
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }
}