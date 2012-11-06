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

package by.stub.yaml.stubs;

import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

   public static final String AUTH_HEADER = "authorization";

   private String url = null;
   private String method = null;
   private String post = null;
   private String file = null;
   private Map<String, String> headers = new HashMap<String, String>();

   public StubRequest() {

   }

   public final String getUrl() {
      return url;
   }

   public final String getMethod() {
      return StringUtils.toUpper(method);
   }

   public final String getPost() {
      return post;
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public void setMethod(final String newMethod) {
      this.method = (StringUtils.isSet(newMethod) ? newMethod : null);
   }

   public void setPost(final String post) {
      this.post = post;
   }

   public final Map<String, String> getHeaders() {
      return headers;
   }

   public void setHeaders(final Map<String, String> headers) {
      this.headers = headers;
   }

   public String getFile() {
      return file;
   }

   public void setFile(final String file) {
      this.file = file;
   }

   public String getPostBody() {
      if (!StringUtils.isSet(file)) {
         return getPost();
      }
      return file;
   }

   public int getUrlHashCode() {
      final String urlToLower = StringUtils.toLower(url.trim());
      final char[] unsortedContent = urlToLower.toCharArray();
      Arrays.sort(unsortedContent);
      final String reconstructedUrl = new String(unsortedContent);

      return reconstructedUrl.hashCode();
   }

   public final boolean isConfigured() {
      return (StringUtils.isSet(url) && StringUtils.isSet(method));
   }

   public static StubRequest createFromHttpServletRequest(final HttpServletRequest request) throws IOException {
      final StubRequest assertionRequest = new StubRequest();

      assertionRequest.setMethod(request.getMethod());

      final String url = StringUtils.isSet(request.getQueryString()) ?
            String.format("%s?%s", request.getPathInfo(), request.getQueryString()) : request.getPathInfo();

      assertionRequest.setUrl(url);
      assertionRequest.setPost(HandlerUtils.extractPostRequestBody(request, "stubs"));

      final Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
      final List<String> headerNames = headerNamesEnumeration == null ? new LinkedList<String>() : Collections.list(request.getHeaderNames());
      for (final String headerName : headerNames) {
         final String headerValue = request.getHeader(headerName);
         assertionRequest.getHeaders().put(StringUtils.toLower(headerName), headerValue);
      }

      return assertionRequest;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest that = (StubRequest) o;

      if (getPostBody() != null ? !getPostBody().equals(that.getPostBody()) : that.getPostBody() != null) return false;
      if (!method.equals(that.method)) return false;
      if (getUrlHashCode() != that.getUrlHashCode()) return false;

      if (!that.getHeaders().isEmpty()) {
         final Map<String, String> headersCopy = new HashMap<String, String>(that.getHeaders());
         headersCopy.remove(StubRequest.AUTH_HEADER); //Auth header is dealt with after matching of assertion request
         if (headersCopy.isEmpty()) {
            return true;
         }
         final boolean wasSetChangedInSomeWay = headersCopy.entrySet().removeAll(headers.entrySet());
         if (wasSetChangedInSomeWay && !headersCopy.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public final int hashCode() {
      int result = url.hashCode();
      result = 31 * result + getUrlHashCode();
      result = 31 * result + getPostBody().hashCode();
      return result;
   }

   @Override
   public final String toString() {
      final StringBuffer sb = new StringBuffer();
      sb.append("StubRequest");
      sb.append("{url='").append(url).append('\'');
      sb.append(", method='").append(method).append('\'');
      sb.append(", post='").append(post).append('\'');
      sb.append(", file='").append(file).append('\'');
      sb.append(", headers=").append(headers);
      sb.append('}');
      return sb.toString();
   }
}