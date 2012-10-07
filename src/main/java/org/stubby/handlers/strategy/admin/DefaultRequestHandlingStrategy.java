package org.stubby.handlers.strategy.admin;

import org.stubby.database.DataStore;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 10/7/12, 2:18 PM
 */
public class DefaultRequestHandlingStrategy implements AdminRequestHandlingStrategy {

   private final DataStore dataStore;
   private final JettyOrchestrator jettyOrchestrator;

   public DefaultRequestHandlingStrategy(final JettyOrchestrator jettyOrchestrator) {
      this.jettyOrchestrator = jettyOrchestrator;
      this.dataStore = jettyOrchestrator.getDataStore();
   }

   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", request.getContextPath());
      response.getWriter().println(adminHandlerHtml);
   }
}
