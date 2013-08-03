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

import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.UnauthorizedStubResponse;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class StubbedDataManager {

   private final File dataYaml;
   private final List<StubHttpLifecycle> stubHttpLifecycles;

   public StubbedDataManager(final File dataYaml, final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.dataYaml = dataYaml;
      this.stubHttpLifecycles = Collections.synchronizedList(stubHttpLifecycles);
   }

   public StubResponse findStubResponseFor(final StubRequest assertingRequest) {
      final StubHttpLifecycle assertingLifecycle = new StubHttpLifecycle();
      assertingLifecycle.setRequest(assertingRequest);
      assertingLifecycle.setResponse(new StubResponse());

      return identifyStubResponseType(assertingLifecycle);
   }

   private synchronized StubResponse identifyStubResponseType(final StubHttpLifecycle assertingLifecycle) {

      final int listIndex = stubHttpLifecycles.indexOf(assertingLifecycle);
      if (listIndex < 0) {
         return new NotFoundStubResponse();
      }

      final StubHttpLifecycle matchedLifecycle = stubHttpLifecycles.get(listIndex);
      final StubResponse stubResponse = matchedLifecycle.getResponse();

      if (matchedLifecycle.isRestricted() && matchedLifecycle.hasNotAuthorized(assertingLifecycle)) {
         return new UnauthorizedStubResponse();
      }

      if (stubResponse.hasHeader("location")) {
         return new RedirectStubResponse().configure(stubResponse);
      }

      return stubResponse;
   }

   public synchronized boolean resetStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles.clear();
      return this.stubHttpLifecycles.addAll(stubHttpLifecycles);
   }

   public List<StubHttpLifecycle> getStubHttpLifecycles() {
      return stubHttpLifecycles;
   }

   public File getDataYaml() {
      return dataYaml;
   }

   public String getMarshalledYamlByIndex(final int httpLifecycleIndex) {
      return stubHttpLifecycles.get(httpLifecycleIndex).getMarshalledYaml();
   }

   public String getYamlParentDirectory() {
      return dataYaml.getParent();
   }

   public synchronized void updateStubHttpLifecycleByIndex(final int httpLifecycleIndex, final StubHttpLifecycle newStubHttpLifecycle) {
      final StubHttpLifecycle removedLifecycle = stubHttpLifecycles.remove(httpLifecycleIndex);
      if (removedLifecycle != null) {
         stubHttpLifecycles.add(httpLifecycleIndex, newStubHttpLifecycle);
      }
   }
}