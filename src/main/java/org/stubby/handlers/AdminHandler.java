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

package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public final class AdminHandler extends AbstractHandler {

   public static final String RESOURCE_PING = "/ping";
   public static final String RESOURCE_STUBDATA_NEW = "/stubdata/new";

   private static final String HTML_TAG_TR_PARAMETIZED_TEMPLATE = "<tr><td width='200px' valign='top' align='left'><code>%s</code></td><td class='%s' align='left'>%s</td></tr>";

   private final DataStore dataStore;
   private final JettyOrchestrator jettyOrchestrator;

   public AdminHandler(final JettyOrchestrator jettyOrchestrator) {
      this.jettyOrchestrator = jettyOrchestrator;
      this.dataStore = jettyOrchestrator.getDataStore();
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

      if (request.getPathInfo().equals(AdminHandler.RESOURCE_PING)) {
         handleGetOnPing(response);
         return;
      } else if (request.getPathInfo().equals(AdminHandler.RESOURCE_STUBDATA_NEW)) {
         handlePostOnRegisteringNewStubData(request, response);
         return;
      }

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", request.getContextPath());
      response.getWriter().println(adminHandlerHtml);
   }

   private void handlePostOnRegisteringNewStubData(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

      if (!request.getMethod().equalsIgnoreCase("post")) {
         final String errorMessage = String.format("Method %s is not allowed on URI %s", request.getMethod(), request.getPathInfo());
         HandlerUtils.configureErrorResponse(response, HttpStatus.METHOD_NOT_ALLOWED_405, errorMessage);
         return;
      }

      final String postBody = HandlerUtils.extractPostRequestBody(request, response);
      if (postBody == null) return;

      try {
         final InputStream is = new ByteArrayInputStream(postBody.getBytes(Charset.forName("UTF-8")));
         final Reader yamlReader = new InputStreamReader(is);

         final List<StubHttpLifecycle> stubHttpLifecycles = jettyOrchestrator.getYamlParser().load(yamlReader);
         if (dataStore.getStubHttpLifecycles().size() > 0) {
            dataStore.getStubHttpLifecycles().clear();
         }
         dataStore.setStubHttpLifecycles(stubHttpLifecycles);
      } catch (Exception ex) {
         HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "Could not parse POSTed YAML configuration: " + ex.toString());
         return;
      }

      response.setStatus(HttpStatus.CREATED_201);
      response.getWriter().println("Configuration created successfully");
   }

   private void handleGetOnPing(final HttpServletResponse response) throws IOException {
      try {
         response.getWriter().println(getConfigDataPresentation());
      } catch (Exception ex) {
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

      final String host = jettyOrchestrator.getCurrentHost();
      final int clientPort = jettyOrchestrator.getCurrentClientPort();
      final int adminPort = jettyOrchestrator.getCurrentAdminPort();

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CLIENT PORT", "", clientPort));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "ADMIN PORT", "", adminPort));

      if (jettyOrchestrator.isSslConfigured()) {
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "SSL PORT", "", JettyOrchestrator.DEFAULT_SSL_PORT));
      }

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "HOST", "", host));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CONFIGURATION", "", jettyOrchestrator.getYamlParser().getLoadedConfigYamlPath()));

      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, RESOURCE_STUBDATA_NEW, host, adminPort);
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "NEW STUB DATA POST URI", "", endpointRegistration));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");
      return String.format(systemStatusTable, builder.toString());
   }

   private String buildPageBodyHtml(final String requestCounterHtml, final String tableName, final Map<String, String> stubMemberFields) throws Exception {
      final StringBuilder builder = new StringBuilder();

      final String host = jettyOrchestrator.getCurrentHost();
      final int clientPort = jettyOrchestrator.getCurrentClientPort();

      for (final Map.Entry<String, String> keyValue : stubMemberFields.entrySet()) {
         Object value = keyValue.getValue();
         String responseClass = "";
         if (value != null) {
            value = HandlerUtils.escapeHtmlEntities(value.toString());

            if (keyValue.getKey().equalsIgnoreCase("body")) {
               value = HandlerUtils.highlightResponseMarkup(value);
               responseClass = "response";
            }
         }

         if (keyValue.getKey().equals("url")) {
            value = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, value, host, clientPort);

            if (jettyOrchestrator.isSslConfigured() && tableName.equalsIgnoreCase("request")) {
               final String sslUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, keyValue.getValue(), host, JettyOrchestrator.DEFAULT_SSL_PORT);
               builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "SSL URL", responseClass, sslUrl));
            }
         }
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, keyValue.getKey().toUpperCase(), responseClass, value));
      }
      return String.format(requestCounterHtml, tableName, builder.toString());
   }
}