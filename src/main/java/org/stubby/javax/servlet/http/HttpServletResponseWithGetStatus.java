package org.stubby.javax.servlet.http;

import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Wrapper around HttpServletResponse that enables 'getStatus()' for servlets API v2.5.
 *
 * @author Alexander Zagniotov
 * @since 10/23/12, 2:29 PM
 */
public final class HttpServletResponseWithGetStatus extends HttpServletResponseWrapper {

   private int status = HttpStatus.OK_200;

   /**
    * Constructs a response adaptor wrapping the given response.
    *
    * @throws IllegalArgumentException if the response is null
    */
   public HttpServletResponseWithGetStatus(final HttpServletResponse response) {
      super(response);
   }

   @Override
   public void setStatus(final int status) {
      this.status = status;
      super.setStatus(status);
   }

   public void sendError(final int status) throws IOException {
      this.status = status;
      super.sendError(status);
   }

   public void sendError(final int status, final String msg) throws IOException {
      this.status = status;
      super.sendError(status, msg);
   }

   public int getStatus() {
      return this.status;
   }
}
