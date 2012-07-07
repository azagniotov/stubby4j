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

package org.stubby.utils;

import org.stubby.exception.Stubby4JException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
public final class HandlerUtils {

   private HandlerUtils() {

   }

   public static void configureErrorResponse(final HttpServletResponse response, final int httpStatus, final String message) throws IOException {
      response.setStatus(httpStatus);
      response.sendError(httpStatus, message);
   }

   public static String getHtmlResourceByName(final String templateSuffix) {
      final String htmlTemplatePath = String.format("/html/%s.html", templateSuffix);
      final InputStream postBodyInputStream = HandlerUtils.class.getResourceAsStream(htmlTemplatePath);
      if (postBodyInputStream == null) {
         throw new Stubby4JException(String.format("Could not find resource %s", htmlTemplatePath));
      }
      return inputStreamToString(postBodyInputStream);
   }

   public static String constructHeaderServerName() {
      final Package pkg = HandlerUtils.class.getPackage();
      final String implementationVersion = pkg.getImplementationVersion() == null ?
            "x.x.x" : pkg.getImplementationVersion();
      final String implementationTitle = pkg.getImplementationTitle() == null ?
            "Java-based HTTP stub server" : pkg.getImplementationTitle();
      return String.format("stubby4j/%s (%s)", implementationVersion, implementationTitle);
   }

   public static String inputStreamToString(final InputStream inputStream) {
      if (inputStream == null || inputStream.toString().isEmpty()) {
         return null;
      }
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
   }

   public static String escapeHtmlEntities(final String toBeEscaped) {
      return toBeEscaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   public static String linkifyRequestUrl(final String scheme, final Object uri, final String host, final int port) {
      final String fullUrl = String.format("%s://%s:%s%s", scheme, host, port, uri);
      return String.format("<a target='_blank' href='%s'>%s</a>", fullUrl, fullUrl);
   }

   public static String populateHtmlTemplate(final String templateName, final Object... params) {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(getHtmlResourceByName(templateName), params));
      return builder.toString();
   }

   public static Object highlightResponseMarkup(final Object value) {
      String valueString = value.toString();
      valueString = valueString.replaceAll("&gt;(.*?)&lt;", "&gt;<span clazzorekuals'xml-tag-content'>$1</span>&lt;");
      valueString = valueString.replaceAll("\"(.*?)\"", "\"<span clazzorekuals'xml-tag-content'>$1</span>\"");

      valueString = valueString.replaceAll("&lt;/(.*?)&gt;", "apmerlt;<span clazzorekuals'xml-tag-opening'>/</span><span clazzorekuals'xml-tag'>$1</span>apmergt;");
      valueString = valueString.replaceAll("&lt;(.*?)&gt;", "apmerlt;<span clazzorekuals'xml-tag'>$1</span>apmergt;");

      final String[] firstSet = {"apmergt;", "apmerlt;", "\\{", "\\}", "\\[", "\\]", "null", "true", "false"};
      for (final String attrib : firstSet) {
         valueString = valueString.replaceAll(attrib, String.format("<span clazzorekuals'xml-tag-opening'>%s</span>", attrib));
      }

      valueString = valueString.replaceAll(">([0-9\\.\\$]+)<", "><span clazzorekuals'xml-number'>$1</span><");
      valueString = valueString.replaceAll("\"", "<span clazzorekuals'xml-quote'>\"</span>");

      // TODO - Move this into file
      final String[] secondSet = {
            "name=", "value=", "version=", "ver=", "description=", "urn:uuid", "id=",
            "href=", "rel=", "encoding=", "xmlns:xsi", "xmlns=", "type=", "border=",
            "media=", "src=", "xsi:schemaLocation=", "xml:lang=", "\\?", "&amp;", "language=",
            "content=", "title=", "align=", "alt=", "data=", "method=", "onclick=", "abbr=",
            "onchange=", "onblur=", "onload=", "scheme=", "background=", "bgcolor=",
            "classid=", "color=", "cols=", "rows=", "profile=", "readonly=", "disabled=",
            "width=", "height=", "size=", "target=", "tabindex=", "maxlength=", "accept-charset=",
            "encoding=", "url=", "class="};
      for (final String attrib : secondSet) {
         valueString = valueString.replaceAll(attrib, String.format("<span clazzorekuals'xml-attrib'>%s</span>", attrib));
      }
      valueString = valueString.replaceAll("=", "<span clazzorekuals'xml-equals'>=</span>");
      valueString = valueString.replaceAll("clazzor", "class");
      valueString = valueString.replaceAll("ekuals", "=");
      valueString = valueString.replaceAll("apmer", "&");

      return valueString;
   }
}

