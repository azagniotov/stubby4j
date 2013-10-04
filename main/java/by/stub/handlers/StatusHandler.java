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

import by.stub.cli.CommandLineInterpreter;
import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.server.JettyContext;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.JarUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlProperties;
import by.stub.yaml.stubs.StubHttpLifecycle;
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
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class StatusHandler extends AbstractHandler {

   private static final List<String> highlightableProperties = Collections.unmodifiableList(Arrays.asList(YamlProperties.FILE, YamlProperties.BODY, YamlProperties.POST));

   private static final String CSS_CLASS_HIGHLIGHTABLE = "highlightable";
   private static final String CSS_CLASS_NO_HIGHLIGHTABLE = "no-highlightable";
   private static final String FILE_METADATA_ROW_TEMPLATE = "<span style='color: #8B0000'>%s</span>=<span style='color: green'>%s</span>";
   private static final String TABLE_ROW_TEMPLATE = "<tr><td width='250px' valign='top' align='left'>%s</td><td class='%s' align='left'>%s</td></tr>";
   private static final String AJAX_HYPERLINK_TEMPLATE = "&nbsp;<strong><a class='ajaxable' href='/ajax/resource/%s/%s/%s'>[view]</a></strong>&nbsp;";
   private static final String HTML_TABLE_TEMPLATE = HandlerUtils.getHtmlResourceByName("snippet_html_table");

   private final StubbedDataManager stubbedDataManager;
   private final JettyContext jettyContext;
   private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
   private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

   public StatusHandler(final JettyContext newContext, final StubbedDataManager newStubbedDataManager) {
      this.jettyContext = newContext;
      this.stubbedDataManager = newStubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      baseRequest.setHandled(true);
      wrapper.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      wrapper.setStatus(HttpStatus.OK_200);
      wrapper.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      try {
         wrapper.getWriter().println(buildStatusPageHtml());
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper);
   }

   private String buildStatusPageHtml() throws Exception {
      final StringBuilder builder = new StringBuilder();

      builder.append(buildJvmParametersHtmlTable());
      builder.append(buildJettyParametersHtmlTable());
      builder.append(buildStubbyParametersTable());

      final List<StubHttpLifecycle> stubHttpLifecycles = stubbedDataManager.getStubHttpLifecycles();
      for (int cycleIndex = 0; cycleIndex < stubHttpLifecycles.size(); cycleIndex++) {
         final StubHttpLifecycle stubHttpLifecycle = stubHttpLifecycles.get(cycleIndex);
         final String resourceId = stubHttpLifecycle.getResourceId();

         builder.append(buildStubRequestTable(stubHttpLifecycle, resourceId));
         builder.append(buildStubResponseTable(stubHttpLifecycle, resourceId));
         builder.append("<br /><br />");
      }

      return HandlerUtils.populateHtmlTemplate("status", builder.toString());
   }

   private String buildStubRequestTable(final StubHttpLifecycle stubHttpLifecycle, final String resourceId) throws Exception {
      final String ajaxLinkToRequestAsYaml = String.format(AJAX_HYPERLINK_TEMPLATE, resourceId, YamlProperties.HTTPLIFECYCLE, "requestAsYaml");
      final StringBuilder requestTableBuilder = populateBuildWithHtmlBody(resourceId, YamlProperties.REQUEST, ReflectionUtils.getProperties(stubHttpLifecycle.getRequest()));
      requestTableBuilder.append(populateTableRowTemplate("RAW YAML", CSS_CLASS_HIGHLIGHTABLE, ajaxLinkToRequestAsYaml));

      return String.format(HTML_TABLE_TEMPLATE, YamlProperties.REQUEST, requestTableBuilder.toString());
   }

   private String buildStubResponseTable(final StubHttpLifecycle stubHttpLifecycle, final String resourceId) throws Exception {
      final StringBuilder responseTableBuilder = new StringBuilder();
      final List<StubResponse> allResponses = stubHttpLifecycle.getAllResponses();
      for (int sequenceId = 0; sequenceId < allResponses.size(); sequenceId++) {

         final String responseTableTitle = (allResponses.size() == 1 ? YamlProperties.RESPONSE : String.format("%s/%s", YamlProperties.RESPONSE, sequenceId));
         final StubResponse stubResponse = allResponses.get(sequenceId);
         final Map<String, String> stubResponseProperties = ReflectionUtils.getProperties(stubResponse);
         final StringBuilder sequencedResponseBuilder = populateBuildWithHtmlBody(resourceId, responseTableTitle, stubResponseProperties);
         final String ajaxLinkToResponseAsYaml = String.format(AJAX_HYPERLINK_TEMPLATE, resourceId, YamlProperties.HTTPLIFECYCLE, "responseAsYaml");
         sequencedResponseBuilder.append(populateTableRowTemplate("RAW YAML", CSS_CLASS_HIGHLIGHTABLE, ajaxLinkToResponseAsYaml));

         responseTableBuilder.append(String.format(HTML_TABLE_TEMPLATE, responseTableTitle, sequencedResponseBuilder.toString()));
      }

      return responseTableBuilder.toString();
   }

   private String buildJvmParametersHtmlTable() throws Exception {

      final StringBuilder builder = new StringBuilder();
      if (!RUNTIME_MX_BEAN.getInputArguments().isEmpty()) {
         builder.append(populateTableRowTemplate("INPUT ARGS", CSS_CLASS_NO_HIGHLIGHTABLE, RUNTIME_MX_BEAN.getInputArguments()));
      }
      builder.append(populateTableRowTemplate("HEAP MEMORY USAGE", CSS_CLASS_NO_HIGHLIGHTABLE, MEMORY_MX_BEAN.getHeapMemoryUsage()));
      builder.append(populateTableRowTemplate("NON-HEAP MEMORY USAGE", CSS_CLASS_NO_HIGHLIGHTABLE, MEMORY_MX_BEAN.getNonHeapMemoryUsage()));

      return String.format(HTML_TABLE_TEMPLATE, "jvm", builder.toString());
   }

   private String buildJettyParametersHtmlTable() throws Exception {

      final StringBuilder builder = new StringBuilder();
      final String host = jettyContext.getHost();
      final int adminPort = jettyContext.getAdminPort();
      builder.append(populateTableRowTemplate("HOST", CSS_CLASS_NO_HIGHLIGHTABLE, host));
      builder.append(populateTableRowTemplate("ADMIN PORT", CSS_CLASS_NO_HIGHLIGHTABLE, adminPort));
      builder.append(populateTableRowTemplate("STUBS PORT", CSS_CLASS_NO_HIGHLIGHTABLE, jettyContext.getStubsPort()));
      builder.append(populateTableRowTemplate("STUBS TLS PORT", CSS_CLASS_NO_HIGHLIGHTABLE, jettyContext.getStubsTlsPort()));
      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, AdminHandler.ADMIN_ROOT, host, adminPort);
      builder.append(populateTableRowTemplate("NEW STUB DATA POST URI", CSS_CLASS_NO_HIGHLIGHTABLE, endpointRegistration));

      return String.format(HTML_TABLE_TEMPLATE, "jetty parameters", builder.toString());
   }

   private String buildStubbyParametersTable() throws Exception {

      final StringBuilder builder = new StringBuilder();
      builder.append(populateTableRowTemplate("VERSION", CSS_CLASS_NO_HIGHLIGHTABLE, JarUtils.readManifestImplementationVersion()));
      builder.append(populateTableRowTemplate("RUNTIME CLASSPATH", CSS_CLASS_NO_HIGHLIGHTABLE, RUNTIME_MX_BEAN.getClassPath()));
      builder.append(populateTableRowTemplate("LOCAL BUILT DATE", CSS_CLASS_NO_HIGHLIGHTABLE, JarUtils.readManifestBuiltDate()));
      builder.append(populateTableRowTemplate("UPTIME", CSS_CLASS_NO_HIGHLIGHTABLE, HandlerUtils.calculateStubbyUpTime(RUNTIME_MX_BEAN.getUptime())));
      builder.append(populateTableRowTemplate("INPUT ARGS", CSS_CLASS_NO_HIGHLIGHTABLE, CommandLineInterpreter.PROVIDED_OPTIONS));
      builder.append(populateTableRowTemplate("STUBBED ENDPOINTS", CSS_CLASS_NO_HIGHLIGHTABLE, stubbedDataManager.getStubHttpLifecycles().size()));
      builder.append(populateTableRowTemplate("LOADED YAML", CSS_CLASS_NO_HIGHLIGHTABLE, generateFileMetadata(stubbedDataManager.getDataYaml())));

      if (!stubbedDataManager.getExternalFiles().isEmpty()) {
         final StringBuilder externalFilesMetadata = new StringBuilder();
         for (Map.Entry<File, Long> entry : stubbedDataManager.getExternalFiles().entrySet()) {
            final File externalFile = entry.getKey();
            externalFilesMetadata.append(generateFileMetadata(externalFile));
         }
         builder.append(populateTableRowTemplate("LOADED EXTERNAL FILES", CSS_CLASS_NO_HIGHLIGHTABLE, externalFilesMetadata.toString()));
      }

      return String.format(HTML_TABLE_TEMPLATE, "stubby4j parameters", builder.toString());
   }

   private String generateFileMetadata(final File file) throws IOException {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(FILE_METADATA_ROW_TEMPLATE, "parentDir", file.getParentFile().getCanonicalPath() + "/")).append("<br />");
      builder.append(String.format(FILE_METADATA_ROW_TEMPLATE, "name", file.getName())).append("<br />");
      builder.append(String.format(FILE_METADATA_ROW_TEMPLATE, "size", String.format("%1$,.2f", ((double) file.length() / 1024)) + "kb")).append("<br />");
      builder.append(String.format(FILE_METADATA_ROW_TEMPLATE, "lastModified", new Date(file.lastModified()))).append("<br />");

      return "<div style='margin-top: 5px; padding: 3px 7px 3px 7px; background-color: #fefefe'>" + builder.toString() + "</div>";
   }

   private StringBuilder populateBuildWithHtmlBody(final String resourceId, final String stubTypeName, final Map<String, String> stubObjectProperties) throws Exception {
      final StringBuilder builder = new StringBuilder();

      for (final Map.Entry<String, String> keyValue : stubObjectProperties.entrySet()) {
         final String value = keyValue.getValue();
         final String key = keyValue.getKey();

         if (!StringUtils.isSet(value)) {
            continue;
         }

         builder.append(constructTableRow(resourceId, stubTypeName, key, value));
      }
      return builder;
   }

   private String constructTableRow(final String resourceId, final String stubTypeName, final String fieldName, final String value) {

      if (highlightableProperties.contains(fieldName)) {
         final String ajaxHyperlink = String.format(AJAX_HYPERLINK_TEMPLATE, resourceId, stubTypeName, fieldName);
         return populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_HIGHLIGHTABLE, ajaxHyperlink);
      }

      final String escapedValue = StringUtils.escapeHtmlEntities(value);
      if (fieldName.equals(YamlProperties.URL)) {
         final String urlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, escapedValue, jettyContext.getHost(), jettyContext.getStubsPort());
         final String tlsUrlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, escapedValue, jettyContext.getHost(), jettyContext.getStubsTlsPort());

         final String tableRowWithUrl = populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, urlAsHyperlink);
         final String tableRowWithTlsUrl = populateTableRowTemplate("TLS " + StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, tlsUrlAsHyperlink);

         return String.format("%s%s", tableRowWithUrl, tableRowWithTlsUrl);
      }

      return populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, escapedValue);
   }

   private String populateTableRowTemplate(final Object... tokens) {
      return String.format(TABLE_ROW_TEMPLATE, tokens);
   }
}