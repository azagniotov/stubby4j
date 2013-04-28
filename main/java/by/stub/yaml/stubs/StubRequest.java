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

import by.stub.utils.CollectionUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethods;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

   private static final String REGEX_START = "^";
   private static final String REGEX_END = "$";
   public static final String AUTH_HEADER = "authorization";

   private String url;
   private ArrayList<String> method = new ArrayList<String>(1) {{
      add(HttpMethods.GET);
   }};
   private String post;
   private byte[] file;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> query = new LinkedHashMap<String, String>();

   public StubRequest() {

   }

   public final ArrayList<String> getMethod() {
      final ArrayList<String> uppercase = new ArrayList<String>(method.size());

      for (final String string : method) {
         uppercase.add(StringUtils.toUpper(string));
      }

      return uppercase;
   }

   public void setMethod(final String newMethod) {
      this.method = new ArrayList<String>(1) {{
         add((StringUtils.isSet(newMethod) ? newMethod : HttpMethods.GET));
      }};
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public final String getUrl() {
      if (getQuery().isEmpty()) {
         return url;
      }

      final String queryString = CollectionUtils.constructQueryString(query);

      return String.format("%s?%s", url, queryString);
   }

   public String getPostBody() {
      if (file == null) {
         return FileUtils.enforceSystemLineSeparator(post);
      }
      final String utf8FileContent = new String(file, StringUtils.charsetUTF8());
      return FileUtils.enforceSystemLineSeparator(utf8FileContent);
   }

   public void setPost(final String post) {
      this.post = post;
   }

   //Used by reflection when populating stubby admin page with stubbed information
   public String getPost() {
      return post;
   }

   public final Map<String, String> getHeaders() {
      final Map<String, String> headersCopy = new HashMap<String, String>();
      final Set<Map.Entry<String, String>> entrySet = headers.entrySet();
      for (final Map.Entry<String, String> entry : entrySet) {
         headersCopy.put(StringUtils.toLower(entry.getKey()), entry.getValue());
      }

      return headersCopy;
   }

   public void setHeaders(final Map<String, String> headers) {
      this.headers = headers;
   }

   public void setQuery(final Map<String, String> query) {
      this.query = query;
   }

   public Map<String, String> getQuery() {
      return query;
   }


   public byte[] getFile() {
      return file;
   }

   public void setFile(final byte[] file) {
      this.file = file;
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

   private boolean regexMatch(final String dataStoreRequestUrl, final String assertingUrl) {

      final Pattern pattern = Pattern.compile(dataStoreRequestUrl);
      final Matcher matcher = pattern.matcher(assertingUrl);

      final boolean isRegexStart = dataStoreRequestUrl.startsWith(REGEX_START);
      final boolean isRegexEnd = dataStoreRequestUrl.endsWith(REGEX_END);

      if (isRegexStart || isRegexEnd) {
         return matcher.find();
      }

      return matcher.matches();
   }

   private boolean stringsMatch(final String dataStoreValue, final String thisAssertingValue) {
      final boolean isAssertingValueSet = StringUtils.isSet(thisAssertingValue);
      final boolean isDatastoreValueSet = StringUtils.isSet(dataStoreValue);

      if (!isDatastoreValueSet) {
         return true;
      } else if (isAssertingValueSet) {
         return dataStoreValue.equals(thisAssertingValue);
      }

      return false;
   }

   private boolean arraysIntersect(final ArrayList<String> dataStoreArray, final ArrayList<String> thisAssertingArray) {
      if (dataStoreArray.isEmpty()) {
         return true;
      } else if (!thisAssertingArray.isEmpty()) {
         for (final String entry : thisAssertingArray) {
            if (dataStoreArray.contains(entry)) {
               return true;
            }
         }
      }
      return false;
   }

   private boolean mapsMatch(final Map<String, String> dataStoreMap, final Map<String, String> thisAssertingMap) {
      if (dataStoreMap.isEmpty()) {
         return true;
      }

      final Map<String, String> assertingMapCopy = new HashMap<String, String>(thisAssertingMap);
      final Map<String, String> dataStoreMapCopy = new HashMap<String, String>(dataStoreMap);
      dataStoreMapCopy.entrySet().removeAll(assertingMapCopy.entrySet());

      return dataStoreMapCopy.isEmpty();
   }

   private boolean urlsMatch(final String dataStoreUrl, final String thisAssertingUrl) {

      if (!StringUtils.isSet(dataStoreUrl)) {
         return true;
      } else if (!StringUtils.isSet(thisAssertingUrl)) {
         return false;
      }

      return regexMatch(dataStoreUrl, thisAssertingUrl);
   }

   private boolean postBodiesMatch(final String dataStorePostBody, final String thisAssertingPostBody) {
      return stringsMatch(dataStorePostBody, thisAssertingPostBody);
   }

   private boolean queriesMatch(final Map<String, String> dataStoreQuery, final Map<String, String> thisAssertingQuery) {
      return mapsMatch(dataStoreQuery, thisAssertingQuery);
   }

   private boolean headersMatch(final Map<String, String> dataStoreHeaders, final Map<String, String> thisAssertingHeaders) {
      final Map<String, String> dataStoreHeadersCopy = new HashMap<String, String>(dataStoreHeaders);
      dataStoreHeadersCopy.remove(StubRequest.AUTH_HEADER); //Auth header dealt with in DataStore after request was matched

      return mapsMatch(dataStoreHeadersCopy, thisAssertingHeaders);
   }

   @Override
   public int hashCode() {
      int result = (url != null ? url.hashCode() : 0);
      result = 31 * result + method.hashCode();
      result = 31 * result + (post != null ? post.hashCode() : 0);
      result = 31 * result + (file != null ? Arrays.hashCode(file) : 0);
      result = 31 * result + headers.hashCode();
      result = 31 * result + query.hashCode();

      return result;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof StubRequest) {
         final StubRequest dataStoreRequest = (StubRequest) o;

         return urlsMatch(dataStoreRequest.url, this.url)
            && arraysIntersect(dataStoreRequest.getMethod(), getMethod())
            && postBodiesMatch(dataStoreRequest.getPostBody(), this.getPostBody())
            && headersMatch(dataStoreRequest.getHeaders(), this.getHeaders())
            && queriesMatch(dataStoreRequest.getQuery(), this.getQuery());
      }

      return false;
   }

   @Override
   public final String toString() {
      final StringBuffer sb = new StringBuffer();
      sb.append("StubRequest");
      sb.append("{url=").append(url);
      sb.append(", method=").append(method);

      if (post != null) {
         sb.append(", post=").append(post);
      }
      sb.append(", query=").append(query);
      sb.append(", headers=").append(getHeaders());
      sb.append('}');

      return sb.toString();
   }

}