/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.handlers;

import by.stub.database.DataStore;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class StubsRegistrationHandler extends AbstractHandler {

   private static final String NAME = "admin";

   //Do not remove this constant without changing the example in documentation
   public static final String ENDPOINT = "/endpoint";
   private final DataStore dataStore;

   public StubsRegistrationHandler(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      if (!request.getMethod().equalsIgnoreCase("post")) {
         final String errorMessage = String.format("Method %s is not allowed on URI %s", request.getMethod(), ENDPOINT);
         HandlerUtils.configureErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED_405, errorMessage);
         return;
      }

      final String post = HandlerUtils.extractPostRequestBody(request, NAME);
      if (!StringUtils.isSet(post)) {
         final String errorMessage = String.format("%s request on URI %s was empty", request.getMethod(), request.getPathInfo());
         HandlerUtils.configureErrorResponse(response, HttpStatus.NO_CONTENT_204, errorMessage);
         return;
      }

      try {

         final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(dataStore.getDataYaml().getParent(), FileUtils.constructReader(post));

         dataStore.resetStubHttpLifecycles(stubHttpLifecycles);

         response.setStatus(HttpStatus.CREATED_201);
         response.getWriter().println("Configuration created successfully");

         ConsoleUtils.logOutgoingResponse(request.getRequestURI(), new HttpServletResponseWithGetStatus(response), NAME);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not parse POSTed YAML configuration: " + ex.toString());
      }
   }
}
