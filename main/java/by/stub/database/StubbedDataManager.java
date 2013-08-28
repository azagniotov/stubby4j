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

import by.stub.client.StubbyResponse;
import by.stub.http.StubbyHttpTransport;
import by.stub.utils.ObjectUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.RedirectStubResponse;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.eclipse.jetty.http.HttpMethods;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StubbedDataManager {

   private final File dataYaml;
   private final String dataYamlAbsolutePath;
   private final String dataYamlParentDirectory;
   private final List<StubHttpLifecycle> stubHttpLifecycles;
   private final StubbyHttpTransport stubbyHttpTransport;

   public StubbedDataManager(final File dataYaml, final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.dataYaml = dataYaml;
      this.dataYamlAbsolutePath = this.dataYaml.getAbsolutePath();
      this.dataYamlParentDirectory = this.dataYaml.getParent();
      this.stubHttpLifecycles = Collections.synchronizedList(stubHttpLifecycles);
      this.stubbyHttpTransport = new StubbyHttpTransport();
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

      if (stubResponse.isRecordingRequired()) {
         try {
            final StubbyResponse stubbyResponse = stubbyHttpTransport.getResponse(HttpMethods.GET, stubResponse.getBody());
            ReflectionUtils.injectObjectFields(stubResponse, "body", stubbyResponse.getContent());
         } catch (Exception e) {

         }
      }
      return stubResponse;
   }

   private synchronized StubHttpLifecycle getMatchedStubHttpLifecycle(final StubHttpLifecycle assertingLifecycle) {
      final int listIndex = stubHttpLifecycles.indexOf(assertingLifecycle);
      if (listIndex < 0) {
         return StubHttpLifecycle.NULL;
      }
      final StubHttpLifecycle foundStubHttpLifecycle = stubHttpLifecycles.get(listIndex);
      foundStubHttpLifecycle.setResourceId(listIndex);

      return foundStubHttpLifecycle;
   }

   public synchronized StubHttpLifecycle getMatchedStubHttpLifecycle(final int index) {

      if (!isStubHttpLifecycleExistsByIndex(index)) {
         return StubHttpLifecycle.NULL;
      }
      return stubHttpLifecycles.get(index);
   }

   public synchronized boolean resetStubHttpLifecycles(final List<StubHttpLifecycle> stubHttpLifecycles) {
      this.stubHttpLifecycles.clear();
      final boolean added = this.stubHttpLifecycles.addAll(stubHttpLifecycles);
      if (added) {
         updateResourceIDHeaders();
      }
      return added;
   }

   // Just a shallow copy that protects collection from modification, the points themselves are not copied
   public List<StubHttpLifecycle> getStubHttpLifecycles() {
      return new LinkedList<StubHttpLifecycle>(stubHttpLifecycles);
   }

   public File getDataYaml() {
      return dataYaml;
   }

   public synchronized Map<File, Long> getExternalFiles() {
      final Set<String> escrow = new HashSet<String>();
      final Map<File, Long> externalFiles = new HashMap<File, Long>();
      for (StubHttpLifecycle cycle : stubHttpLifecycles) {

         final File requestFile = cycle.getRequest().getRawFile();
         if (ObjectUtils.isNotNull(requestFile) && !escrow.contains(requestFile.getName())) {
            escrow.add(requestFile.getName());
            externalFiles.put(requestFile, requestFile.lastModified());
         }

         final File responseFile = cycle.getResponse().getRawFile();
         if (ObjectUtils.isNotNull(responseFile) && !escrow.contains(responseFile.getName())) {
            escrow.add(responseFile.getName());
            externalFiles.put(responseFile, responseFile.lastModified());
         }
      }

      return externalFiles;
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
      deleteStubHttpLifecycleByIndex(httpLifecycleIndex);
      stubHttpLifecycles.add(httpLifecycleIndex, newStubHttpLifecycle);
      updateResourceIDHeaders();
   }

   public synchronized boolean isStubHttpLifecycleExistsByIndex(final int httpLifecycleIndex) {
      return stubHttpLifecycles.size() - 1 >= httpLifecycleIndex;
   }

   public synchronized StubHttpLifecycle deleteStubHttpLifecycleByIndex(final int httpLifecycleIndex) {
      final StubHttpLifecycle removedLifecycle = stubHttpLifecycles.remove(httpLifecycleIndex);
      updateResourceIDHeaders();

      return removedLifecycle;
   }

   private void updateResourceIDHeaders() {
      for (int index = 0; index < stubHttpLifecycles.size(); index++) {
         stubHttpLifecycles.get(index).setResourceId(index);
      }
   }
}