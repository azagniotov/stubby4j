package org.stubby.handlers;

import org.stubby.server.JettyOrchestrator;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
final class HandlerUtils {

   static String getHtmlResourceByName(final String templateSuffix) {
      final String adminHandlerCssPath = String.format("/html/%s.html", templateSuffix);
      final InputStream postBodyInputStream = HandlerUtils.class.getResourceAsStream(adminHandlerCssPath);
      return inputStreamToString(postBodyInputStream);
   }

   static String inputStreamToString(final InputStream inputStream) {
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
   }

   static String escapeHtmlEntities(final String toBeEscaped) {
      return toBeEscaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   static String linkifyRequestUrl(final Object url) {
      return String.format("<a target='_blank' href='http://%s:%s%s'>%s</a>",
            JettyOrchestrator.currentHost, JettyOrchestrator.currentClientPort, url, url);
   }

   static String populateHtmlTemplate(final String templateName, final Object... params) {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(getHtmlResourceByName(templateName), params));
      return builder.toString();
   }
}

