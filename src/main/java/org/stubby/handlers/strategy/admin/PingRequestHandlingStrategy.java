package org.stubby.handlers.strategy.admin;

import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.stubby.database.DataStore;
import org.stubby.handlers.AdminEndpoints;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;
import org.stubby.utils.ReflectionUtils;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 10/7/12, 2:18 PM
 */
public class PingRequestHandlingStrategy  implements AdminRequestHandlingStrategy {

   private static final String HTML_TAG_TR_PARAMETIZED_TEMPLATE = "<tr><td width='200px' valign='top' align='left'><code>%s</code></td><td class='%s' align='left'>%s</td></tr>";

   private final DataStore dataStore;
   private final JettyOrchestrator jettyOrchestrator;

   public PingRequestHandlingStrategy(final JettyOrchestrator jettyOrchestrator) {
      this.jettyOrchestrator = jettyOrchestrator;
      this.dataStore = jettyOrchestrator.getDataStore();
   }

   @Override
   public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

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
      final int clientPort = jettyOrchestrator.getCurrentStubsPort();
      final int adminPort = jettyOrchestrator.getCurrentAdminPort();

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CLIENT PORT", "", clientPort));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "ADMIN PORT", "", adminPort));

      if (jettyOrchestrator.isSslConfigured()) {
         builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "SSL PORT", "", JettyOrchestrator.DEFAULT_SSL_PORT));
      }

      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "HOST", "", host));
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "CONFIGURATION", "", jettyOrchestrator.getYamlParser().getLoadedConfigYamlPath()));

      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, AdminEndpoints.STUBDATA_NEW.desc(), host, adminPort);
      builder.append(String.format(HTML_TAG_TR_PARAMETIZED_TEMPLATE, "NEW STUB DATA POST URI", "", endpointRegistration));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");
      return String.format(systemStatusTable, builder.toString());
   }

   private String buildPageBodyHtml(final String requestCounterHtml, final String tableName, final Map<String, String> stubMemberFields) throws Exception {
      final StringBuilder builder = new StringBuilder();

      final String host = jettyOrchestrator.getCurrentHost();
      final int clientPort = jettyOrchestrator.getCurrentStubsPort();

      for (final Map.Entry<String, String> keyValue : stubMemberFields.entrySet()) {
         Object value = keyValue.getValue();
         String responseClass = "";
         if (value != null) {
            value = HandlerUtils.escapeHtmlEntities(value.toString());

            if (keyValue.getKey().equalsIgnoreCase("body") || keyValue.getKey().equalsIgnoreCase("file")) {
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
