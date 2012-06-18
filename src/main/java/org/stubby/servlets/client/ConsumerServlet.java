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

package org.stubby.servlets.client;

import org.stubby.database.Repository;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 7:47 PM
 */
public final class ConsumerServlet extends HttpServlet {

   private final static long serialVersionUID = 159L;
   private static String version = null;

   private final Repository repository;

   public ConsumerServlet(final Repository repository) {
      this.repository = repository;
      version = getClass().getPackage().getImplementationVersion();
   }

   @Override
   protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      final String postBody = null;
      retrieveStubResponse(request, response, postBody);
   }

   @Override
   protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      final InputStream postBodyInputStream = request.getInputStream();
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      final String postBody = new Scanner(postBodyInputStream, "UTF-8").useDelimiter("\\A").next();

      if (postBody == null || postBody.isEmpty()) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.getWriter().println("Oh oh :( \n\nBad request, POST body is missing");
         return;
      }
      retrieveStubResponse(request, response, postBody);
   }

   private void retrieveStubResponse(final HttpServletRequest request, final HttpServletResponse response, final String postBody) throws IOException {
      response.setContentType("text/plain;charset=utf-8");
      response.setHeader("Server", String.format("stubby4j-%s", version));

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