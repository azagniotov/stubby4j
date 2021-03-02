/*
 * Source: https://raw.githubusercontent.com/sushain97/contestManagement/master/src/util/RequestPrinter.java
 * Note: Slight modifications made to fit custom requirements.
 */

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNotNull;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNull;

@GeneratedCodeCoverageExclusion
final class HttpRequestUtils {

    private static final String INDENT_UNIT = "   ";
    private static final String LEFT_BRACKET = "[";
    private static final String RIGHT_BRACKET = "]";
    private static final String LEFT_CURLY_BRACE = "{";
    private static final String RIGHT_CURLY_BRACE = "}";
    private static final String COMMA = ",";
    private static final String EMPTY_BRACES = LEFT_CURLY_BRACE + RIGHT_CURLY_BRACE;
    private static final int AT_LEAST_ONE_HEADER = 1;

    private HttpRequestUtils() {

    }


    private static String debugStringHeader(final String indentString, final String headerName, final List<String> headerValues) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.
                append(indentString).
                append(INDENT_UNIT).
                append("'").append(headerName).append("': ");
        if (isNull(headerValues) || headerValues.size() == 0) {
            stringBuilder.append("None");
        } else {
            if (headerValues.size() > AT_LEAST_ONE_HEADER) {
                stringBuilder.append(LEFT_BRACKET);
            }
            final String[] headerValuesArray = new String[headerValues.size()];
            stringBuilder.append(StringUtils.join(headerValues.toArray(headerValuesArray), COMMA));
            if (headerValues.size() > AT_LEAST_ONE_HEADER) {
                stringBuilder.append(RIGHT_BRACKET);
            }
        }
        return stringBuilder.toString();
    }


    private static String debugStringHeaders(final HttpServletRequest request, final int indent) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (isNull(headerNames) || !headerNames.hasMoreElements()) {
            return EMPTY_BRACES;
        }
        final String indentString = StringUtils.repeat(INDENT_UNIT, indent);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LEFT_CURLY_BRACE).append(BR);
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final Enumeration<String> headerValues = request.getHeaders(headerName);
            final List<String> headerValuesList = new ArrayList<>();
            while (isNotNull(headerValues) && headerValues.hasMoreElements()) {
                headerValuesList.add(headerValues.nextElement());
            }
            stringBuilder.
                    append(HttpRequestUtils.debugStringHeader(indentString, headerName, headerValuesList)).
                    append(COMMA).append(BR);
        }
        stringBuilder.append(indentString).append(RIGHT_CURLY_BRACE);
        return stringBuilder.toString();
    }

    /**
     * Debug complete request
     *
     * @param request Request parameter.
     * @return A string with debug information on Request's header
     */

    static String dump(final HttpServletRequest request) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(INDENT_UNIT + "PROTOCOL: ").append(request.getProtocol()).append(BR);
        stringBuilder.append(INDENT_UNIT + "METHOD: ").append(request.getMethod()).append(BR);
        stringBuilder.append(INDENT_UNIT + "CONTEXT PATH: ").append(request.getContextPath()).append(BR);
        stringBuilder.append(INDENT_UNIT + "SERVLET PATH: ").append(request.getServletPath()).append(BR);
        stringBuilder.append(INDENT_UNIT + "AUTH TYPE: ").append(request.getAuthType()).append(BR);
        stringBuilder.append(INDENT_UNIT + "REMOTE USER: ").append(request.getRemoteUser()).append(BR);
        stringBuilder.append(INDENT_UNIT + "REQUEST URI: ").append(request.getRequestURI()).append(BR);
        stringBuilder.append(INDENT_UNIT + "REQUEST URL: ").append(request.getRequestURL()).append(BR);
        stringBuilder.append(INDENT_UNIT + "QUERY STRING: ").append(request.getQueryString()).append(BR);
        stringBuilder.append(INDENT_UNIT + "PATH INFO: ").append(request.getPathInfo()).append(BR);
        stringBuilder.append(INDENT_UNIT + "PATH TRANSLATED: ").append(request.getPathTranslated()).append(BR);
        stringBuilder.append(INDENT_UNIT + "HEADERS: ").append(HttpRequestUtils.debugStringHeaders(request, 1));

        return stringBuilder.toString();
    }
}
