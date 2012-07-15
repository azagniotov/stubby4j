package org.stubby.handlers;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 9:40 AM
 */
public class HttpRequestInfo {

   public static final String AUTH_HEADER = "authorization";

   private final String method;
   private final String url;
   private final String postBody;
   private final String authorizationHeader;

   public HttpRequestInfo(final HttpServletRequest request, final String postBody) {
      this.method = request.getMethod();
      this.url = constructFullURI(request);
      this.postBody = postBody;
      this.authorizationHeader = request.getHeader(AUTH_HEADER);
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

   public String getAuthorizationHeader() {
      return authorizationHeader;
   }

   private String constructFullURI(final HttpServletRequest request) {
      final String pathInfo = request.getPathInfo();
      final String queryStr = request.getQueryString();
      final String queryString = (queryStr == null || queryStr.equals("")) ? "" : String.format("?%s", request.getQueryString());
      return String.format("%s%s", pathInfo, queryString);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof HttpRequestInfo)) return false;

      final HttpRequestInfo that = (HttpRequestInfo) o;

      if (authorizationHeader != null ? !authorizationHeader.equals(that.authorizationHeader) : that.authorizationHeader != null)
         return false;
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
      result = 31 * result + (authorizationHeader != null ? authorizationHeader.hashCode() : 0);
      return result;
   }
}
