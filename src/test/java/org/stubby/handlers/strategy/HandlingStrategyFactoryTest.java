package org.stubby.handlers.strategy;

import junit.framework.Assert;
import org.junit.Test;
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

      final HandlingStrategy handlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(handlingStrategy instanceof DefaultResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
      final StubResponse stubResponse = new NotFoundStubResponse();

      final HandlingStrategy handlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(handlingStrategy instanceof NotFoundResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
      final StubResponse stubResponse = new UnauthorizedStubResponse();

      final HandlingStrategy handlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(handlingStrategy instanceof UnauthorizedResponseHandlingStrategy);
   }

   @Test
   public void shouldIdentifyResponseStrategyForRedirectResponse() throws Exception {
      final StubResponse stubResponse = new RedirectStubResponse();

      final HandlingStrategy handlingStrategy = HandlingStrategyFactory.identifyHandlingStrategyFor(stubResponse);
      Assert.assertTrue(handlingStrategy instanceof RedirectResponseHandlingStrategy);
   }
}
