package org.stubby.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.Repository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public final class ClientHandler extends AbstractHandler {

   private final static long serialVersionUID = 159L;
   private static String serverNameHeader = null;

   private final Repository repository;

   public ClientHandler(final Repository repository) {
      this.repository = repository;
      final String implementationVersion = getClass().getPackage().getImplementationVersion();
      final String implementationTitle = getClass().getPackage().getImplementationTitle();
      serverNameHeader = String.format("stubby4j/%s (%s)", implementationVersion, implementationTitle);
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {

      baseRequest.setHandled(true);
      response.setContentType("text/plain;charset=utf-8");
      response.setHeader("Server", serverNameHeader);

      String postBody = null;
      if (request.getMethod().toLowerCase().equals("post")) {
         final InputStream postBodyInputStream = request.getInputStream();
         // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
         // the entire stream, from beginning to (illogical) next beginning.
         postBody = new Scanner(postBodyInputStream, "UTF-8").useDelimiter("\\A").next();
         if (postBody == null || postBody.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Oh oh :( \n\nBad request, POST body is missing");
            return;
         }
      }

      final Map<String, String> responseBody = repository.retrieveResponseFor(request.getPathInfo(), request.getMethod(), postBody);
      if (responseBody.size() == 1) {
         response.setStatus(HttpServletResponse.SC_OK);
         response.getWriter().println(responseBody.get("null"));
         return;
      }

      response.setStatus(Integer.parseInt(responseBody.get("STATUS")));
      response.getWriter().println(responseBody.get("BODY"));
   }
}