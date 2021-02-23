package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StubsResponseHandlingStrategyFactoryTest {

    private static final byte[] EMPTY_BYTES = {};

    @Mock
    private StubResponse mockStubResponse;

    @Test
    public void shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(EMPTY_BYTES);

        StubResponseHandlingStrategy handlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);

        assertThat(handlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn("something".getBytes());

        StubResponseHandlingStrategy handlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);

        assertThat(handlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }
}
