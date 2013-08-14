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
import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

   private static final String REGEX_START = "^";
   private static final String REGEX_END = "$";
   public static final String AUTH_HEADER = "authorization";

   private final String url;
   private final String post;
   private final File file;
   private final byte[] fileBytes;
   private final List<String> method;
   private final Map<String, String> headers;
   private final Map<String, String> query;

   public StubRequest(final String url,
                      final String post,
                      final File file,
                      final List<String> method,
                      final Map<String, String> headers,
                      final Map<String, String> query) {
      this.url = url;
      this.post = post;
      this.file = file;
      this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
      this.method = ObjectUtils.isNull(method) ? new ArrayList<String>() : method;
      this.headers = ObjectUtils.isNull(headers) ? new HashMap<String, String>() : headers;
      this.query = ObjectUtils.isNull(query) ? new LinkedHashMap<String, String>() : query;

   }

   public final ArrayList<String> getMethod() {
      final ArrayList<String> uppercase = new ArrayList<String>(method.size());

      for (final String string : method) {
         uppercase.add(StringUtils.toUpper(string));
      }

      return uppercase;
   }

   public void addMethod(final String newMethod) {
      if (StringUtils.isSet(newMethod)) {
         method.add(newMethod);
      }
   }

   public String getUrl() {
      if (getQuery().isEmpty()) {
         return url;
      }

      final String queryString = CollectionUtils.constructQueryString(query);

      return String.format("%s?%s", url, queryString);
   }

   private byte[] getFileBytes() {
      try {
         return FileUtils.fileToBytes(file);
      } catch (IOException e) {
      }
      return new byte[]{};
   }

   public String getPostBody() {
      if (ObjectUtils.isNull(fileBytes) || fileBytes.length == 0) {
         return FileUtils.enforceSystemLineSeparator(post);
      }
      final String utf8FileContent = StringUtils.newStringUtf8(fileBytes);
      return FileUtils.enforceSystemLineSeparator(utf8FileContent);
   }

   //Used by reflection when populating stubby admin page with stubbed information
   public String getPost() {
      return post;
   }

   public final Map<String, String> getHeaders() {
      final Map<String, String> headersCopy = new HashMap<String, String>(headers);
      final Set<Map.Entry<String, String>> entrySet = headersCopy.entrySet();
      this.headers.clear();
      for (final Map.Entry<String, String> entry : entrySet) {
         this.headers.put(StringUtils.toLower(entry.getKey()), entry.getValue());
      }

      return headers;
   }

   public Map<String, String> getQuery() {
      return query;
   }

   public byte[] getFile() {
      return fileBytes;
   }

   public File getRawFile() {
      return file;
   }

   public boolean hasHeaders() {
      return !getHeaders().isEmpty();
   }

   public boolean hasQuery() {
      return !getQuery().isEmpty();
   }

   public boolean hasPostBody() {
      return StringUtils.isSet(getPostBody());
   }

   public static StubRequest newStubRequest() {
      return new StubRequest(null, null, null, null, null, null);
   }

   public static StubRequest newStubRequest(final String url, final String post) {
      return new StubRequest(url, post, null, null, null, null);
   }

   public static StubRequest createFromHttpServletRequest(final HttpServletRequest request) throws IOException {
      final StubRequest assertionRequest = StubRequest.newStubRequest(request.getPathInfo(),
         HandlerUtils.extractPostRequestBody(request, "stubs"));
      assertionRequest.addMethod(request.getMethod());

      final Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
      final List<String> headerNames = ObjectUtils.isNotNull(headerNamesEnumeration)
         ? Collections.list(request.getHeaderNames()) : new LinkedList<String>();
      for (final String headerName : headerNames) {
         final String headerValue = request.getHeader(headerName);
         assertionRequest.getHeaders().put(StringUtils.toLower(headerName), headerValue);
      }

      assertionRequest.getQuery().putAll(CollectionUtils.constructParamMap(request.getQueryString()));

      return assertionRequest;
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

   private boolean urlsMatch(final String dataStoreUrl, final String thisAssertingUrl) {
      return stringsMatch(dataStoreUrl, thisAssertingUrl);
   }

   private boolean postBodiesMatch(final String dataStorePostBody, final String thisAssertingPostBody) {
      return stringsMatch(dataStorePostBody, thisAssertingPostBody);
   }

   private boolean queriesMatch(final Map<String, String> dataStoreQuery, final Map<String, String> thisAssertingQuery) {
      return mapsMatch(dataStoreQuery, thisAssertingQuery);
   }

   private boolean headersMatch(final Map<String, String> dataStoreHeaders, final Map<String, String> thisAssertingHeaders) {
      final Map<String, String> dataStoreHeadersCopy = new HashMap<String, String>(dataStoreHeaders);
      dataStoreHeadersCopy.remove(StubRequest.AUTH_HEADER); //Auth header dealt with in StubbedDataManager after request was matched

      return mapsMatch(dataStoreHeadersCopy, thisAssertingHeaders);
   }

   private boolean mapsMatch(final Map<String, String> dataStoreMap, final Map<String, String> thisAssertingMap) {
      if (dataStoreMap.isEmpty()) {
         return true;
      }

      final Map<String, String> dataStoreMapCopy = new HashMap<String, String>(dataStoreMap);
      final Map<String, String> assertingMapCopy = new HashMap<String, String>(thisAssertingMap);

      for (Map.Entry<String, String> dataStoreParam: dataStoreMapCopy.entrySet()) {
         final boolean containsRequiredParam = assertingMapCopy.containsKey(dataStoreParam.getKey());
         if (!containsRequiredParam) {
            return false;
         } else {
            String assertedQueryValue = assertingMapCopy.get(dataStoreParam.getKey());
            if (!stringsMatch(dataStoreParam.getValue(), assertedQueryValue)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean stringsMatch(final String dataStoreValue, final String thisAssertingValue) {
      final boolean isAssertingValueSet = StringUtils.isSet(thisAssertingValue);
      final boolean isDatastoreValueSet = StringUtils.isSet(dataStoreValue);

      if (!isDatastoreValueSet) {
         return true;
      } else if (!isAssertingValueSet) {
         return false;
      } else if (isArrayString(dataStoreValue)) {
         return dataStoreValue.equals(thisAssertingValue);
      } else {
         return regexMatch(dataStoreValue, thisAssertingValue);
      }
   }

   private boolean isArrayString(final String dataStoreValue) {
      return dataStoreValue.startsWith("[") && dataStoreValue.endsWith("]");
   }

   private boolean regexMatch(final String dataStoreValue, final String thisAssertingValue) {
      try {
         return Pattern.compile(dataStoreValue, Pattern.MULTILINE).matcher(thisAssertingValue).matches();
      } catch (PatternSyntaxException e) {
         return dataStoreValue.equals(thisAssertingValue);
      }
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

   @Override
   public int hashCode() {
      int result = (ObjectUtils.isNotNull(url) ? url.hashCode() : 0);
      result = 31 * result + method.hashCode();
      result = 31 * result + (ObjectUtils.isNotNull(post) ? post.hashCode() : 0);
      result = 31 * result + (ObjectUtils.isNotNull(fileBytes) ? Arrays.hashCode(fileBytes) : 0);
      result = 31 * result + headers.hashCode();
      result = 31 * result + query.hashCode();

      return result;
   }

   @Override
   public final String toString() {
      final StringBuffer sb = new StringBuffer();
      sb.append("StubRequest");
      sb.append("{url=").append(url);
      sb.append(", method=").append(method);

      if (!ObjectUtils.isNull(post)) {
         sb.append(", post=").append(post);
      }
      sb.append(", query=").append(query);
      sb.append(", headers=").append(getHeaders());
      sb.append('}');

      return sb.toString();
   }

}
