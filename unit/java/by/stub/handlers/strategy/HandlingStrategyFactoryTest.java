package by.stub.handlers.strategy;

import by.stub.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import by.stub.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

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
