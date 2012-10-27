package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.utils.ConsoleUtils;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import javax.servlet.ServletException;
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
 * @since 10/25/12, 8:56 AM
 */
public class StubsRegistrationHandler extends AbstractHandler {

   private static final String NAME = "admin";

   //Do not remove this constant without changing the example in documentation
   public static final String RESOURCE_STUBDATA_NEW = "/stubdata/new";
   private final DataStore dataStore;
   private final YamlParser yamlParser;

   public StubsRegistrationHandler(final DataStore dataStore, final YamlParser yamlParser) {
      this.dataStore = dataStore;
      this.yamlParser = yamlParser;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      if (!request.getMethod().equalsIgnoreCase("post")) {
         final String errorMessage = String.format("Method %s is not allowed on URI %s", request.getMethod(), request.getPathInfo());
         HandlerUtils.configureErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED_405, errorMessage);
         return;
      }

      final String postBody = HandlerUtils.extractPostRequestBody(request, NAME);
      if (postBody == null || postBody.trim().length() == 0) {
         final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getPathInfo());
         HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      try {
         final InputStream is = new ByteArrayInputStream(postBody.getBytes(StringUtils.utf8Charset()));
         final Reader yamlReader = new InputStreamReader(is, StringUtils.utf8Charset());

         final List<StubHttpLifecycle> stubHttpLifecycles = yamlParser.load(yamlReader);

         dataStore.resetStubHttpLifecycles(stubHttpLifecycles);

         response.setStatus(HttpStatus.CREATED_201);
         response.getWriter().println("Configuration created successfully");

         ConsoleUtils.logOutgoingResponse(request, response, NAME);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not parse POSTed YAML configuration: " + ex.toString());
      }
   }
}
