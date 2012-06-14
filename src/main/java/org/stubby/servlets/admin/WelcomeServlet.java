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
