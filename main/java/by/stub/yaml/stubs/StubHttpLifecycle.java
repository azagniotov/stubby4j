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

package by.stub.yaml.stubs;


import by.stub.annotations.VisibleForTesting;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:21 AM
 */
@SuppressWarnings("unchecked")
public class StubHttpLifecycle {

   private final AtomicInteger responseSequencedIdCounter = new AtomicInteger(0);

   public static final StubHttpLifecycle NULL = null;
   private String httpLifeCycleAsYaml;
   private StubRequest request;
   private Object response;
   private String requestAsYaml;
   private String responseAsYaml;

   public StubHttpLifecycle() {
      response = StubResponse.newStubResponse();
   }

   public void setRequest(final StubRequest request) {
      this.request = request;
   }

   public void setResponse(final Object response) {
      this.response = response;
   }

   public StubRequest getRequest() {
      return request;
   }

   public StubResponse getResponse(final boolean incrementSequencedResponseId) {

      if (response instanceof StubResponse) {
         return (StubResponse) response;
      }

      final List<StubResponse> responses = (LinkedList<StubResponse>) response;
      if (responses.isEmpty()) {
         return StubResponse.newStubResponse();
      }

      if (incrementSequencedResponseId) {
         final int responseSequencedId = responseSequencedIdCounter.getAndIncrement();
         responseSequencedIdCounter.compareAndSet(responses.size(), 0);
         return responses.get(responseSequencedId);
      }

      return responses.get(responseSequencedIdCounter.get());
   }

   public int getNextSequencedResponseId() {
      return responseSequencedIdCounter.get();
   }

   public List<StubResponse> getAllResponses() {

      if (response instanceof StubResponse) {
         return new LinkedList<StubResponse>() {{
            add((StubResponse) response);
         }};
      }

      return (LinkedList<StubResponse>) response;
   }

   public boolean isAuthorizationRequired() {
      return request.isSecured();
   }

   @VisibleForTesting
   String getRawAuthorizationHttpHeader() {
      return request.getRawAuthorizationHttpHeader();
   }

   @VisibleForTesting
   String getStubbedAuthorizationHeaderValue(final StubHeaderTypes stubbedAuthorizationHeaderType) {
      return request.getStubbedAuthorizationHeaderValue(stubbedAuthorizationHeaderType);
   }

   public boolean isAssertingRequestUnauthorized(final StubHttpLifecycle assertingLifecycle) {
      final StubHeaderTypes stubbedAuthorizationHeaderType = request.getStubbedAuthorizationTypeHeader();
      if (stubbedAuthorizationHeaderType == StubHeaderTypes.AUTHORIZATION_TYPE_UNSUPPORTED) {
         return true;
      }
      final String stubbedAuthorizationHeaderValue = getStubbedAuthorizationHeaderValue(stubbedAuthorizationHeaderType);
      final String assertingAuthorizationHeaderValue = assertingLifecycle.getRawAuthorizationHttpHeader();

      return !stubbedAuthorizationHeaderValue.equals(assertingAuthorizationHeaderValue);
   }

   public String getResourceId() {
      return getAllResponses().get(0).getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER);
   }

   public String getHttpLifeCycleAsYaml() {
      return httpLifeCycleAsYaml;
   }

   public void setHttpLifeCycleAsYaml(final String httpLifeCycleAsYaml) {
      this.httpLifeCycleAsYaml = httpLifeCycleAsYaml;
   }

   /**
    * Do not remove this method if your IDE complains that it is unused.
    * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
    */
   public String getRequestAsYaml() {
      return requestAsYaml;
   }

   public void setRequestAsYaml(final String requestAsYaml) {
      this.requestAsYaml = requestAsYaml;
   }

   /**
    * Do not remove this method if your IDE complains that it is unused.
    * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
    */
   public String getResponseAsYaml() {
      return responseAsYaml;
   }

   public void setResponseAsYaml(final String responseAsYaml) {
      this.responseAsYaml = responseAsYaml;
   }

   public void setResourceId(final int listIndex) {
      for (final StubResponse response : getAllResponses()) {
         response.addResourceIDHeader(listIndex);
      }
   }

   public String getAjaxResponseContent(final StubTypes stubType, final String propertyName) throws Exception {
      switch (stubType) {
         case REQUEST:
            return StringUtils.objectToString(ReflectionUtils.getPropertyValue(request, propertyName));
         case RESPONSE:
            return StringUtils.objectToString(ReflectionUtils.getPropertyValue(getResponse(false), propertyName));
         default:
            return StringUtils.objectToString(ReflectionUtils.getPropertyValue(this, propertyName));
      }
   }

   public String getAjaxResponseContent(final String propertyName, final int sequencedResponseId) throws Exception {
      final List<StubResponse> allResponses = getAllResponses();
      final StubResponse sequencedResponse = allResponses.get(sequencedResponseId);
      return StringUtils.objectToString(ReflectionUtils.getPropertyValue(sequencedResponse, propertyName));
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof StubHttpLifecycle)) {
         return false;
      }

      final StubHttpLifecycle that = (StubHttpLifecycle) o;
      if (!request.equals(that.request)) {
         return false;
      }

      return true;
   }
}
