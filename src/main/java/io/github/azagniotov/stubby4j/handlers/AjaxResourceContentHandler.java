package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubTypes;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.azagniotov.stubby4j.utils.HandlerUtils.getHtmlResourceByName;

public class AjaxResourceContentHandler extends AbstractHandler implements AbstractHandlerExtension {

    private static final Pattern REGEX_REQUEST = Pattern.compile("^(request)$");
    private static final Pattern REGEX_RESPONSE = Pattern.compile("^(response)$");
    private static final Pattern REGEX_PROXY_CONFIG = Pattern.compile("^(proxy-config)$");
    private static final Pattern REGEX_HTTPLIFECYCLE = Pattern.compile("^(httplifecycle)$");
    private static final Pattern REGEX_NUMERIC = Pattern.compile("^[0-9]+$");

    private final StubRepository stubRepository;

    public AjaxResourceContentHandler(final StubRepository stubRepository) {
        this.stubRepository = stubRepository;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        if (logAndCheckIsHandled("ajaxResource", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);

        HandlerUtils.setResponseMainHeaders(response);
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);

        // e.g.: http://localhost:8889/ajax/resource/proxy-config/some-unique-name/proxyConfigAsYAML => /proxy-config/some-unique-name/proxyConfigAsYAML
        // e.g.: http://localhost:8889/ajax/resource/32/httplifecycle/responseAsYAML => /32/httplifecycle/responseAsYAML
        // e.g.: http://localhost:8889/ajax/resource/0/response/body => /0/response/body
        final String[] uriFragments = request.getRequestURI().split("/");
        final int urlFragmentsLength = uriFragments.length;

        // e.g.: 'body' or 'responseAsYAML'
        final String targetFieldName = uriFragments[urlFragmentsLength - 1];

        // e.g.: 'request', 'httplifecycle' or unique name of a proxy-config
        final String stubType = uriFragments[urlFragmentsLength - 2];

        // e.g.: sequenced responses
        if (REGEX_NUMERIC.matcher(stubType).matches()) {
            final int sequencedResponseId = Integer.parseInt(stubType);
            final int resourceIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 4]);
            final StubHttpLifecycle foundStub = throwErrorOnNonExistentResourceIndex(response, resourceIndex);
            renderAjaxResponseContent(response, sequencedResponseId, targetFieldName, foundStub);
        } else {

            // e.g.: /32/httplifecycle/responseAsYAML , the '32'
            final String resourceIndexAsString = uriFragments[urlFragmentsLength - 3];
            if (REGEX_NUMERIC.matcher(resourceIndexAsString).matches()) {
                final int resourceIndex = Integer.parseInt(resourceIndexAsString);
                final StubHttpLifecycle foundStub = throwErrorOnNonExistentResourceIndex(response, resourceIndex);
                if (REGEX_REQUEST.matcher(stubType).matches()) {
                    renderAjaxResponseContent(response, StubTypes.REQUEST, targetFieldName, foundStub);
                } else if (REGEX_RESPONSE.matcher(stubType).matches()) {
                    renderAjaxResponseContent(response, StubTypes.RESPONSE, targetFieldName, foundStub);
                } else if (REGEX_HTTPLIFECYCLE.matcher(stubType).matches()) {
                    renderAjaxResponseContent(response, StubTypes.HTTPLIFECYCLE, targetFieldName, foundStub);
                } else {
                    response.getWriter().println(String.format("Could not fetch the content for stub type: %s", stubType));
                }
            }
            // e.g.: /proxy-config/some-unique-name/proxyConfigAsYAML , the 'proxy-config'
            else if (REGEX_PROXY_CONFIG.matcher(resourceIndexAsString).matches()) {

                // the 'stubType' is actually proxy config unique name here
                final StubProxyConfig foundProxyConfig = stubRepository.matchProxyConfigByName(stubType);
                renderProxyConfigAjaxResponse(response, targetFieldName, foundProxyConfig);
            } else {
                response.getWriter().println(String.format("Could not fetch the content for proxy config: %s", resourceIndexAsString));
            }

        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }

    @VisibleForTesting
    void renderProxyConfigAjaxResponse(final HttpServletResponse response, final String targetFieldName, final StubProxyConfig foundProxyConfig) throws IOException {
        try {
            final String ajaxResponse = StringUtils.objectToString(ReflectionUtils.getPropertyValue(foundProxyConfig, targetFieldName));
            final String popupHtmlTemplate = getHtmlResourceByName("_popup_proxy_config");
            final String htmlPopup = String.format(popupHtmlTemplate, foundProxyConfig.getUUID(), ajaxResponse);
            response.getWriter().println(htmlPopup);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    @VisibleForTesting
    void renderAjaxResponseContent(final HttpServletResponse response, final StubTypes stubType, final String targetFieldName, final StubHttpLifecycle foundStub) throws IOException {
        try {
            final String ajaxResponse = foundStub.getAjaxResponseContent(stubType, targetFieldName);
            final String popupHtmlTemplate = getHtmlResourceByName("_popup_generic");
            final String htmlPopup = String.format(popupHtmlTemplate, foundStub.getResourceId(), foundStub.getUUID(), ajaxResponse);
            response.getWriter().println(htmlPopup);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    @VisibleForTesting
    void renderAjaxResponseContent(final HttpServletResponse response, final int sequencedResponseId, final String targetFieldName, final StubHttpLifecycle foundStub) throws IOException {
        try {
            final String ajaxResponse = foundStub.getAjaxResponseContent(targetFieldName, sequencedResponseId);
            final String popupHtmlTemplate = getHtmlResourceByName("_popup_generic");
            final String htmlPopup = String.format(popupHtmlTemplate, foundStub.getResourceId(), foundStub.getUUID(), ajaxResponse);
            response.getWriter().println(htmlPopup);
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
    }

    @VisibleForTesting
    StubHttpLifecycle throwErrorOnNonExistentResourceIndex(final HttpServletResponse response, final int resourceIndex) throws IOException {
        final Optional<StubHttpLifecycle> foundStubOptional = stubRepository.matchStubByIndex(resourceIndex);
        if (!foundStubOptional.isPresent()) {
            final String error = "Resource does not exist for ID: " + resourceIndex;
            response.getWriter().println(error);
            throw new IOException(error);
        }
        return foundStubOptional.get();
    }
}
