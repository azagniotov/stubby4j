package org.stubby.yaml.stubs;

import org.eclipse.jetty.http.HttpStatus;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class RedirectStubResponse extends StubResponse {

   public RedirectStubResponse() {
      super();
   }

   @Override
   public int getHttpStatus() {
      return HttpStatus.MOVED_PERMANENTLY_301;
   }
}