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

package org.stubby.handlers;

import org.stubby.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestInfo {

   public static final String AUTH_HEADER = "authorization";

   private final String method;
   private final String url;
   private final String postBody;
   private final Map<String, String> headers = new HashMap<String, String>();
   private final Map<String, String> queryParams = new HashMap<String, String>();

   public HttpRequestInfo(final HttpServletRequest request, final String postBody) {
      this.method = request.getMethod();
      this.url = request.getPathInfo();
      this.postBody = postBody;

      final String authHeader = request.getHeader(AUTH_HEADER);
      if (StringUtils.isSet(authHeader)) {
         headers.put(AUTH_HEADER, authHeader);
      }

      this.queryParams.putAll(constructParamMap(request.getQueryString()));
   }

   public String getMethod() {
      return method;
   }

   public String getUrl() {
      return url;
   }

   public String getPostBody() {
      return postBody;
   }

   public Map<String, String> getHeaders() {
      return headers;
   }

   public Map<String, String> getQueryParams() {
      return queryParams;
   }

   private Map<String, String> constructParamMap(final String queryString) {

      if (queryString == null || queryString.trim().length() == 0)
         return new HashMap<String, String>();

      final Map<String, String> paramMap = new HashMap<String, String>();

      final String[] pairs = queryString.split("&");
      for (final String pair : pairs) {
         final String[] splittedPair = pair.split("=");
         paramMap.put(splittedPair[0], splittedPair[1]);
      }

      return paramMap;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof HttpRequestInfo)) return false;

      final HttpRequestInfo that = (HttpRequestInfo) o;

      if (!method.equals(that.method)) return false;
      if (postBody != null ? !postBody.equals(that.postBody) : that.postBody != null) return false;
      if (!url.equals(that.url)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = method.hashCode();
      result = 31 * result + url.hashCode();
      result = 31 * result + (postBody != null ? postBody.hashCode() : 0);
      result = 31 * result + headers.hashCode();
      return result;
   }
}
