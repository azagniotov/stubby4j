package org.stubby.handlers.strategy;

import junit.framework.Assert;
import org.junit.Test;
import org.stubby.handlers.strategy.client.DefaultResponseHandlingStrategy;
import org.stubby.handlers.strategy.client.NotFoundResponseHandlingStrategy;
import org.stubby.handlers.strategy.client.RedirectResponseHandlingStrategy;
import org.stubby.handlers.strategy.client.StubResponseHandlingStrategy;
import org.stubby.handlers.strategy.client.HandlingStrategyFactory;
import org.stubby.handlers.strategy.client.UnauthorizedResponseHandlingStrategy;
import org.stubby.yaml.stubs.NotFoundStubResponse;
import org.stubby.yaml.stubs.RedirectStubResponse;
import org.stubby.yaml.stubs.StubResponse;
import org.stubby.yaml.stubs.UnauthorizedStubResponse;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 1:42 PM
 */
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
