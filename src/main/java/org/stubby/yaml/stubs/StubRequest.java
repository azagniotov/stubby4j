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

package org.stubby.yaml.stubs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

   private String url = null;
   private String method = null;
   private String postBody = null;
   private Map<String, String> headers = new HashMap<String, String>();

   public StubRequest() {

   }

   public final String getUrl() {
      return url;
   }

   public final String getMethod() {
      return method.toUpperCase();
   }

   public final String getPostBody() {
      return postBody;
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public final void setMethod(final String method) {
      this.method = (method != null ? method.toUpperCase() : null);
   }

   public final void setPostBody(final String postBody) {
      this.postBody = postBody;
   }

   public final void addHeader(final String param, final String value) {
      headers.put(param, value);
   }

   public final Map<String, String> getHeaders() {
      return headers;
   }

   public boolean isConfigured() {
      return (url != null && method != null);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest that = (StubRequest) o;

      if (postBody != null ? !postBody.equals(that.postBody) : that.postBody != null) return false;
      if (!method.equals(that.method)) return false;
      if (!url.equals(that.url)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = url.hashCode();
      result = 31 * result + method.hashCode();
      result = 31 * result + (postBody != null ? postBody.hashCode() : 0);
      return result;
   }

   @Override
   public String toString() {
      return "StubRequest{" +
            "url='" + url + '\'' +
            ", method='" + method + '\'' +
            ", postBody='" + postBody + '\'' +
            ", headers=" + headers +
            '}';
   }
}