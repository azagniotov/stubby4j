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
public final class StubResponse {

   private String status = null;
   private String body = null;
   private Map<String, String> headers = new HashMap<String, String>();

   public StubResponse() {

   }

   public static boolean isFieldCorrespondsToYamlNode(final String fieldName) {
      for (Field field : StubResponse.class.getDeclaredFields()) {
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

   public String getStatus() {
      return status;
   }

   public String getBody() {
      return body;
   }

   public void setStatus(final String status) {
      this.status = status;
   }

   public void setBody(final String body) {
      this.body = body;
   }

   public void addHeader(final String param, final String value) {
      headers.put(param, value);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubResponse)) return false;

      final StubResponse that = (StubResponse) o;

      if (!body.equals(that.body)) return false;
      if (!headers.equals(that.headers)) return false;
      if (!status.equals(that.status)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = status.hashCode();
      result = 31 * result + body.hashCode();
      result = 31 * result + headers.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return "StubResponse{" +
            "status='" + status + '\'' +
            ", body='" + body + '\'' +
            ", headers=" + headers +
            '}';
   }
}