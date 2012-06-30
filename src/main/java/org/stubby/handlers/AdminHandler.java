package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.Repository;
import org.stubby.server.JettyOrchestrator;
import org.stubby.yaml.YamlConsumer;

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

   private static final String HTML_TAG_TR_PARAMETIZED_TEMPLATE = "<tr><td width='120px' valign='top' align='left'><code>%s</code></td><td align='left'>%s</td></tr>";

   private final Repository repository;

   public AdminHandler(final Repository repository) {
      this.repository = repository;
   }

   @Override
   public void handle(final String target,
                      final Request baseRequest,
                      final HttpServletRequest request,
                      final HttpServletResponse response) throws IOException, ServletException {

      baseRequest.setHandled(true);
      response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      if (request.getPathInfo().equals("/ping")) {
         response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
         response.getWriter().println(getConfigDataPresentation());
         return;
      }

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", request.getContextPath());
      response.getWriter().println(adminHandlerHtml);
   }

   private String getConfigDataPresentation() {
      final List<List<Map<String, Object>>> data = repository.getHttpConfigData();
      final List<Map<String, Object>> requestData = data.get(0);
      final List<Map<String, Object>> requestHeaderData = data.get(1);
      final List<Map<String, Object>> responseData = data.get(2);
      final List<Map<String, Object>> responseHeaderData = data.get(3);

      final StringBuilder builder = new StringBuilder();

      builder.append(buildRequestCounterHtmlTable(requestData));
      builder.append(buildSystemStatusHtmlTable());

      final String requestCounterHtml = HandlerUtils.getHtmlResourceByName("snippet_request_response_tables");
      for (int idx = 0; idx < requestData.size(); idx++) {
         final Map<String, Object> requestHeaders = (requestHeaderData.size() > 0 ? requestHeaderData.get(idx) : null);
         builder.append(buildPageBodyHtml(requestCounterHtml, "Request", requestData.get(idx), constructTableRowWithHeadersData(requestHeaders)));
         final Map<String, Object> responseHeaders = (responseHeaderData.size() > 0 ? responseHeaderData.get(idx) : null);
         builder.append(buildPageBodyHtml(requestCounterHtml, "Response", responseData.get(idx), constructTableRowWithHeadersData(responseHeaders)));
      }

      return HandlerUtils.populateHtmlTemplate("ping", requestData.size(), builder.toString());
   }

   private String buildSystemStatusHtmlTable() {

      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CLIENT PORT", JettyOrchestrator.CURRENT_CLIENT_PORT));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "ADMIN PORT", JettyOrchestrator.CURRENT_ADMIN_PORT));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "HOST", JettyOrchestrator.CURRENT_HOST));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CONFIGURATION", YamlConsumer.LOADED_CONFIG));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");
      return String.format(systemStatusTable, builder.toString());
   }

   private String buildRequestCounterHtmlTable(final List<Map<String, Object>> requestData) {

      final StringBuilder builder = new StringBuilder();
      for (final Map<String, Object> rowData : requestData) {
         final String urlAsHyperLink = HandlerUtils.linkifyRequestUrl(rowData.get(Repository.TBL_COLUMN_URL));
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, urlAsHyperLink, rowData.get(Repository.TBL_COLUMN_COUNTER)));
      }
      final String requestCounterHtml = HandlerUtils.getHtmlResourceByName("snippet_request_counter_table");
      return String.format(requestCounterHtml, builder.toString());
   }

   private String buildPageBodyHtml(final String requestCounterHtml, final String tableName, final Map<String, Object> rowData, final String rowHeaderData) {
      final StringBuilder builder = new StringBuilder();

      for (final Map.Entry<String, Object> columnData : rowData.entrySet()) {
         if (!columnData.getKey().equals(Repository.TBL_COLUMN_ID)) {

            Object value = columnData.getValue();
            if (columnData.getKey().equals(Repository.TBL_COLUMN_URL)) {
               value = HandlerUtils.linkifyRequestUrl(rowData.get(Repository.TBL_COLUMN_URL));
            } else if (value != null) {
               value = HandlerUtils.escapeHtmlEntities(value.toString());
            }

            builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, columnData.getKey(), value));
         }
      }
      return String.format(requestCounterHtml, tableName, rowHeaderData, builder.toString());
   }

   private String constructTableRowWithHeadersData(final Map<String, Object> headerData) {
      final StringBuilder builder = new StringBuilder();

      if (headerData == null) {
         return builder.append("None").toString();
      }

      final List<Object> params = new LinkedList<Object>();
      for (final Map.Entry<String, Object> columnData : headerData.entrySet()) {

         if (!columnData.getKey().endsWith(Repository.TBL_COLUMN_ID)) {
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
