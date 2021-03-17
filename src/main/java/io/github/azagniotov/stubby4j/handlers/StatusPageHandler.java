package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.server.JettyContext;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.DateTimeUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.JarUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.azagniotov.stubby4j.utils.HandlerUtils.getHtmlResourceByName;
import static io.github.azagniotov.stubby4j.utils.StringUtils.toUpper;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.POST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.PROXY_CONFIG;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.REQUEST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.RESPONSE;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@GeneratedCodeCoverageExclusion
public final class StatusPageHandler extends AbstractHandler implements AbstractHandlerExtension {

    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    private static final List<ConfigurableYAMLProperty> FIELDS_FOR_AJAX_LINKS = unmodifiableList(asList(FILE, BODY, POST));

    private static final String TEMPLATE_LOADED_FILE_METADATA_PAIR = "<span style='color: #8B0000'>%s</span>=<span style='color: green'>%s</span>";
    private static final String TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK = "<strong><a class='ajax-resource' href='/ajax/resource/%s/%s/%s'>[view]</a></strong>";
    private static final String TEMPLATE_AJAX_TO_STATS_HYPERLINK = "<strong><a class='ajax-stats' href='/ajax/stats'>[view]</a></strong>";
    private static final String TEMPLATE_HTML_TABLE_ROW = "<tr><td width='250px' valign='top' align='left'>%s</td><td align='left'>%s</td></tr>";
    private static final String NEXT_IN_THE_QUEUE = " NEXT IN THE QUEUE";
    private static final String HTML_BR = "<br />";

    private final StubRepository stubRepository;
    private final JettyContext jettyContext;

    public StatusPageHandler(final JettyContext jettyContext, final StubRepository stubRepository) {
        this.jettyContext = jettyContext;
        this.stubRepository = stubRepository;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        if (logAndCheckIsHandled("status", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        response.setHeader(HttpHeader.SERVER.asString().toLowerCase(Locale.US), HandlerUtils.constructHeaderServerName());

        try {
            response.getWriter().println(buildStatusPageHtml());
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }

    private String buildStatusPageHtml() throws Exception {
        final StringBuilder builder = new StringBuilder();

        final String templateHtmlTable = getHtmlResourceByName("_table");

        builder.append(buildJvmParametersHtmlTable(templateHtmlTable));
        builder.append(buildJettyParametersHtmlTable(templateHtmlTable));
        builder.append(buildStubbyParametersHtmlTable(templateHtmlTable));
        builder.append(buildEndpointStatsHtmlTable(templateHtmlTable));

        final Map<String, StubProxyConfig> proxyConfigs = stubRepository.getProxyConfigs();
        if (!proxyConfigs.isEmpty()) {
            builder.append(HTML_BR);
            final String proxyConfigDefaultName = StubProxyConfig.Builder.DEFAULT_UUID;
            builder.append(buildStubProxyConfigtHtmlTable(proxyConfigs.get(proxyConfigDefaultName), templateHtmlTable));
            for (Map.Entry<String, StubProxyConfig> entry : proxyConfigs.entrySet()) {
                if (entry.getKey().equals(proxyConfigDefaultName)) {
                    continue;
                }
                builder.append(buildStubProxyConfigtHtmlTable(entry.getValue(), templateHtmlTable));
            }
            builder.append(HTML_BR).append(HTML_BR);
        }

        final List<StubHttpLifecycle> stubHttpLifecycles = stubRepository.getStubs();
        for (final StubHttpLifecycle stubHttpLifecycle : stubHttpLifecycles) {
            builder.append(buildStubRequestHtmlTable(stubHttpLifecycle, templateHtmlTable));
            builder.append(buildStubResponseHtmlTable(stubHttpLifecycle, templateHtmlTable));
            builder.append(HTML_BR).append(HTML_BR);
        }

        final long timestamp = System.currentTimeMillis();
        return HandlerUtils.populateHtmlTemplate("status", timestamp, timestamp, builder.toString());
    }

    private String buildStubProxyConfigtHtmlTable(final StubProxyConfig stubProxyConfig, final String templateHtmlTable) throws Exception {
        final String proxyUuid = stubProxyConfig.getUUID();
        final String ajaxLinkToRequestAsYaml = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, PROXY_CONFIG, proxyUuid, "proxyConfigAsYAML");
        final StringBuilder proxyConfigTableBuilder = buildStubHtmlTableBody(proxyUuid, PROXY_CONFIG.toString(), ReflectionUtils.getProperties(stubProxyConfig));
        proxyConfigTableBuilder.append(interpolateHtmlTableRowTemplate("RAW YAML", ajaxLinkToRequestAsYaml));

        return String.format(templateHtmlTable, PROXY_CONFIG, proxyConfigTableBuilder.toString());
    }

    private String buildStubRequestHtmlTable(final StubHttpLifecycle stubHttpLifecycle, final String templateHtmlTable) throws Exception {
        final String resourceId = stubHttpLifecycle.getResourceId();
        final String ajaxLinkToRequestAsYaml = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, ConfigurableYAMLProperty.HTTPLIFECYCLE, "requestAsYAML");
        final StringBuilder requestTableBuilder = buildStubHtmlTableBody(resourceId, REQUEST.toString(), ReflectionUtils.getProperties(stubHttpLifecycle.getRequest()));
        requestTableBuilder.append(interpolateHtmlTableRowTemplate("RAW YAML", ajaxLinkToRequestAsYaml));

        return String.format(templateHtmlTable, REQUEST, requestTableBuilder.toString());
    }

    private String buildStubResponseHtmlTable(final StubHttpLifecycle stubHttpLifecycle, final String templateHtmlTable) throws Exception {
        final String resourceId = stubHttpLifecycle.getResourceId();
        final StringBuilder responseTableBuilder = new StringBuilder();
        final List<StubResponse> allResponses = stubHttpLifecycle.getResponses();
        for (int sequenceId = 0; sequenceId < allResponses.size(); sequenceId++) {

            final boolean isResponsesSequenced = allResponses.size() != 1;
            final int nextSequencedResponseId = stubHttpLifecycle.getNextSequencedResponseId();
            final String nextResponseLabel = (isResponsesSequenced && nextSequencedResponseId == sequenceId ? NEXT_IN_THE_QUEUE : "");
            final String responseTableTitle = (isResponsesSequenced ? String.format("%s/%s%s", RESPONSE, sequenceId, nextResponseLabel) : RESPONSE.toString());
            final StubResponse stubResponse = allResponses.get(sequenceId);
            final Map<String, String> stubResponseProperties = ReflectionUtils.getProperties(stubResponse);
            final StringBuilder sequencedResponseBuilder = buildStubHtmlTableBody(resourceId, responseTableTitle, stubResponseProperties);
            final String ajaxLinkToResponseAsYaml = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, ConfigurableYAMLProperty.HTTPLIFECYCLE, "responseAsYAML");
            sequencedResponseBuilder.append(interpolateHtmlTableRowTemplate("RAW YAML", ajaxLinkToResponseAsYaml));

            responseTableBuilder.append(String.format(templateHtmlTable, responseTableTitle, sequencedResponseBuilder.toString()));
        }

        return responseTableBuilder.toString();
    }

