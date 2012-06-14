package org.stubby.servlets.admin;

import org.stubby.database.Queries;
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

      final String pong = "Pong!\n\n";
      final String queryResult = repository.executeQuery(Queries.SELECT_ALL_FROM_PERSON);

      response.getWriter().println(pong + queryResult);
   }
}
