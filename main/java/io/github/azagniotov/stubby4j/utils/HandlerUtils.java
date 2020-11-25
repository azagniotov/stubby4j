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

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.common.Common;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static io.github.azagniotov.stubby4j.utils.StringUtils.pluralize;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
@SuppressWarnings("serial")
public final class HandlerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerUtils.class);

    private HandlerUtils() {

    }

    public static void configureErrorResponse(final HttpServletResponse response, final int httpStatus, final String message) throws IOException {
        response.setStatus(httpStatus);
        // Setting custom error message will no longer work in Jetty versions > 9.4.20, see:
        // https://github.com/eclipse/jetty.project/issues/4154
        // response.sendError(httpStatus, message);
        response.sendError(httpStatus);
        response.flushBuffer();

        ANSITerminal.error(message);
        LOGGER.error(message);
    }

    public static String getHtmlResourceByName(final String templateSuffix) throws IOException {
        final String htmlTemplatePath = String.format("/ui/html/%s.html", templateSuffix);
        final InputStream inputStream = HandlerUtils.class.getResourceAsStream(htmlTemplatePath);
        if (ObjectUtils.isNull(inputStream)) {
            throw new IOException(String.format("Could not find resource %s", htmlTemplatePath));
        }
        return StringUtils.inputStreamToString(inputStream);
    }

    @CoberturaIgnore
    public static String constructHeaderServerName() {
        final Package pkg = HandlerUtils.class.getPackage();
        final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
                pkg.getImplementationVersion() : "x.x.xx";

        return String.format("stubby4j/%s (HTTP stub server)", implementationVersion);
    }

    public static void setResponseMainHeaders(final HttpServletResponse response) {
        response.setCharacterEncoding(StringUtils.UTF_8);
        response.setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
        response.setHeader(HttpHeader.DATE.asString(), DateTimeUtils.systemDefault());
        response.setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
        response.setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-stage, must-revalidate"); // HTTP 1.1.
        response.setHeader(HttpHeader.PRAGMA.asString(), "no-cache"); // HTTP 1.0.
        response.setDateHeader(HttpHeader.EXPIRES.asString(), 0);
    }

    public static String linkifyRequestUrl(final String scheme, final Object uri, final String host, final int port) {
        final String fullUrl = String.format("%s://%s:%s%s", scheme.toLowerCase(), host, port, uri);
        final String href = StringUtils.encodeSingleQuotes(fullUrl);
        return String.format("<a target='_blank' href='%s'>%s</a>", href, fullUrl);
    }

    public static String populateHtmlTemplate(final String templateName, final Object... params) throws IOException {
        return String.format(getHtmlResourceByName(templateName), params);
    }

    public static String extractPostRequestBody(final HttpServletRequest request, final String source) throws IOException {
        if (!Common.POSTING_METHODS.contains(request.getMethod().toUpperCase())) {
            return null;
        }

        try {
            final String requestContent = StringUtils.inputStreamToString(request.getInputStream());

            return requestContent.replaceAll("\\\\/", "/"); //https://code.google.com/p/snakeyaml/issues/detail?id=93
        } catch (final Exception ex) {
            final String err = String.format("Error when extracting POST body: %s, returning null..", ex.toString());
            ConsoleUtils.logIncomingRequestError(request, source, err);
            return null;
        }
    }

    public static String calculateStubbyUpTime(final long timestamp) {
        final long days = MILLISECONDS.toDays(timestamp);
        final long hours = MILLISECONDS.toHours(timestamp) - DAYS.toHours(MILLISECONDS.toDays(timestamp));
        final long mins = MILLISECONDS.toMinutes(timestamp) - HOURS.toMinutes(MILLISECONDS.toHours(timestamp));
        final long secs = MILLISECONDS.toSeconds(timestamp) - MINUTES.toSeconds(MILLISECONDS.toMinutes(timestamp));

        return String.format("%d day%s, %d hour%s, %d min%s, %d sec%s",
                days, pluralize(days), hours, pluralize(hours), mins, pluralize(mins), secs, pluralize(secs));
    }
}