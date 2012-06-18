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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 6/11/12, 7:47 PM
 */
public final class WelcomeServlet extends HttpServlet {

   private final static long serialVersionUID = 159L;

   public WelcomeServlet() {

   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      final String welcomeMessage = "Hello :) Are you lost?<br /><br />If you meant to ping, then ";
      final String doPingLink = "<a href='" + request.getContextPath() + "/ping'>do ping</a>.";
      response.getWriter().println(welcomeMessage + doPingLink);
   }
}
