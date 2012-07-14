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

import org.stubby.yaml.stubs.NullStubResponse;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

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

   public void setStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles = stubHttpLifecycles;
   }

   protected StubResponse findGetFor(final String pathInfo) {
      final StubHttpLifecycle assertionStubHttpLifecycle = buildAssertionStubRequest("GET", pathInfo, null);
      return findStubResponse(assertionStubHttpLifecycle);
   }

   private StubResponse findStubResponse(final StubHttpLifecycle assertionStubHttpLifecycle) {

      if (stubHttpLifecycles.contains(assertionStubHttpLifecycle)) {
         final int indexOf = stubHttpLifecycles.indexOf(assertionStubHttpLifecycle);
         return stubHttpLifecycles.get(indexOf).getResponse();
      }

      return new NullStubResponse();
   }

   protected StubResponse findPostFor(final String pathInfo, final String postData) {
      final StubHttpLifecycle assertionStubHttpLifecycle = buildAssertionStubRequest("POST", pathInfo, postData);
      return findStubResponse(assertionStubHttpLifecycle);
   }

   private StubHttpLifecycle buildAssertionStubRequest(final String method, final String pathInfo, final String postData) {
      final StubRequest assertionStubRequest = new StubRequest();
      assertionStubRequest.setMethod(method);
      assertionStubRequest.setUrl(pathInfo);
      assertionStubRequest.setPostBody(postData);
      return new StubHttpLifecycle(assertionStubRequest, new StubResponse());
   }

   public StubResponse findResponseFor(final String pathInfo, final String method, final String postData) {
      if (method.equalsIgnoreCase("get")) {
         return findGetFor(pathInfo);
      }
      return findPostFor(pathInfo, postData);
   }

   public List<StubHttpLifecycle> getStubHttpLifecycles() {
      return stubHttpLifecycles;
   }
}
