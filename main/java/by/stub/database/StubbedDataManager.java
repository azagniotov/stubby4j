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

import by.stub.utils.ObjectUtils;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.UnauthorizedStubResponse;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StubbedDataManager {

   private final File dataYaml;
   private final String dataYamlAbsolutePath;
   private final String dataYamlParentDirectory;
   private final List<StubHttpLifecycle> stubHttpLifecycles;

   public StubbedDataManager(final File dataYaml, final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.dataYaml = dataYaml;
      this.dataYamlAbsolutePath = this.dataYaml.getAbsolutePath();
      this.dataYamlParentDirectory = this.dataYaml.getParent();
      this.stubHttpLifecycles = new LinkedList<StubHttpLifecycle>(stubHttpLifecycles);
   }

   public StubResponse findStubResponseFor(final StubRequest assertingRequest) {
      final StubHttpLifecycle assertingLifecycle = new StubHttpLifecycle();
      assertingLifecycle.setRequest(assertingRequest);
      assertingLifecycle.setResponse(StubResponse.newStubResponse());

      return identifyStubResponseType(assertingLifecycle);
   }

   private StubResponse identifyStubResponseType(final StubHttpLifecycle assertingLifecycle) {

      final StubHttpLifecycle matchedLifecycle = getMatchedStubHttpLifecycle(assertingLifecycle);
      if (ObjectUtils.isNull(matchedLifecycle)) {
         return new NotFoundStubResponse();
      }

      final StubResponse stubResponse = matchedLifecycle.getResponse();
      if (matchedLifecycle.isRestricted() && matchedLifecycle.hasNotAuthorized(assertingLifecycle)) {
         return new UnauthorizedStubResponse();
      }

      if (stubResponse.hasHeaderLocation()) {
         return RedirectStubResponse.newRedirectStubResponse(stubResponse);
      }

      return stubResponse;
   }

   private synchronized StubHttpLifecycle getMatchedStubHttpLifecycle(final StubHttpLifecycle assertingLifecycle) {
      final int listIndex = stubHttpLifecycles.indexOf(assertingLifecycle);
      if (listIndex < 0) {
         return StubHttpLifecycle.NULL;
      }
      return stubHttpLifecycles.get(listIndex);
   }

   public synchronized boolean resetStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles.clear();
      return this.stubHttpLifecycles.addAll(stubHttpLifecycles);
   }

   // Just a shallow copy that protects collection from modification, the points themselves are not copied
   public List<StubHttpLifecycle> getStubHttpLifecycles() {
      return new LinkedList<StubHttpLifecycle>(stubHttpLifecycles);
   }

   public File getDataYaml() {
      return dataYaml;
   }

   public String getYamlAbsolutePath() {
      return dataYamlAbsolutePath;
   }

   public String getYamlParentDirectory() {
      return dataYamlParentDirectory;
   }

   public synchronized String getMarshalledYaml() {
      final StringBuilder builder = new StringBuilder();
      for (final StubHttpLifecycle cycle : stubHttpLifecycles) {
         builder.append(cycle.getMarshalledYaml()).append("\n\n");
      }

      return builder.toString();
   }

   public synchronized String getMarshalledYamlByIndex(final int httpLifecycleIndex) {
      return stubHttpLifecycles.get(httpLifecycleIndex).getMarshalledYaml();
   }

   public synchronized void updateStubHttpLifecycleByIndex(final int httpLifecycleIndex, final StubHttpLifecycle newStubHttpLifecycle) {
      final StubHttpLifecycle removedLifecycle = deleteStubHttpLifecycleByIndex(httpLifecycleIndex);
      if (ObjectUtils.isNotNull(removedLifecycle)) {
         stubHttpLifecycles.add(httpLifecycleIndex, newStubHttpLifecycle);
      }
   }

   public synchronized boolean isStubHttpLifecycleExistsByIndex(final int httpLifecycleIndex) {
      return stubHttpLifecycles.size() - 1 >= httpLifecycleIndex;
   }

   public synchronized StubHttpLifecycle deleteStubHttpLifecycleByIndex(final int httpLifecycleIndex) {
      return stubHttpLifecycles.remove(httpLifecycleIndex);
   }
}