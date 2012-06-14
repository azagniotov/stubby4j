package org.stubby.yaml.stubs;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public final class StubResponse {

   private final String status;
   private final String body;
   private final List<StubHeader> headers = new LinkedList<StubHeader>();

   public StubResponse(final String status, final String body) {
      this.status = status;
      this.body = body;
   }

   public String getStatus() {
      return status;
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
}