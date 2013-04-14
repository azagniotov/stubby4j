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
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.CollectionUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
   private ArrayList<String> method = new ArrayList<String>(1) {{
      add("GET");
   }};
   private String post = null;
   private byte[] file = null;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> query = new HashMap<String, String>();

   public StubRequest() {

   }

   public final ArrayList<String> getMethod() {
      final ArrayList<String> uppercase = new ArrayList<String>(method.size());

      for (final String string : method) uppercase.add(StringUtils.toUpper(string));

      return uppercase;
   }

   public void setMethod(final String newMethod) {
      this.method = new ArrayList<String>(1) {{
         add((StringUtils.isSet(newMethod) ? newMethod : "GET"));
      }};
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public final String getUrl() {
      if (query.isEmpty()) {
         return url;
      }

      final String queryString = CollectionUtils.constructQueryString(query);

      return String.format("%s?%s", url, queryString);
   }

   public String getPostBody() {
      if (file == null) {
         return FileUtils.enforceSystemLineSeparator(post);
      }
      return new String(file, StringUtils.utf8Charset());
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


   public byte[] getFile() {
      return file;
   }


   public final boolean isConfigured() {
      return StringUtils.isSet(url);
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

      final StubRequest other = (StubRequest) o;

      final String postBody = other.getPostBody();
      if (StringUtils.isSet(postBody) && stringValuesDoNotMatch("post body", other.getPostBody(), this.getPostBody()))
         return false;

      if (arraysDoNotMatch(other.method, this.method)) return false;
      if (stringValuesDoNotMatch("url", other.url, this.url)) return false;

      if (!other.getHeaders().isEmpty()) {

         final Map<String, String> stubHeadersCopy = new HashMap<String, String>(other.getHeaders());
         stubHeadersCopy.remove(StubRequest.AUTH_HEADER); //Auth header is dealt with after matching of assertion request
         if (stubHeadersCopy.isEmpty()) {
            return true;
         }

         if (getHeaders().isEmpty()) {
            ANSITerminal.warn(String.format("Stubbed headers could not be matched with empty headers from the client: %s VS %s", stubHeadersCopy, getHeaders()));

            return false;
         }

         if (mapsDoNotMatch(other.getHeaders(), getHeaders())) return false;
      }

      if (!other.getQuery().isEmpty() && getQuery().isEmpty()) {
         ANSITerminal.warn(String.format("Stubbed query string could not be matched with empty query string from the client: %s VS %s", other.getQuery(), getQuery()));
         return false;
      }

      if (mapsDoNotMatch(other.getQuery(), getQuery())) return false;

      dumpMatchedRequestToConsole(other);

      return true;
   }

   private void dumpMatchedRequestToConsole(final StubRequest stub) {
      if (!CommandLineInterpreter.isDebug()) return;
      ANSITerminal.info("Matched:");
      ANSITerminal.status("-----------------------------------------------------------------------------------");
      ANSITerminal.loaded("[STUB] >>");
      ANSITerminal.info(String.format("%s", stub));
      ANSITerminal.status("-----------------------------------------------------------------------------------");
      ANSITerminal.loaded("[LIVE] >>");
      ANSITerminal.info(String.format("%s", this));
      ANSITerminal.status("-----------------------------------------------------------------------------------");
   }

   private boolean stringValuesDoNotMatch(final String propName, final String othersPropValue, final String myPropValue) {
      if (othersPropValue != null ? !othersPropValue.equals(myPropValue) : myPropValue != null) {
         if (CommandLineInterpreter.isDebug())
            ANSITerminal.warn(String.format("Could not match incoming '%s' with configured: %s VS %s", propName, othersPropValue, myPropValue));
         return true;
      }
      return false;
   }

   private boolean arraysDoNotMatch(final ArrayList<String> othersArray, final ArrayList<String> myArray) {
      if (othersArray == null && myArray == null) return false;
      if (othersArray == null || myArray == null) return true;

      if (othersArray.size() == 0 && myArray.size() == 0) return false;

      return !othersArray.contains(myArray.get(0));
   }

   private boolean mapsDoNotMatch(final Map<String, String> othersMap, final Map<String, String> myMap) {
      final Map<String, String> stubbedMapCopy = new HashMap<String, String>(othersMap);
      stubbedMapCopy.entrySet().removeAll(myMap.entrySet());
      if (!stubbedMapCopy.isEmpty()) {
         if (CommandLineInterpreter.isDebug())
            ANSITerminal.warn(String.format("Stubbed hashmap could not be matched: %s VS %s", othersMap, myMap));
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
      sb.append(", file='").append(Arrays.toString(file)).append('\'');
      sb.append(", headers=").append(headers);
      sb.append(", query=").append(query);
      sb.append('}');
      return sb.toString();
   }
}