    private String buildJvmParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        if (!RUNTIME_MX_BEAN.getInputArguments().isEmpty()) {
            builder.append(interpolateHtmlTableRowTemplate("INPUT ARGS", RUNTIME_MX_BEAN.getInputArguments()));
        }
        builder.append(interpolateHtmlTableRowTemplate("HEAP MEMORY USAGE", MEMORY_MX_BEAN.getHeapMemoryUsage()));
        builder.append(interpolateHtmlTableRowTemplate("NON-HEAP MEMORY USAGE", MEMORY_MX_BEAN.getNonHeapMemoryUsage()));

        return String.format(templateHtmlTable, "jvm", builder.toString());
    }

    private String buildJettyParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        final String host = jettyContext.getHost();
        final int adminPort = jettyContext.getAdminPort();
        builder.append(interpolateHtmlTableRowTemplate("HOST", host));
        builder.append(interpolateHtmlTableRowTemplate("ADMIN PORT", adminPort));
        builder.append(interpolateHtmlTableRowTemplate("STUBS PORT", jettyContext.getStubsPort()));
        builder.append(interpolateHtmlTableRowTemplate("STUBS TLS PORT", jettyContext.getStubsTlsPort()));
        final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), AdminPortalHandler.ADMIN_ROOT, host, adminPort);
        builder.append(interpolateHtmlTableRowTemplate("NEW STUB DATA POST URI", endpointRegistration));

        return String.format(templateHtmlTable, "jetty parameters", builder.toString());
    }

    private String buildStubbyParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        builder.append(interpolateHtmlTableRowTemplate("VERSION", JarUtils.readManifestImplementationVersion()));
        builder.append(interpolateHtmlTableRowTemplate("RUNTIME CLASSPATH", RUNTIME_MX_BEAN.getClassPath()));
        builder.append(interpolateHtmlTableRowTemplate("LOCAL BUILT DATE", JarUtils.readManifestBuiltDate()));
        builder.append(interpolateHtmlTableRowTemplate("UPTIME", HandlerUtils.calculateStubbyUpTime(RUNTIME_MX_BEAN.getUptime())));
        builder.append(interpolateHtmlTableRowTemplate("INPUT ARGS", CommandLineInterpreter.PROVIDED_OPTIONS));
        builder.append(interpolateHtmlTableRowTemplate("STUBBED ENDPOINTS", stubRepository.getStubs().size()));
        builder.append(interpolateHtmlTableRowTemplate("LOADED YAML", buildLoadedFileMetadata(stubRepository.getYamlConfig())));

        if (!stubRepository.getExternalFiles().isEmpty()) {
            final StringBuilder externalFilesMetadata = new StringBuilder();
            for (Map.Entry<File, Long> entry : stubRepository.getExternalFiles().entrySet()) {
                final File externalFile = entry.getKey();
                externalFilesMetadata.append(buildLoadedFileMetadata(externalFile));
            }
            builder.append(interpolateHtmlTableRowTemplate("LOADED EXTERNAL FILES", externalFilesMetadata.toString()));
        }

        return String.format(templateHtmlTable, "stubby4j parameters", builder.toString());
    }

    private String buildEndpointStatsHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        if (stubRepository.getResourceStats().isEmpty()) {
            builder.append(interpolateHtmlTableRowTemplate("ENDPOINT HITS", "No requests were made to stubby yet"));
        } else {
            builder.append(interpolateHtmlTableRowTemplate("ENDPOINT HITS", TEMPLATE_AJAX_TO_STATS_HYPERLINK));
        }

        return String.format(templateHtmlTable, "stubby stats", builder.toString());
    }

    private String buildLoadedFileMetadata(final File file) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "parentDir", determineParentDir(file))).append(HTML_BR);
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "name", file.getName())).append(HTML_BR);
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "size", String.format("%1$,.2f", ((double) file.length() / 1024)) + "kb")).append(HTML_BR);
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "lastModified", DateTimeUtils.systemDefault(file.lastModified()))).append(HTML_BR);

        return "<div style='margin-top: 5px; padding: 3px 7px 3px 7px; background-color: #fefefe'>" + builder.toString() + "</div>";
    }

    private String determineParentDir(final File file) throws IOException {
        return (ObjectUtils.isNull(file.getParentFile()) ? file.getCanonicalPath().replaceAll(file.getName(), "") : file.getParentFile().getCanonicalPath() + "/");
    }

    private StringBuilder buildStubHtmlTableBody(final String resourceId, final String stubTypeName, final Map<String, String> stubObjectProperties) throws Exception {
        final StringBuilder builder = new StringBuilder();

        for (final Map.Entry<String, String> keyValue : stubObjectProperties.entrySet()) {
            final String value = keyValue.getValue();
            final String key = keyValue.getKey();

            if (!StringUtils.isSet(value)) {
                continue;
            }

            builder.append(buildHtmlTableSingleRow(resourceId, stubTypeName, key, value));
        }
        return builder;
    }

    private String buildHtmlTableSingleRow(final String resourceId, final String stubTypeName, final String fieldName, final String value) {

        if (!ConfigurableYAMLProperty.isUnknownProperty(fieldName) && FIELDS_FOR_AJAX_LINKS.contains(ConfigurableYAMLProperty.valueOf(toUpper(fieldName)))) {
            final String cleansedStubTypeName = stubTypeName.replaceAll(NEXT_IN_THE_QUEUE, "");   //Only when there are sequenced responses
            final String ajaxHyperlink = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, cleansedStubTypeName, fieldName);
            return interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), ajaxHyperlink);
        }

        final String escapedValue = StringUtils.escapeHtmlEntities(value);
        if (fieldName.equals(ConfigurableYAMLProperty.URL.toString())) {
            final String urlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), escapedValue, jettyContext.getHost(), jettyContext.getStubsPort());
            final String tlsUrlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTPS.asString(), escapedValue, jettyContext.getHost(), jettyContext.getStubsTlsPort());

            final String tableRowWithUrl = interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), urlAsHyperlink);
            final String tableRowWithTlsUrl = interpolateHtmlTableRowTemplate("TLS " + StringUtils.toUpper(fieldName), tlsUrlAsHyperlink);

            return String.format("%s%s", tableRowWithUrl, tableRowWithTlsUrl);
        }

        return interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), escapedValue);
    }

    private String interpolateHtmlTableRowTemplate(final Object... tokens) {
        return String.format(TEMPLATE_HTML_TABLE_ROW, tokens);
    }
}
