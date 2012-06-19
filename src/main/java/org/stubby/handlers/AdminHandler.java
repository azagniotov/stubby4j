package org.stubby.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.Repository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public final class AdminHandler extends AbstractHandler {

   private final static long serialVersionUID = 159L;
   private final Repository repository;
   private static String serverNameHeader = null;

   public AdminHandler(final Repository repository) {
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
      response.setContentType("text/html;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.setHeader("Server", serverNameHeader);

      if (request.getPathInfo().equals("/ping")) {
         response.setContentType("text/html;charset=utf-8");
         response.getWriter().println("Pong!\n\n" + getConfigDataPresentation());
         return;
      }

      final String welcomeMessage = "Hello :) Are you lost?<br /><br />If you meant to ping, then ";
      final String doPingLink = "<a href='" + request.getContextPath() + "/ping'>do ping</a>.";
      response.getWriter().println(welcomeMessage + doPingLink);
   }

   private String getConfigDataPresentation() {
      final List<List<Map<String, Object>>> data = repository.getHttpConfigData();
      final List<Map<String, Object>> requestData = data.get(0);
      final List<Map<String, Object>> requestHeaderData = data.get(1);
      final List<Map<String, Object>> responseData = data.get(2);
      final List<Map<String, Object>> responseHeaderData = data.get(3);

      final StringBuilder builder = new StringBuilder();
      builder.append("<html><head><title>Pong!</title>" +
            "<style type='text/css'>" +
            "table {color: #333333; font-family: 'Arial',sans-serif; font-size: 0.9em; line-height: 1.58;}" +
            "table { border: 1px solid #BBBBBB; border-collapse: collapse; border-spacing: 0; margin: 0 0 0.8em;}" +
            "table thead th { border: 1px solid #4D90FE; }" +
            "table th, table td {padding: 3px 8px;}" +
            "th {background-color: #6199DF; color: #FFFFFF; font-weight: bold;}" +
            "td {float: none !important; font-weight: normal; text-align: left; vertical-align: middle; }" +
            "table td {border: 1px solid #BBBBBB; vertical-align: top;}" +
            "code { color: #007000; font: 1em/1.5 'Droid Sans Mono',monospace;}" +
            "</style>" +
            "</head><body>");
      builder.append(String.format("<p>Have total of %s requests:</p>", requestData.size()));

      for (int idx = 0; idx < requestData.size(); idx++) {
         final Map<String, Object> requestHeaders = (requestHeaderData.size() > 0 ? requestHeaderData.get(idx) : null);
         builder.append(buildSomething("Request", requestData.get(idx), constructHeaders(requestHeaders)));
         final Map<String, Object> responseHeaders = (responseHeaderData.size() > 0 ? responseHeaderData.get(idx) : null);
         builder.append(buildSomething("Response", responseData.get(idx), constructHeaders(responseHeaders)));
         builder.append("<br /><br />");
      }
      builder.append("</body></html>");
      return builder.toString();
   }

   private String buildSomething(final String tableName, final Map<String, Object> rowData, final String rowHeaderData) {
      final StringBuilder builder = new StringBuilder();
      builder.append("<table width='95%' border='0'>");
      builder.append(String.format("<tr><th colspan='2' align='left'>%s</th></tr>", tableName));

      final String template = "<tr><td width='100px' valign='top' align='left'><code>%s</code></td><td align='left'>%s</td></tr>";
      builder.append(String.format(template, "HEADERS", rowHeaderData));
      for (final Map.Entry<String, Object> columnData : rowData.entrySet()) {
         if (!columnData.getKey().equals("ID")) {
            builder.append(String.format(template, columnData.getKey(), columnData.getValue()));
         }
      }
      builder.append("</table>");
      return builder.toString();
   }

   private String constructHeaders(final Map<String, Object> headerData) {
      final StringBuilder builder = new StringBuilder();

      if (headerData == null) {
         return builder.append("None").toString();
      }

      final List<Object> params = new LinkedList<Object>();
      for (final Map.Entry<String, Object> columnData : headerData.entrySet()) {

         if (!columnData.getKey().endsWith("ID")) {
            params.add(columnData.getValue());

            if (params.size() == 2) {
               builder.append(String.format("%s=%s ", params.get(0).toString(), params.get(1).toString()));
               params.clear();
            }
         }
      }
      return builder.toString();
   }
}
