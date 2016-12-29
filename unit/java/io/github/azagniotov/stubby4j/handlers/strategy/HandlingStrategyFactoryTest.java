package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.junit.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;


public class HandlingStrategyFactoryTest {

    @Test
    public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.okResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.notFoundResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.unauthorizedResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(UnauthorizedResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.redirectResponse(Optional.empty());

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }
}
