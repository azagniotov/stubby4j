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

package by.stub.yaml.stubs;

import by.stub.utils.ObjectUtils;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class RedirectStubResponse extends StubResponse {

   private RedirectStubResponse() {
      super();
   }

   @Override
   public StubResponseTypes getStubResponseType() {
      return StubResponseTypes.REDIRECT;
   }

   public static RedirectStubResponse newRedirectStubResponse(final StubResponse stubResponse) {
      final RedirectStubResponse redirectStubResponse = new RedirectStubResponse();

      if (ObjectUtils.isNull(stubResponse)) {
          return redirectStubResponse;
      }
      redirectStubResponse.setLatency(stubResponse.getLatency());
      redirectStubResponse.setBody(stubResponse.getBody());
      redirectStubResponse.setStatus(stubResponse.getStatus());
      redirectStubResponse.setHeaders(stubResponse.getHeaders());
      redirectStubResponse.setFile(stubResponse.getFile());

      return redirectStubResponse;
   }
}