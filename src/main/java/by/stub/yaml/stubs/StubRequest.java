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

import by.stub.utils.StringUtils;
import by.stub.utils.HandlerUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

   public static final String AUTH_HEADER = "authorization";

   private String url = null;
   private String method = null;
   private String postBody = null;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> queryParams = new HashMap<String, String>();

   public StubRequest() {

   }

   public final String getUrl() {
      return url;
   }

   public final String getMethod() {
      return StringUtils.toUpper(method);
   }

   public final String getPostBody() {
      return postBody;
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public void setMethod(final String newMethod) {
      this.method = (StringUtils.isSet(newMethod) ? newMethod : null);
   }

   public void setPostBody(final String postBody) {
      this.postBody = postBody;
   }

   public final Map<String, String> getHeaders() {
      return headers;
   }

   public void setHeaders(final Map<String, String> headers) {
      this.headers = headers;
   }

   public final Map<String, String> getQueryParams() {
      return queryParams;
   }

   public void setQueryParams(final Map<String, String> queryParams) {
      this.queryParams = queryParams;
   }

   public final boolean isConfigured() {
      return (url != null && method != null);
   }

   public static final StubRequest creatFromHttpServletRequest(final HttpServletRequest request) throws IOException {
      final StubRequest assertionRequest = new StubRequest();

      assertionRequest.setMethod(request.getMethod());
      assertionRequest.setUrl(request.getPathInfo());
      assertionRequest.setQueryParams(HandlerUtils.constructParamMap(request.getQueryString()));
      assertionRequest.setPostBody(HandlerUtils.extractPostRequestBody(request, "stubs"));

      final String authHeader = request.getHeader(AUTH_HEADER);
      if (StringUtils.isSet(authHeader)) {
         assertionRequest.getHeaders().put(AUTH_HEADER, authHeader);
      }

      return assertionRequest;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest that = (StubRequest) o;


      if (postBody != null ? !postBody.equals(that.postBody) : that.postBody != null) return false;
      if (!method.equals(that.method)) return false;
      if (!url.equals(that.url)) return false;
      if (!queryParams.equals(that.queryParams)) return false;

      return true;
   }

   @Override
   public final int hashCode() {
      int result = url.hashCode();
      result = 31 * result + method.hashCode();
      result = 31 * result + (postBody != null ? postBody.hashCode() : 0);
      return result;
   }

   @Override
   public final String toString() {
      final StringBuffer sb = new StringBuffer();
      sb.append("StubRequest");
      sb.append("{url='").append(url).append('\'');
      sb.append(", method='").append(method).append('\'');
      sb.append(", postBody='").append(postBody).append('\'');
      sb.append(", headers=").append(headers);
      sb.append(", queryParams=").append(queryParams);
      sb.append('}');
      return sb.toString();
   }
}