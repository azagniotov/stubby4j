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

package io.github.azagniotov.stubby4j.yaml.stubs;


import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;

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

    private String completeYAML;
    private StubRequest request;
    private Object response;
    private String requestAsYAML;
    private String responseAsYAML;

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

    public List<StubResponse> getResponses() {

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
    String getStubbedAuthorizationHeaderValue(final StubAuthorizationTypes stubbedAuthorizationHeaderType) {
        return request.getStubbedAuthorizationHeaderValue(stubbedAuthorizationHeaderType);
    }

    public boolean isIncomingRequestUnauthorized(final StubHttpLifecycle assertingLifecycle) {
        final String stubbedAuthorizationHeaderValue = getStubbedAuthorizationHeaderValue(request.getStubbedAuthorizationTypeHeader());
        return !stubbedAuthorizationHeaderValue.equals(assertingLifecycle.getRawAuthorizationHttpHeader());
    }

    public String getResourceId() {
        return getResponses().get(0).getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER);
    }

    /**
     * @see StubRequest#getUrl()
     */
    public String getUrl() {
        return request.getUrl();
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getCompleteYAML() {
        return completeYAML;
    }

    public void setCompleteYAML(final String completeYAML) {
        this.completeYAML = completeYAML;
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getRequestAsYAML() {
        return requestAsYAML;
    }

    public void setRequestAsYAML(final String requestAsYAML) {
        this.requestAsYAML = requestAsYAML;
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getResponseAsYAML() {
        return responseAsYAML;
    }

    public void setResponseAsYAML(final String responseAsYAML) {
        this.responseAsYAML = responseAsYAML;
    }

    public void setResourceId(final int resourceId) {
        getResponses().forEach(response -> response.addResourceIDHeader(resourceId));
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
        final List<StubResponse> allResponses = getResponses();
        final StubResponse sequencedResponse = allResponses.get(sequencedResponseId);
        return StringUtils.objectToString(ReflectionUtils.getPropertyValue(sequencedResponse, propertyName));
    }

    @Override
    @CoberturaIgnore
    public int hashCode() {
        return this.request.hashCode();
    }

    @Override
    @CoberturaIgnore
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StubHttpLifecycle)) {
            return false;
        }

        final StubHttpLifecycle that = (StubHttpLifecycle) o;
        // The 'this' is actually the incoming asserting StubRequest, the 'that' is the stubbed one
        return this.request.equals(that.request);
    }
}
