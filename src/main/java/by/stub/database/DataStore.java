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

package by.stub.database;

import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.UnauthorizedStubResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataStore {

   private final List<StubHttpLifecycle> stubHttpLifecycles;

   public DataStore(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles = Collections.synchronizedList(stubHttpLifecycles);
   }

   public StubResponse findStubResponseFor(final StubRequest assertionStubRequest) {
      return identifyTypeOfStubResponse(new StubHttpLifecycle(assertionStubRequest, new StubResponse()));
   }

   private StubResponse identifyTypeOfStubResponse(final StubHttpLifecycle assertionStubHttpLifecycle) {

      if (stubHttpLifecycles.contains(assertionStubHttpLifecycle)) {
         final int indexOf = stubHttpLifecycles.indexOf(assertionStubHttpLifecycle);
         final StubHttpLifecycle foundStubHttpLifecycle = stubHttpLifecycles.get(indexOf);

         final Map<String, String> headers = foundStubHttpLifecycle.getRequest().getHeaders();
         if (headers.containsKey(StubRequest.AUTH_HEADER)) {
            final String foundBasicAuthorization = headers.get(StubRequest.AUTH_HEADER);
            final String givenBasicAuthorization = assertionStubHttpLifecycle.getRequest().getHeaders().get(StubRequest.AUTH_HEADER);

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

   public final void resetStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles.clear();
      this.stubHttpLifecycles.addAll(stubHttpLifecycles);
   }

   public final List<StubHttpLifecycle> getStubHttpLifecycles() {
      return stubHttpLifecycles;
   }
}