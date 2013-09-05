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

import by.stub.annotations.CoberturaIgnore;
import by.stub.annotations.VisibleForTesting;
import by.stub.utils.CollectionUtils;
import by.stub.utils.FileUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.YamlProperties;

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

   public static final String AUTH_HEADER = "authorization";

   private final String url;
   private final String post;
   private final File file;
   private final byte[] fileBytes;
   private final List<String> method;
   private final Map<String, String> headers;
   private final Map<String, String> query;
   private final Map<String, String> regexGroups;

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
      this.regexGroups = new TreeMap<String, String>();
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
      } catch (Exception e) {
         return new byte[]{};
      }
   }

   public String getPostBody() {
      if (fileBytes.length == 0) {
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

   // Just a shallow copy that protects collection from modification, the points themselves are not copied
   public Map<String, String> getRegexGroups() {
      return new TreeMap<String, String>(regexGroups);
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
            && arraysIntersect(dataStoreRequest.getMethod(), this.getMethod())
            && postBodiesMatch(dataStoreRequest.getPostBody(), this.getPostBody())
            && headersMatch(dataStoreRequest.getHeaders(), this.getHeaders())
            && queriesMatch(dataStoreRequest.getQuery(), this.getQuery());
      }

      return false;
   }

   private boolean urlsMatch(final String dataStoreUrl, final String thisAssertingUrl) {
      return stringsMatch(dataStoreUrl, thisAssertingUrl, YamlProperties.URL);
   }

   private boolean postBodiesMatch(final String dataStorePostBody, final String thisAssertingPostBody) {
      return stringsMatch(dataStorePostBody, thisAssertingPostBody, YamlProperties.POST);
   }

   private boolean queriesMatch(final Map<String, String> dataStoreQuery, final Map<String, String> thisAssertingQuery) {
      return mapsMatch(dataStoreQuery, thisAssertingQuery, YamlProperties.QUERY);
   }

   private boolean headersMatch(final Map<String, String> dataStoreHeaders, final Map<String, String> thisAssertingHeaders) {
      final Map<String, String> dataStoreHeadersCopy = new HashMap<String, String>(dataStoreHeaders);
      dataStoreHeadersCopy.remove(StubRequest.AUTH_HEADER); //Auth header dealt with in StubbedDataManager after request was matched

      return mapsMatch(dataStoreHeadersCopy, thisAssertingHeaders, YamlProperties.HEADERS);
   }

   private boolean mapsMatch(final Map<String, String> dataStoreMap, final Map<String, String> thisAssertingMap, final String yamlPropertyName) {
      if (dataStoreMap.isEmpty()) {
         return true;
      }

      final Map<String, String> dataStoreMapCopy = new HashMap<String, String>(dataStoreMap);
      final Map<String, String> assertingMapCopy = new HashMap<String, String>(thisAssertingMap);

      for (Map.Entry<String, String> dataStoreParam : dataStoreMapCopy.entrySet()) {
         final boolean containsRequiredParam = assertingMapCopy.containsKey(dataStoreParam.getKey());
         if (!containsRequiredParam) {
            return false;
         } else {
            String assertedQueryValue = assertingMapCopy.get(dataStoreParam.getKey());
            if (!stringsMatch(dataStoreParam.getValue(), assertedQueryValue, yamlPropertyName)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean stringsMatch(final String dataStoreValue, final String thisAssertingValue, final String yamlPropertyName) {
      final boolean isAssertingValueSet = StringUtils.isSet(thisAssertingValue);
      final boolean isDataStoreValueSet = StringUtils.isSet(dataStoreValue);

      if (!isDataStoreValueSet) {
         return true;
      } else if (!isAssertingValueSet) {
         return false;
      } else if (StringUtils.isWithinSquareBrackets(dataStoreValue)) {
         return dataStoreValue.equals(thisAssertingValue);
      } else {
         return regexMatch(dataStoreValue, thisAssertingValue, yamlPropertyName);
      }
   }

   @VisibleForTesting
   boolean regexMatch(final String dataStoreValue, final String thisAssertingValue, final String yamlPropertyName) {
      try {
         // Pattern.MULTILINE changes the behavior of '^' and '$' characters,
         // it does not mean that newline feeds and carriage return will be matched by default
         // You need to make sure that you regex pattern covers both \r (carriage return) and \n (linefeed).
         // It is achievable by using symbol '\s+' which covers both \r (carriage return) and \n (linefeed).
         final Matcher matcher = Pattern.compile(dataStoreValue, Pattern.MULTILINE).matcher(thisAssertingValue);
         final boolean matches = matcher.matches();
         if (matches) {
            //Matcher.groupCount() returns the number of capturing groups in the pattern regardless
            // of whether the capturing groups actually participated in the match.
            final int groupCount = matcher.groupCount();
            // group(0) holds the full match, we are interested in sub groups, therefore we don't care about group#0
            if (groupCount > 0) {
               for (int idx = 1; idx <= groupCount; idx++) {
                  final String regexKey = String.format("%s%s.%s%s",
                     StringUtils.TEMPLATE_TOKEN_LEFT, yamlPropertyName, idx, StringUtils.TEMPLATE_TOKEN_RIGHT);
                  regexGroups.put(regexKey, matcher.group(idx));
               }
            }
         }

         return matches;
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
   @CoberturaIgnore
   public int hashCode() {
      int result = (ObjectUtils.isNotNull(url) ? url.hashCode() : 0);
      result = 31 * result + method.hashCode();
      result = 31 * result + (ObjectUtils.isNotNull(post) ? post.hashCode() : 0);
      result = 31 * result + (ObjectUtils.isNotNull(fileBytes) && fileBytes.length != 0 ? Arrays.hashCode(fileBytes) : 0);
      result = 31 * result + headers.hashCode();
      result = 31 * result + query.hashCode();

      return result;
   }

   @Override
   @CoberturaIgnore
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