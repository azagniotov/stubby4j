package org.stubby.handlers.strategy;

import org.stubby.yaml.stubs.StubResponse;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 11:27 AM
 */
public final class HandlingStrategyFactory {

   public static StubResponseHandlingStrategy identifyHandlingStrategyFor(final StubResponse foundStubResponse) {

      switch (foundStubResponse.getStubResponseType()) {
         case NOTFOUND:
            return new NotFoundResponseHandlingStrategy(foundStubResponse);

         case UNAUTHORIZED:
            return new UnauthorizedResponseHandlingStrategy(foundStubResponse);

         case REDIRECT:
            return new RedirectResponseHandlingStrategy(foundStubResponse);

      }

      return new DefaultResponseHandlingStrategy(foundStubResponse);
   }
}
