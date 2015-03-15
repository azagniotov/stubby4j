/*
 * Source: https://raw.githubusercontent.com/sushain97/contestManagement/master/src/util/RequestPrinter.java
 * Note: Slight modifications made to fit custom requirements.
 */

package by.stub.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class HttpRequestUtils {

   private static String INDENT_UNIT = "   ";

   private HttpRequestUtils() {

   }

   private static String debugStringSession(HttpSession session, int indent) {
      final String indentString = StringUtils.repeat(INDENT_UNIT, indent);
      if (session == null) {
         return indentString + "{}";
      }
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(indentString).append("{").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'id': '").append(session.getId()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'last_accessed_time': ").append(session.getLastAccessedTime()).append(", ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'max_inactive_interval': ").append(session.getMaxInactiveInterval()).append(", ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'is_new': '").append(session.isNew()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'attributes': {").append(FileUtils.BR);
      Enumeration<String> attributeNames = session.getAttributeNames();
      while (attributeNames != null && attributeNames.hasMoreElements()) {
         String attributeName = attributeNames.nextElement();
         Object o = session.getAttribute(attributeName);
         stringBuilder.
            append(indentString).
            append(INDENT_UNIT).
            append("'").append(attributeName).append("': ").
            append("'").append(o.toString()).append("',").append(FileUtils.BR);
      }
      stringBuilder.append(indentString).append(INDENT_UNIT).append("}").append(FileUtils.BR);
      stringBuilder.append(indentString).append("}").append(FileUtils.BR);
      return stringBuilder.toString();
   }

   private static String debugStringParameter(String indentString, String parameterName, String[] parameterValues) {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.
         append(indentString).
         append(INDENT_UNIT).
         append("'").append(parameterName).append("': ");
      if (parameterValues == null || parameterValues.length == 0) {
         stringBuilder.append("None");
      } else {
         if (parameterValues.length > 1) stringBuilder.append("[");
         stringBuilder.append(StringUtils.join(parameterValues, ','));
         if (parameterValues.length > 1) stringBuilder.append("]");
      }
      return stringBuilder.toString();
   }

   private static String debugStringHeader(String indentString, String headerName, List<String> headerValues) {
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.
         append(indentString).
         append(INDENT_UNIT).
         append("'").append(headerName).append("': ");
      if (headerValues == null || headerValues.size() == 0) {
         stringBuilder.append("None");
      } else {
         if (headerValues.size() > 1) {
            stringBuilder.append("[");
         }
         stringBuilder.append(StringUtils.join(headerValues.toArray(new String[headerValues.size()]), ','));
         if (headerValues.size() > 1) {
            stringBuilder.append("]");
         }
      }
      return stringBuilder.toString();
   }

   @SuppressWarnings({"rawtypes", "unused"})
   private static String debugStringParameters(HttpServletRequest request, int indent) {
      Enumeration<String> parameterNames = request.getParameterNames();
      if (parameterNames == null || !parameterNames.hasMoreElements()) {
         return "{}";
      }

      final String indentString = StringUtils.repeat(INDENT_UNIT, indent);
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("{").append(FileUtils.BR);
      while (parameterNames.hasMoreElements()) {
         String parameterName = parameterNames.nextElement();
         String[] parameterValues = request.getParameterValues(parameterName);
         stringBuilder.
            append(HttpRequestUtils.debugStringParameter(indentString, parameterName, parameterValues)).
            append(",").append(FileUtils.BR);
      }
      stringBuilder.append(indentString).append("}").append(FileUtils.BR);
      return stringBuilder.toString();
   }

   private static String debugStringCookie(Cookie cookie, String indentString) {
      if (cookie == null) {
         return "";
      }
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(indentString).append("{ ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'name': '").append(cookie.getName()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'value': '").append(cookie.getValue()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'domain': '").append(cookie.getDomain()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'path': '").append(cookie.getPath()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'max_age': ").append(cookie.getMaxAge()).append(", ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'version': ").append(cookie.getVersion()).append(", ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'comment': '").append(cookie.getComment()).append("', ").append(FileUtils.BR);
      stringBuilder.append(indentString).append(INDENT_UNIT).append("'secure': '").append(cookie.getSecure()).append("',").append(FileUtils.BR);
      stringBuilder.append(indentString).append("}");

      return stringBuilder.toString();
   }

   private static String debugStringCookies(HttpServletRequest request, int indent) {
      if (request.getCookies() == null) {
         return "[]";
      }
      String indentString = StringUtils.repeat(INDENT_UNIT, indent);
      StringBuilder sb = new StringBuilder();
      sb.append("[").append(FileUtils.BR);
      int cookieCount = 0;
      for (Cookie cookie : request.getCookies()) {
         sb.append(HttpRequestUtils.debugStringCookie(cookie, indentString + INDENT_UNIT)).append(",").append(FileUtils.BR);
         cookieCount++;
      }
      if (cookieCount > 0) {
         sb.delete(sb.length() - ("," + FileUtils.BR).length(), sb.length());
      }
      sb.append("").append(FileUtils.BR).append(indentString).append("]").append(FileUtils.BR);

      return sb.toString();
   }

   private static String debugStringHeaders(HttpServletRequest request, int indent) {
      Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames == null) {
         return "{}";
      }
      final String indentString = StringUtils.repeat(INDENT_UNIT, indent);
      final StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("{").append(FileUtils.BR);
      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         Enumeration<String> headerValues = request.getHeaders(headerName);
         List<String> headerValuesList = new ArrayList<>();
         while (headerValues != null && headerValues.hasMoreElements()) {
            String headerValue = headerValues.nextElement();
            headerValuesList.add(headerValue);
         }
         stringBuilder.
            append(HttpRequestUtils.debugStringHeader(indentString, headerName, headerValuesList)).
            append(",").append(FileUtils.BR);
      }
      stringBuilder.append(indentString).append("}").append(FileUtils.BR);
      return stringBuilder.toString();
   }

   /**
    * Debug complete request
    *
    * @param request Request parameter.
    * @return A string with debug information on Request's header
    */
   public static String dump(HttpServletRequest request) {
      final StringBuilder sb = new StringBuilder();

      // GENERAL INFO
      sb.append(INDENT_UNIT + "PROTOCOL: ").append(request.getProtocol()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "METHOD: ").append(request.getMethod()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "CONTEXT PATH: ").append(request.getContextPath()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "SERVLET PATH: ").append(request.getServletPath()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "AUTH TYPE: ").append(request.getAuthType()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "REMOTE USER: ").append(request.getRemoteUser()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "REQUEST SESSION ID: ").append(request.getRequestedSessionId()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "REQUEST URL: ").append(request.getRequestURL()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "QUERY STRING: ").append(request.getQueryString()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "PATH INFO: ").append(request.getPathInfo()).append(FileUtils.BR);
      sb.append(INDENT_UNIT + "PATH TRANSLATED: ").append(request.getPathTranslated()).append(FileUtils.BR);

      // COOKIES
      sb.append(INDENT_UNIT + "COOKIES: ").append(HttpRequestUtils.debugStringCookies(request, 1)).append(FileUtils.BR);

      // PARAMETERS
      sb.append(INDENT_UNIT + "PARAMETERS: ").append(HttpRequestUtils.debugStringParameters(request, 1)).append(FileUtils.BR);

      // HEADERS
      sb.append(INDENT_UNIT + "HEADERS: ").append(HttpRequestUtils.debugStringHeaders(request, 1)).append(FileUtils.BR);

      // SESSION
      sb.append(INDENT_UNIT + "SESSION: ").append(FileUtils.BR);
      HttpSession session = request.getSession(false);
      if (session != null) {
         sb.append(HttpRequestUtils.debugStringSession(session, 1)).append(FileUtils.BR);
      } else {
         sb.append(INDENT_UNIT + INDENT_UNIT + "NO SESSION AVAILABLE").append(FileUtils.BR);
      }

      return sb.toString();
   }
}
