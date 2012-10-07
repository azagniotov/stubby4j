package org.stubby.handlers.strategy.admin;

import org.stubby.handlers.AdminEndpoints;
import org.stubby.server.JettyOrchestrator;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 11:27 AM
 */
public final class AdminRequestHandlingStrategyFactory {

   public static AdminRequestHandlingStrategy identifyHandlingStrategyFor(final String pathInfo, final JettyOrchestrator jettyOrchestrator) {

      switch (AdminEndpoints.getFor(pathInfo)) {

         case PING:
            return new PingRequestHandlingStrategy(jettyOrchestrator);

         case STUBDATA_NEW:
            return new SetupStubDataHandlingStrategy(jettyOrchestrator);

      }

      return new DefaultRequestHandlingStrategy(jettyOrchestrator);
   }
}
