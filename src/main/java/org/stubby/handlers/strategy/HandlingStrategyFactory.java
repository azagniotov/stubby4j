package org.stubby.handlers.strategy;

import org.eclipse.jetty.http.HttpStatus;
import org.stubby.yaml.stubs.StubResponse;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 11:27 AM
 */
public final class HandlingStrategyFactory {

   public static HandlingStrategy identifyHandlingStrategyFor(final StubResponse foundStubResponse) {

      switch (foundStubResponse.getHttpStatus()) {
         case HttpStatus.NOT_FOUND_404:
            return new NotFoundResponseHandlingStrategy(foundStubResponse);

         case HttpStatus.UNAUTHORIZED_401:
            return new UnauthorizedResponseHandlingStrategy(foundStubResponse);

      }

      return new DefaultResponseHandlingStrategy(foundStubResponse);
   }
}
