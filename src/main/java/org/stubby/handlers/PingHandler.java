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

package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.server.JettyContext;
import org.stubby.server.JettyFactory;
import org.stubby.utils.ConsoleUtils;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.ReflectionUtils;
import org.stubby.utils.StringUtils;
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class PingHandler extends AbstractHandler {

   private static final String NAME = "admin";
   private static final String HTML_TAG_TR_PARAMETIZED_TEMPLATE = "<tr><td width='200px' valign='top' align='left'><code>%s</code></td><td class='%s' align='left'>%s</td></tr>";

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

      final String requestCounterHtml = HandlerUtils.getHtmlResourceByName("snippet_request_response_tables");
      for (final StubHttpLifecycle stubHttpLifecycle : stubHttpLifecycles) {
         final StubRequest stubRequest = stubHttpLifecycle.getRequest();
         final StubResponse stubResponse = stubHttpLifecycle.getResponse();
         builder.append(buildPageBodyHtml(requestCounterHtml, "Request", ReflectionUtils.getProperties(stubRequest)));
         builder.append(buildPageBodyHtml(requestCounterHtml, "Response", ReflectionUtils.getProperties(stubResponse)));
         builder.append("<br /><br />");
      }
      return HandlerUtils.populateHtmlTemplate("ping", stubHttpLifecycles.size(), builder.toString());
   }

   private String buildSystemStatusHtmlTable() throws Exception {

      final StringBuilder builder = new StringBuilder();

      final String host = jettyContext.getHost();
      final int clientPort = jettyContext.getStubsPort();
      final int adminPort = jettyContext.getAdminPort();

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CLIENT PORT", "", clientPort));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "ADMIN PORT", "", adminPort));

      if (jettyContext.isSslEnabled()) {
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "SSL PORT", "", JettyFactory.DEFAULT_SSL_PORT));
      }

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "HOST", "", host));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CONFIGURATION", "", yamlParser.getLoadedConfigYamlPath()));

      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, StubsRegistrationHandler.RESOURCE_STUBDATA_NEW, host, adminPort);
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "NEW STUB DATA POST URI", "", endpointRegistration));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");
      return String.format(systemStatusTable, builder.toString());
   }

   private String buildPageBodyHtml(final String requestCounterHtml, final String tableName, final Map<String, String> stubMemberFields) throws Exception {
      final StringBuilder builder = new StringBuilder();

      final String host = jettyContext.getHost();
      final int clientPort = jettyContext.getStubsPort();

      final String[] attributesToHighlight = HandlerUtils.getHighlightableHtmlAttributes("html_attributes_to_style.txt");

      for (final Map.Entry<String, String> keyValue : stubMemberFields.entrySet()) {
         Object value = keyValue.getValue();
         String responseClass = "";
         if (value != null) {
            value = StringUtils.escapeHtmlEntities(value.toString());

            if (keyValue.getKey().equalsIgnoreCase("body") || keyValue.getKey().equalsIgnoreCase("file")) {
               value = HandlerUtils.highlightResponseMarkup(value, attributesToHighlight);
               responseClass = "response";
            }
         }

         if (keyValue.getKey().equals("url")) {
            value = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, value, host, clientPort);

            if (jettyContext.isSslEnabled() && tableName.equalsIgnoreCase("request")) {
               final String sslUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, keyValue.getValue(), host, JettyFactory.DEFAULT_SSL_PORT);
               builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "SSL URL", responseClass, sslUrl));
            }
         }
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, StringUtils.toUpper(keyValue.getKey()), responseClass, value));
      }
      return String.format(requestCounterHtml, tableName, builder.toString());
   }
}
