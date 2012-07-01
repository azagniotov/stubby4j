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

import org.stubby.exception.Stubby4JException;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
final class HandlerHelper {

   private HandlerHelper() {

   }

   static String getHtmlResourceByName(final String templateSuffix) {
      final String htmlTemplatePath = String.format("/html/%s.html", templateSuffix);
      final InputStream postBodyInputStream = HandlerHelper.class.getResourceAsStream(htmlTemplatePath);
      if (postBodyInputStream == null) {
         throw new Stubby4JException(String.format("Could not find resource %s", htmlTemplatePath));
      }
      return inputStreamToString(postBodyInputStream);
   }

   static String constructHeaderServerName() {
      final Package pkg = HandlerHelper.class.getPackage();
      final String implementationVersion = pkg.getImplementationVersion() == null ?
            "x.x.x" : pkg.getImplementationVersion();
      final String implementationTitle = pkg.getImplementationTitle() == null ?
            "Java-based HTTP stub server" : pkg.getImplementationTitle();
      return String.format("stubby4j/%s (%s)", implementationVersion, implementationTitle);
   }

   static String inputStreamToString(final InputStream inputStream) {
      if (inputStream == null || inputStream.toString().isEmpty()) {
         return null;
      }
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
   }

   static String escapeHtmlEntities(final String toBeEscaped) {
      return toBeEscaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   static String linkifyRequestUrl(final Object uri, final String host, final int clientPort) {
      return String.format("<a target='_blank' href='http://%s:%s%s'>%s</a>", host, clientPort, uri, uri);
   }

   static String populateHtmlTemplate(final String templateName, final Object... params) {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(getHtmlResourceByName(templateName), params));
      return builder.toString();
   }
}

