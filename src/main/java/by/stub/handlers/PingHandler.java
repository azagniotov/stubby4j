/*
HTTP stub server written in Java with embedded Jetty

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

package by.stub.handlers;

import by.stub.database.DataStore;
import by.stub.server.JettyContext;
import by.stub.server.JettyFactory;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlParser;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class PingHandler extends AbstractHandler {

   private static final String CSS_CLASS_HIGHLIGHTABLE = "highlightable";
   private static final String CSS_CLASS_NO_HIGHLIGHTABLE = "no-highlightable";
   private static final String NAME = "admin";
   private static final String HTML_TABLE_ROW_TEMPLATE = "<tr><td width='200px' valign='top' align='left'>%s</td><td class='%s' align='left'>%s</td></tr>";
   private static List<String> highlightableProperties = Collections.unmodifiableList(new ArrayList<String>() {{
      add("file");
      add("body");
      add("post");
   }});

   private final DataStore dataStore;
   private final JettyContext jettyContext;
   private final YamlParser yamlParser;

   public PingHandler(final JettyContext newContext, final DataStore newDataStore, final YamlParser newYamlParser) {
      this.jettyContext = newContext;
      this.dataStore = newDataStore;
      this.yamlParser = newYamlParser;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      response.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      response.setStatus(HttpStatus.OK_200);
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      try {
         response.getWriter().println(getConfigDataPresentation());
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }
   }

   private String getConfigDataPresentation() throws Exception {

      final List<StubHttpLifecycle> stubHttpLifecycles = dataStore.getStubHttpLifecycles();

      final StringBuilder builder = new StringBuilder();
      builder.append(buildSystemStatusHtmlTable());
      builder.append("<br /><br />");

      final String htmlTemplateContent = HandlerUtils.getHtmlResourceByName("snippet_request_response_tables");

      for (final StubHttpLifecycle stubHttpLifecycle : stubHttpLifecycles) {
         final StubRequest stubRequest = stubHttpLifecycle.getRequest();
         final StubResponse stubResponse = stubHttpLifecycle.getResponse();

         builder.append(buildPageBodyHtml(htmlTemplateContent, "Request", ReflectionUtils.getProperties(stubRequest)));
         builder.append(buildPageBodyHtml(htmlTemplateContent, "Response", ReflectionUtils.getProperties(stubResponse)));
         builder.append("<br /><br />");
      }

      return HandlerUtils.populateHtmlTemplate("ping", stubHttpLifecycles.size(), builder.toString());
   }

   private String buildSystemStatusHtmlTable() throws Exception {

      final StringBuilder builder = new StringBuilder();

      final String host = jettyContext.getHost();
      final int clientPort = jettyContext.getStubsPort();
      final int adminPort = jettyContext.getAdminPort();

      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "CLIENT PORT", CSS_CLASS_NO_HIGHLIGHTABLE, clientPort));
      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "ADMIN PORT", CSS_CLASS_NO_HIGHLIGHTABLE, adminPort));
      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "SSL PORT", CSS_CLASS_NO_HIGHLIGHTABLE, JettyFactory.DEFAULT_SSL_PORT));
      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "HOST", CSS_CLASS_NO_HIGHLIGHTABLE, host));
      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "CONFIGURATION", CSS_CLASS_NO_HIGHLIGHTABLE, yamlParser.getLoadedConfigYamlPath()));

      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP,
            StubsRegistrationHandler.RESOURCE_STUBDATA_NEW, host, adminPort);
      builder.append(String.format(HTML_TABLE_ROW_TEMPLATE, "NEW STUB DATA POST URI", CSS_CLASS_NO_HIGHLIGHTABLE, endpointRegistration));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");

      return String.format(systemStatusTable, builder.toString());
   }


   private String buildPageBodyHtml(final String htmlTemplateContent, final String tableName, final Map<String, String> stubObjectProperties) throws Exception {
      final StringBuilder builder = new StringBuilder();

      for (final Map.Entry<String, String> keyValue : stubObjectProperties.entrySet()) {
         final String value = keyValue.getValue();
         final String key = keyValue.getKey();

         if (!StringUtils.isSet(value)) {
            continue;
         }

         builder.append(constructTableRow(key, value));
      }
      return String.format(htmlTemplateContent, tableName, builder.toString());
   }


   private String constructTableRow(final String key, final String value) {
      final String escapedValue = StringUtils.escapeHtmlEntities(value);

      if (highlightableProperties.contains(key)) {
         final String row = String.format("%s%s%s", "<pre><code>", escapedValue, "</code></pre>");

         return String.format(HTML_TABLE_ROW_TEMPLATE, StringUtils.toUpper(key), CSS_CLASS_HIGHLIGHTABLE, row);
      }

      if (key.equals("url")) {
         final String linkifiedUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, escapedValue, jettyContext.getHost(), jettyContext.getStubsPort());
         final String linkifiedSslUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, escapedValue, jettyContext.getHost(), JettyFactory.DEFAULT_SSL_PORT);

         final String tableRowWithUrl = String.format(HTML_TABLE_ROW_TEMPLATE, StringUtils.toUpper(key), CSS_CLASS_NO_HIGHLIGHTABLE, linkifiedUrl);
         final String tableRowWithSslUrl = String.format(HTML_TABLE_ROW_TEMPLATE, "SSL " + StringUtils.toUpper(key), CSS_CLASS_NO_HIGHLIGHTABLE, linkifiedSslUrl);

         return String.format("%s%s", tableRowWithUrl, tableRowWithSslUrl);
      }

      return String.format(HTML_TABLE_ROW_TEMPLATE, StringUtils.toUpper(key), CSS_CLASS_NO_HIGHLIGHTABLE, escapedValue);
   }
}