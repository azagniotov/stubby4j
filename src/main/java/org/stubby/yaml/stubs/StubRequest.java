package org.stubby.yaml.stubs;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public final class StubRequest {

   private final String url;
   private final String method;
   private final String body;
   private final List<StubHeader> headers = new LinkedList<StubHeader>();

   public StubRequest(final String url, final String method, final String body) {
      this.url = url;
      this.method = method;
      this.body = body;
   }

   public String getUrl() {
      return url;
   }

   public String getMethod() {
      return method;
   }

   public String getBody() {
      return body;
   }

   public List<StubHeader> getHeaders() {
      return headers;
   }

   public void addHeader(final StubHeader header) {
      headers.add(header);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubRequest)) return false;

      final StubRequest that = (StubRequest) o;

      if (body != null ? !body.equals(that.body) : that.body != null) return false;
      if (!headers.equals(that.headers)) return false;
      if (!method.equals(that.method)) return false;
      if (!url.equals(that.url)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = url.hashCode();
      result = 31 * result + method.hashCode();
      result = 31 * result + (body != null ? body.hashCode() : 0);
      result = 31 * result + headers.hashCode();
      return result;
   }
}