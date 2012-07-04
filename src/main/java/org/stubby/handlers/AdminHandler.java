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
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.stubby.database.DataStore;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/17/12, 11:25 PM
 */
public final class AdminHandler extends AbstractHandler {

   public static final String RESOURCE_PING = "/ping";
   public static final String RESOURCE_ENDPOINT_NEW = "/endpoint/new";

   private static final String HTML_TAG_TR_PARAMETIZED_TEMPLATE = "<tr><td width='120px' valign='top' align='left'><code>%s</code></td><td align='left'>%s</td></tr>";

   private final DataStore dataStore;
   private final JettyOrchestrator jettyOrchestrator;

   public AdminHandler(final DataStore dataStore, final JettyOrchestrator jettyOrchestrator) {
      this.dataStore = dataStore;
      this.jettyOrchestrator = jettyOrchestrator;
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
      } else if (request.getPathInfo().equals(AdminHandler.RESOURCE_ENDPOINT_NEW)) {
         handlePostOnRegisteringNewEndpoint(request, response);
         return;
      }

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", request.getContextPath());
      response.getWriter().println(adminHandlerHtml);
   }

   private void handlePostOnRegisteringNewEndpoint(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
      if (request.getMethod().equalsIgnoreCase("get")) {
         response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
         response.sendError(HttpStatus.METHOD_NOT_ALLOWED_405, String.format("Method GET is not allowed on URI %s", request.getPathInfo()));
         return;
      }

      final StubHttpLifecycle registered = addHttpCycleToDataStore(request);
      if (!registered.isComplete()) {
         response.setStatus(HttpStatus.BAD_REQUEST_400);
         response.sendError(HttpStatus.BAD_REQUEST_400, String.format("Endpoint content provided is not complete, was given %s", registered));
         return;
      }

      final boolean isAdded = !dataStore.getStubHttpLifecycles().contains(registered)
            && dataStore.getStubHttpLifecycles().add(registered);
      if (!isAdded) {
         response.setStatus(HttpStatus.CONFLICT_409);
         response.sendError(HttpStatus.CONFLICT_409, String.format("Endpoint already exists for provided parameters, was given %s", registered));
         return;
      }

      response.setStatus(HttpStatus.CREATED_201);
      response.getWriter().println(String.format("Created endpoint %s", registered));
   }

   private StubHttpLifecycle addHttpCycleToDataStore(final HttpServletRequest request) {

      final StubRequest registeredStubRequest = new StubRequest();
      registeredStubRequest.setMethod(request.getParameter("method"));
      registeredStubRequest.setUrl(request.getParameter("url"));
      registeredStubRequest.setPostBody(request.getParameter("postBody"));

      final StubResponse registeredStubResponse = new StubResponse();
      registeredStubResponse.setBody(request.getParameter("body"));
      registeredStubResponse.setStatus(request.getParameter("status"));

      final Map<String, String> headers = new HashMap<String, String>();
      for (final String header : request.getParameter("responseHeaders").split(",")) {
         final String[] keyAndValueHeader = header.split("=");
         if (keyAndValueHeader.length != 2) {
            continue;
         }
         headers.put(keyAndValueHeader[0], keyAndValueHeader[1]);
      }
      registeredStubResponse.setHeaders(headers);
      return new StubHttpLifecycle(registeredStubRequest, registeredStubResponse);
   }

   private void handleGetOnPing(final HttpServletResponse response) throws IOException {
      try {
         response.getWriter().println(getConfigDataPresentation());
      } catch (IllegalAccessException e) {
         response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
         response.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getMessage());
      }
   }

   private String getConfigDataPresentation() throws IllegalAccessException {

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

   private String buildSystemStatusHtmlTable() {

      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CLIENT PORT", jettyOrchestrator.getCurrentClientPort()));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "ADMIN PORT", jettyOrchestrator.getCurrentAdminPort()));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "HOST", jettyOrchestrator.getCurrentHost()));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CONFIGURATION", YamlConsumer.LOADED_CONFIG));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");
      return String.format(systemStatusTable, builder.toString());
   }

   private String buildPageBodyHtml(final String requestCounterHtml, final String tableName, final Map<String, String> stubMemberFields) {
      final StringBuilder builder = new StringBuilder();

      for (final Map.Entry<String, String> keyValue : stubMemberFields.entrySet()) {

         Object value = keyValue.getValue();
         if (keyValue.getKey().equals("url")) {
            value = HandlerUtils.linkifyRequestUrl(stubMemberFields.get(keyValue.getKey()),
                  jettyOrchestrator.getCurrentHost(), jettyOrchestrator.getCurrentClientPort());
         } else if (value != null) {
            value = HandlerUtils.escapeHtmlEntities(value.toString());
         }

         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, keyValue.getKey().toUpperCase(), value));
      }
      return String.format(requestCounterHtml, tableName, builder.toString());
   }
}