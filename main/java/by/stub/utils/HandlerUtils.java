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

package by.stub.utils;

import by.stub.exception.Stubby4JException;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
@SuppressWarnings("serial")
public final class HandlerUtils {

   private HandlerUtils() {

   }

   public static void configureErrorResponse(final HttpServletResponse response, final int httpStatus, final String message) throws IOException {
      response.setStatus(httpStatus);
      response.sendError(httpStatus, message);
      response.flushBuffer();
   }

   public static String getHtmlResourceByName(final String templateSuffix) {
      final String htmlTemplatePath = String.format("/ui/html/templates/%s.html", templateSuffix);
      final InputStream inputStream = HandlerUtils.class.getResourceAsStream(htmlTemplatePath);
      if (inputStream == null) {
         throw new Stubby4JException(String.format("Could not find resource %s", htmlTemplatePath));
      }
      return StringUtils.inputStreamToString(inputStream);
   }

   public static String constructHeaderServerName() {
      final Package pkg = HandlerUtils.class.getPackage();
      final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
         pkg.getImplementationVersion() : "x.x.xx";

      return String.format("stubby4j/%s (HTTP stub server)", implementationVersion);
   }

   public static void setResponseMainHeaders(final HttpServletResponse response) {
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      response.setHeader(HttpHeaders.DATE, new Date().toString());
      response.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT_HTML_UTF_8);
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"); // HTTP 1.1.
      response.setHeader(HttpHeaders.PRAGMA, "no-cache"); // HTTP 1.0.
      response.setDateHeader(HttpHeaders.EXPIRES, 0);
   }

   public static String linkifyRequestUrl(final String scheme, final Object uri, final String host, final int port) {
      final String fullUrl = String.format("%s://%s:%s%s", scheme, host, port, uri);
      final String href = StringUtils.encodeSingleQuotes(fullUrl);
      return String.format("<a target='_blank' href='%s'>%s</a>", href, fullUrl);
   }

   public static String populateHtmlTemplate(final String templateName, final Object... params) {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(getHtmlResourceByName(templateName), params));
      return builder.toString();
   }

   public static String extractPostRequestBody(final HttpServletRequest request, final String source) throws IOException {
      final Set<String> httpMethodsContainingBody = new HashSet<String>() {{
         add("post");
         add("put");
      }};
      if (!httpMethodsContainingBody.contains(request.getMethod().toLowerCase())) return null;

      try {
         return StringUtils.inputStreamToString(request.getInputStream());
      } catch (final Exception ex) {
         final String err = String.format("Error when extracting POST body: %s, returning null..", ex.toString());
         ConsoleUtils.logIncomingRequestError(request, source, err);
         return null;
      }
   }
}