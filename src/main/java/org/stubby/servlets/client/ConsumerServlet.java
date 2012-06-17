package org.stubby.servlets.client;

import org.stubby.database.Repository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

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

      final String pathInfo = request.getPathInfo();
      final String method = request.getMethod();

      if (pathInfo == null || method == null) {
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         response.getWriter().println("Oh oh :( \n\nBad request, path info or method are missing");
         return;
      }
      final Map<String, String> responseBody = repository.findResponseFor(method, pathInfo);
      if (responseBody.size() == 1) {
         response.setStatus(HttpServletResponse.SC_OK);
         response.getWriter().println(responseBody.get("null"));
         return;
      }
      response.setStatus(Integer.parseInt(responseBody.get("STATUS")));
      response.getWriter().println(responseBody.get("BODY"));
   }
}