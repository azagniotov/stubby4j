package org.stubby.servlets.client;

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
public final class ConsumerServlet extends HttpServlet {

   private final static long serialVersionUID = 159L;

   private final Repository repository;

   public ConsumerServlet(final Repository repository) {
      this.repository = repository;
   }


   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/plain;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);

      final String id = request.getParameter("id");

      if (id == null) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.getWriter().println("Oh oh :(");
         return;
      }
      final String queryResult = repository.executeQueryWithParams(id, "John-" + id, "Doe-" + id);
      response.getWriter().println(queryResult);
   }
}