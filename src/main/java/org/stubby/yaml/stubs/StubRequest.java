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
public final class StubRequest {

   private String url = null;
   private String method = null;
   private String postBody = null;
   private Map<String, String> headers = new HashMap<String, String>();

   public StubRequest() {

   }

   public static boolean isFieldCorrespondsToYamlNode(final String fieldName) {
      for (Field field : StubRequest.class.getDeclaredFields()) {
         final String reflectedFieldName = field.getName().toLowerCase();
         if (!fieldName.equals("headers") && reflectedFieldName.equals(fieldName)) {
            return true;
         }
      }
      return false;
   }

   public void setValue(final String fieldName, final String value) throws InvocationTargetException, IllegalAccessException {
      for (Method method : this.getClass().getDeclaredMethods()) {
         if (method.getName().toLowerCase().equals("set" + fieldName)) {
            method.invoke(this, value);
            break;
         }
      }
   }

   public String getUrl() {
      return url;
   }

   public String getMethod() {
      return method;
   }

   public String getPostBody() {
      return postBody;
   }

   public void setUrl(final String url) {
      this.url = url;
   }

   public void setMethod(final String method) {
      this.method = method;
   }

   public void setPostBody(final String postBody) {
      this.postBody = postBody;
   }

   public void addHeader(final String param, final String value) {
      headers.put(param, value);
   }

   public Map<String, String> getHeaders() {
      return headers;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest that = (StubRequest) o;

      if (postBody != null ? !postBody.equals(that.postBody) : that.postBody != null) return false;
      if (!headers.equals(that.headers)) return false;
      if (!method.equals(that.method)) return false;
      if (!url.equals(that.url)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = url.hashCode();
      result = 31 * result + method.hashCode();
      result = 31 * result + (postBody != null ? postBody.hashCode() : 0);
      result = 31 * result + headers.hashCode();
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