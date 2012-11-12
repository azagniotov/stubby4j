package unit.by.stub.handlers.strategy;

import by.stub.handlers.strategy.DefaultResponseHandlingStrategy;
import by.stub.handlers.strategy.HandlingStrategyFactory;
import by.stub.handlers.strategy.NotFoundResponseHandlingStrategy;
import by.stub.handlers.strategy.RedirectResponseHandlingStrategy;
import by.stub.handlers.strategy.StubResponseHandlingStrategy;
import by.stub.handlers.strategy.UnauthorizedResponseHandlingStrategy;
import by.stub.testing.junit.categories.UnitTest;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 1:42 PM
 */
@Category(UnitTest.class)
public class HandlingStrategyFactoryTest {

   @Test
   public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
      final StubResponse stubResponse = new StubResponse();

      final StubResponseHandlingStrategy stubResponseHandlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(stubResponseHandlingStrategy instanceof DefaultResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
      final StubResponse stubResponse = new NotFoundStubResponse();

      final StubResponseHandlingStrategy stubResponseHandlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(stubResponseHandlingStrategy instanceof NotFoundResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
      final StubResponse stubResponse = new UnauthorizedStubResponse();

      final StubResponseHandlingStrategy stubResponseHandlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(stubResponseHandlingStrategy instanceof UnauthorizedResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForRedirectResponse() throws Exception {
      final StubResponse stubResponse = new RedirectStubResponse();

      final StubResponseHandlingStrategy stubResponseHandlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(stubResponseHandlingStrategy instanceof RedirectResponseHandlingStrategy);
   }
}
