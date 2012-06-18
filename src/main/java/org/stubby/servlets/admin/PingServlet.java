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

package org.stubby.servlets.admin;

import org.stubby.database.Repository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 7:47 PM
 */
public final class PingServlet extends HttpServlet {

   private final static long serialVersionUID = 159L;

   private final Repository repository;

   public PingServlet(final Repository repository) {
      this.repository = repository;
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/plain;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().println("Pong!\n\n" + repository.getHealthCheck());
   }
}
