package org.stubby.yaml.stubs;

import org.eclipse.jetty.http.HttpStatus;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class NotFoundStubResponse extends StubResponse {

   public NotFoundStubResponse() {
      super();
   }

   @Override
   public int getHttpStatus() {
      return HttpStatus.NOT_FOUND_404;
   }
}