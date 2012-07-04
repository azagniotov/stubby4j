/*
A Java-based HTTP stub server

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

package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.NullStubResponse;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public class ClientHandler extends AbstractHandler {

   protected static final String BAD_POST_REQUEST_MESSAGE = "Oh oh :( Bad request, POST body is missing";
   private final DataStore dataStore;

   public ClientHandler(final DataStore dataStore) {
      this.dataStore = dataStore;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {

      baseRequest.setHandled(true);
      setResponseMainHeaders(response);

      String postBody = null;
      if (request.getMethod().toLowerCase().equals("post")) {

         try {
            postBody = HandlerUtils.inputStreamToString(request.getInputStream());
            if (postBody == null || postBody.isEmpty()) {
               createResponseToHandleBadPost(response);
               return;
            }
         } catch (Exception ex) {
            createResponseToHandleBadPost(response);
            return;
         }
      }

      final StubResponse stubResponse = dataStore.findResponseFor(constructFullURI(request), request.getMethod(), postBody);
      if (stubResponse instanceof NullStubResponse) {
         response.setStatus(HttpStatus.NOT_FOUND_404);
         final String error = generate404ErrorMessage(request, postBody);
         response.sendError(HttpStatus.NOT_FOUND_404, error);

         return;
      }

      setStubResponseHeaders(stubResponse, response);
      response.setStatus(Integer.parseInt(stubResponse.getStatus()));
      response.getWriter().println(stubResponse.getBody());
   }

   private String generate404ErrorMessage(final HttpServletRequest request, final String postBody) {
      final String postMessage = (postBody != null ? " for post data: " + postBody : "");
      return "No data found for " + request.getMethod() + " request at URI " + constructFullURI(request) + postMessage;
   }

   private void createResponseToHandleBadPost(final HttpServletResponse response) throws IOException {
      response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
      response.setStatus(HttpStatus.BAD_REQUEST_400);
      response.sendError(HttpStatus.BAD_REQUEST_400, BAD_POST_REQUEST_MESSAGE);
   }

   private String constructFullURI(final HttpServletRequest request) {
      final String pathInfo = request.getPathInfo();
      final String queryStr = request.getQueryString();
      final String queryString = (queryStr == null || queryStr.equals("")) ? "" : String.format("?%s", request.getQueryString());
      return String.format("%s%s", pathInfo, queryString);
   }

   private void setResponseMainHeaders(final HttpServletResponse response) {
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      response.setHeader(HttpHeaders.DATE, new Date().toString());
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"); // HTTP 1.1.
      response.setHeader(HttpHeaders.PRAGMA, "no-cache"); // HTTP 1.0.
      response.setDateHeader(HttpHeaders.EXPIRES, 0);
   }

   private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
      response.setCharacterEncoding("UTF-8");
      for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
         response.setHeader(entry.getKey(), entry.getValue());
      }
   }
}