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

import by.stub.cli.ANSITerminal;
import by.stub.utils.CollectionUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.IOUtils;
import by.stub.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   private Map<String, String> query = new HashMap<String, String>();

   public StubRequest() {

   }

   public final String getUrl() {
      return getFullUrl();
   }

   public final String getMethod() {
      return StringUtils.toUpper(method);
   }

   public final String getPost() {
      return IOUtils.enforceSystemLineSeparator(post);
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public String getFullUrl() {
      if (query.isEmpty()) {
         return url;
      }

      final String queryString = CollectionUtils.constructQueryString(query);

      return String.format("%s?%s", url, queryString);
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

      final Set<Map.Entry<String, String>> entrySet = headers.entrySet();
      for (final Map.Entry<String, String> entry : entrySet) {
         this.headers.put(StringUtils.toLower(entry.getKey()), entry.getValue());
      }
   }

   public Map<String, String> getQuery() {
      return query;
   }

   public void setQuery(final Map<String, String> query) {
      this.query = query;
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
      return IOUtils.enforceSystemLineSeparator(file);
   }

   public final boolean isConfigured() {
      return (StringUtils.isSet(url) && StringUtils.isSet(method));
   }

   public static StubRequest createFromHttpServletRequest(final HttpServletRequest request) throws IOException {
      final StubRequest assertionRequest = new StubRequest();

      assertionRequest.setMethod(request.getMethod());
      assertionRequest.setUrl(request.getPathInfo());
      assertionRequest.setPost(HandlerUtils.extractPostRequestBody(request, "stubs"));

      final Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
      final List<String> headerNames = headerNamesEnumeration == null ? new LinkedList<String>() : Collections.list(request.getHeaderNames());
      for (final String headerName : headerNames) {
         final String headerValue = request.getHeader(headerName);
         assertionRequest.getHeaders().put(StringUtils.toLower(headerName), headerValue);
      }

      assertionRequest.getQuery().putAll(CollectionUtils.constructParamMap(request.getQueryString()));

      return assertionRequest;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest stub = (StubRequest) o;

      if (compareStubbedPropertyVsAssertionProperty("post body", stub.getPostBody(), getPostBody())) return false;
      if (compareStubbedPropertyVsAssertionProperty("method", stub.method, method)) return false;
      if (compareStubbedPropertyVsAssertionProperty("url", stub.url, url)) return false;

      if (!stub.getHeaders().isEmpty()) {

         final Map<String, String> stubHeadersCopy = new HashMap<String, String>(stub.getHeaders());
         stubHeadersCopy.remove(StubRequest.AUTH_HEADER); //Auth header is dealt with after matching of assertion request
         if (stubHeadersCopy.isEmpty()) {
            return true;
         }

         if (getHeaders().isEmpty()) {
            ANSITerminal.warn(String.format("Stubbed headers could not be matched with empty headers from the client: %s VS %s", stubHeadersCopy, getHeaders()));

            return false;
         }

         if (compareStubbedMapVsAssertionMap(stub.getHeaders(), getHeaders())) return false;
      }

      if (!stub.getQuery().isEmpty() && getQuery().isEmpty()) {
         ANSITerminal.warn(String.format("Stubbed query string could not be matched with empty query string from the client: %s VS %s", stub.getQuery(), getQuery()));
         return false;
      }

      if (compareStubbedMapVsAssertionMap(stub.getQuery(), getQuery())) return false;

      dumpMatchedRequestToConsole(stub);

      return true;
   }

   private void dumpMatchedRequestToConsole(final StubRequest stub) {
      ANSITerminal.info("Matched:");
      ANSITerminal.status("-----------------------------------------------------------------------------------");
      ANSITerminal.loaded("[STUB] >>");
      ANSITerminal.info(String.format("%s", stub));
      ANSITerminal.status("-----------------------------------------------------------------------------------");
      ANSITerminal.loaded("[LIVE] >>");
      ANSITerminal.info(String.format("%s", this));
      ANSITerminal.status("-----------------------------------------------------------------------------------");
   }

   private boolean compareStubbedPropertyVsAssertionProperty(final String propName, final String stubbedProp, final String assertionProp) {
      if (stubbedProp != null ? !stubbedProp.equals(assertionProp) : assertionProp != null) {
         ANSITerminal.warn(String.format("Stubbed %s could not be matched: %s VS %s", propName, stubbedProp, assertionProp));
         return true;
      }
      return false;
   }

   private boolean compareStubbedMapVsAssertionMap(final Map<String, String> stubbedMap, final Map<String, String> assertionMap) {
      final Map<String, String> stubbedMapCopy = new HashMap<String, String>(stubbedMap);
      stubbedMapCopy.entrySet().removeAll(assertionMap.entrySet());
      if (!stubbedMapCopy.isEmpty()) {
         ANSITerminal.warn(String.format("Stubbed hashmap could not be matched: %s VS %s", stubbedMap, assertionMap));
         return true;
      }
      return false;
   }

   @Override
   public final int hashCode() {
      int result = url.hashCode();
      result = 31 * result;
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
      sb.append(", query=").append(query);
      sb.append('}');
      return sb.toString();
   }
}