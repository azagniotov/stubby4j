package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubSearchResult;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory.getStrategy;

public class StubsPortalHandler extends AbstractHandler implements AbstractHandlerExtension {

    private final StubRepository stubRepository;

    public StubsPortalHandler(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException, ServletException {
        if (logAndCheckIsHandled("stubs", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);

        try {
            final StubSearchResult stubSearchResult = stubRepository.search(request);
            final StubResponseHandlingStrategy strategyStubResponse = getStrategy(stubSearchResult.getMatch());

            strategyStubResponse.handle(response, stubSearchResult.getInvariant());
            ConsoleUtils.logOutgoingResponse(stubSearchResult.getInvariant().getUrl(), response);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }
}
