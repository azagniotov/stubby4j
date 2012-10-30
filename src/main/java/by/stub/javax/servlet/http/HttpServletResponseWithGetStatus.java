/*
HTTP stub server written in Java with embedded Jetty

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

package by.stub.javax.servlet.http;

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
