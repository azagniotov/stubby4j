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

package org.stubby.database;

import org.stubby.handlers.HttpRequestInfo;
import org.stubby.yaml.stubs.NotFoundStubResponse;
import org.stubby.yaml.stubs.RedirectStubResponse;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;
import org.stubby.yaml.stubs.UnauthorizedStubResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 7/1/12, 11:22 PM
 */
public class DataStore {

   private List<StubHttpLifecycle> stubHttpLifecycles = new LinkedList<StubHttpLifecycle>();

   public DataStore() {

   }

   public StubResponse findStubResponseFor(final HttpRequestInfo httpRequestInfo) {

      final StubRequest assertionStubRequest = new StubRequest();

      assertionStubRequest.setMethod(httpRequestInfo.getMethod());
      assertionStubRequest.setUrl(httpRequestInfo.getUrl());
      assertionStubRequest.getHeaders().put(HttpRequestInfo.AUTH_HEADER, httpRequestInfo.getAuthorizationHeader());

      if (httpRequestInfo.getPostBody() != null) {
         assertionStubRequest.setPostBody(httpRequestInfo.getPostBody());
      }

      return identifyTypeOfStubResponse(new StubHttpLifecycle(assertionStubRequest, new StubResponse()));
   }

   private StubResponse identifyTypeOfStubResponse(final StubHttpLifecycle assertionStubHttpLifecycle) {

      if (stubHttpLifecycles.contains(assertionStubHttpLifecycle)) {
         final int indexOf = stubHttpLifecycles.indexOf(assertionStubHttpLifecycle);
         final StubHttpLifecycle foundStubHttpLifecycle = stubHttpLifecycles.get(indexOf);

         if (foundStubHttpLifecycle.getRequest().getHeaders().containsKey(HttpRequestInfo.AUTH_HEADER)) {
            final String foundBasicAuthorization = foundStubHttpLifecycle.getRequest().getHeaders().get(HttpRequestInfo.AUTH_HEADER);
            final String givenBasicAuthorization = assertionStubHttpLifecycle.getRequest().getHeaders().get(HttpRequestInfo.AUTH_HEADER);

            if (!foundBasicAuthorization.equals(givenBasicAuthorization)) {
               return new UnauthorizedStubResponse();
            }
         }

         if (foundStubHttpLifecycle.getResponse().getHeaders().containsKey("location")) {
            final RedirectStubResponse redirectStubResponse = new RedirectStubResponse();

            redirectStubResponse.setLatency(foundStubHttpLifecycle.getResponse().getLatency());
            redirectStubResponse.setBody(foundStubHttpLifecycle.getResponse().getBody());
            redirectStubResponse.setStatus(foundStubHttpLifecycle.getResponse().getStatus());
            redirectStubResponse.setHeaders(foundStubHttpLifecycle.getResponse().getHeaders());

            return redirectStubResponse;
         }

         return foundStubHttpLifecycle.getResponse();
      }

      return new NotFoundStubResponse();
   }

   public void setStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles = stubHttpLifecycles;
   }

   public List<StubHttpLifecycle> getStubHttpLifecycles() {
      return stubHttpLifecycles;
   }
}
