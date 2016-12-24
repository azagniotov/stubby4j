package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.yaml.stubs.NotFoundStubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.RedirectStubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import io.github.azagniotov.stubby4j.yaml.stubs.UnauthorizedStubResponse;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 1:42 PM
 */

public class HandlingStrategyFactoryTest {

    @Test
    public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.newStubResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
        final StubResponse stubResponse = new NotFoundStubResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
        final StubResponse stubResponse = new UnauthorizedStubResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(UnauthorizedResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponse() throws Exception {
        final StubResponse stubResponse = RedirectStubResponse.newRedirectStubResponse(null);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }
}
