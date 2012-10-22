package org.stubby.handlers.strategy.admin;

import org.eclipse.jetty.http.HttpStatus;
import org.stubby.database.DataStore;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 10/7/12, 2:19 PM
 */
public class SetupStubDataHandlingStrategy implements AdminRequestHandlingStrategy {

   private final DataStore dataStore;
   private final JettyOrchestrator jettyOrchestrator;

   public SetupStubDataHandlingStrategy(final JettyOrchestrator jettyOrchestrator) {
      this.jettyOrchestrator = jettyOrchestrator;
      this.dataStore = jettyOrchestrator.getDataStore();
   }


   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      if (!request.getMethod().equalsIgnoreCase("post")) {
         final String errorMessage = String.format("Method %s is not allowed on URI %s", request.getMethod(), request.getPathInfo());
         HandlerUtils.configureErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED_405, errorMessage);
         return;
      }

      final String postBody = HandlerUtils.extractPostRequestBody(request, "admin");
      if (postBody == null) {
         response.setStatus(HttpStatus.NO_CONTENT_204);
         response.getWriter().println("The POST request did not contain any content");
         return;
      }

      try {
         final InputStream is = new ByteArrayInputStream(postBody.getBytes(Charset.forName("UTF-8")));
         final Reader yamlReader = new InputStreamReader(is);

         final List<StubHttpLifecycle> stubHttpLifecycles = jettyOrchestrator.getYamlParser().load(yamlReader);
         if (dataStore.getStubHttpLifecycles().size() > 0) {
            dataStore.getStubHttpLifecycles().clear();
         }
         dataStore.setStubHttpLifecycles(stubHttpLifecycles);
      } catch (Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not parse POSTed YAML configuration: " + ex.toString());
         return;
      }

      response.setStatus(HttpStatus.CREATED_201);
      response.getWriter().println("Configuration created successfully");
   }
}
