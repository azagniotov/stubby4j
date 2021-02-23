package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedList;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.okResponse;


public class StubHttpLifecycle implements ReflectableStub {

    private final AtomicInteger responseSequencedIdCounter = new AtomicInteger(0);

    private final String completeYAML;
    private final StubRequest request;
    private final Object response;
    private final String requestAsYAML;
    private final String responseAsYAML;
    private final String description;
    private final String uuid;

    private StubHttpLifecycle(
            final StubRequest request,
            final Object response,
            final String requestAsYAML,
            final String responseAsYAML,
            final String completeYAML,
            final String description,
            final String uuid) {
        this.request = request;
        this.response = response;
        this.requestAsYAML = requestAsYAML;
        this.responseAsYAML = responseAsYAML;
        this.completeYAML = completeYAML;
        this.description = description;
        this.uuid = uuid;
    }

    public StubRequest getRequest() {
        return request;
    }

    public StubResponse getResponse(final boolean incrementSequencedResponseId) {

        if (response instanceof StubResponse) {
            return (StubResponse) response;
        }

        final List<StubResponse> stubResponses = asCheckedLinkedList(this.response, StubResponse.class);
        if (stubResponses.isEmpty()) {
            return okResponse();
        }

        if (incrementSequencedResponseId) {
            final int responseSequencedId = responseSequencedIdCounter.getAndIncrement();
            responseSequencedIdCounter.compareAndSet(stubResponses.size(), 0);
            return stubResponses.get(responseSequencedId);
        }

        return stubResponses.get(responseSequencedIdCounter.get());
    }

    public int getNextSequencedResponseId() {
        return responseSequencedIdCounter.get();
    }

    public List<StubResponse> getResponses() {
        if (response instanceof StubResponse) {
            return new LinkedList<>(Collections.singletonList((StubResponse) response));
        }
        return asCheckedLinkedList(this.response, StubResponse.class);
    }

    boolean isAuthorizationRequired() {
        return request.isSecured();
    }

    @VisibleForTesting
    String getRawHeaderAuthorization() {
        return request.getRawHeaderAuthorization();
    }

    @VisibleForTesting
    String getStubbedHeaderAuthorization(final StubbableAuthorizationType stubbedAuthorizationHeaderType) {
        return request.getStubbedHeaderAuthorization(stubbedAuthorizationHeaderType);
    }

    boolean isIncomingRequestUnauthorized(final StubHttpLifecycle assertingLifecycle) {
        final String stubbedHeaderAuthorization = getStubbedHeaderAuthorization(request.getStubbedAuthorizationType());
        return !stubbedHeaderAuthorization.equals(assertingLifecycle.getRawHeaderAuthorization());
    }

    public String getResourceId() {
        return getResponses().get(0).getResourceIDHeader();
    }

    void setResourceId(final int resourceId) {
        getResponses().forEach(response -> response.addResourceIDHeader(resourceId));
    }

    /**
     * @see StubRequest#getUrl()
     */
    public String getUrl() {
        return request.getUrl();
    }

    public String getDescription() {
        return description;
    }

    public String getUUID() {
        return uuid;
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getCompleteYaml() {
        return completeYAML;
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getRequestAsYAML() {
        return requestAsYAML;
    }

    /**
     * Do not remove this method if your IDE complains that it is unused.
     * It is used by {@link ReflectionUtils} at runtime when fetching content for Ajax response
     */
    public String getResponseAsYAML() {
        return responseAsYAML;
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

    public int hashCode() {
        return this.request.hashCode();
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
        // The 'this' is actually the incoming asserting StubRequest, the 'that' is the stubbed one
        return this.request.equals(that.request);
    }

    public static class Builder {
        private StubRequest request;
        private Object response;
        private String completeYAML;
        private String requestAsYAML;
        private String responseAsYAML;
        private String description;
        private String uuid;

        public Builder() {
            this.request = null;
            this.response = okResponse();
            this.completeYAML = null;
            this.requestAsYAML = null;
            this.responseAsYAML = null;
            this.description = null;
            this.uuid = null;
        }

        public Builder withRequest(final StubRequest request) {
            this.request = request;

            return this;
        }

        public Builder withResponse(final Object response) {
            if (response instanceof StubResponse || response instanceof Collection) {
                this.response = response;
            } else {
                throw new IllegalArgumentException("Trying to set response of the wrong type");
            }

            return this;
        }

        public Builder withRequestAsYAML(final String requestAsYAML) {
            this.requestAsYAML = requestAsYAML;

            return this;
        }

        public Builder withResponseAsYAML(final String responseAsYAML) {
            this.responseAsYAML = responseAsYAML;

            return this;
        }

        public Builder withCompleteYAML(final String completeYAML) {
            this.completeYAML = completeYAML;

            return this;
        }

        public Builder withResourceId(final int resourceId) {
            getResponses().forEach(response -> response.addResourceIDHeader(resourceId));

            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;

            return this;
        }

        public Builder withUUID(final String uuid) {
            this.uuid = uuid;

            return this;
        }

        public StubHttpLifecycle build() {
            final StubHttpLifecycle stubHttpLifecycle =
                    new StubHttpLifecycle(
                            request,
                            response,
                            requestAsYAML,
                            responseAsYAML,
                            completeYAML,
                            description,
                            uuid);

            this.request = null;
            this.response = okResponse();
            this.completeYAML = null;
            this.requestAsYAML = null;
            this.responseAsYAML = null;
            this.description = null;
            this.uuid = null;

            return stubHttpLifecycle;
        }

        private List<StubResponse> getResponses() {
            if (response instanceof StubResponse) {
                return new LinkedList<>(Collections.singletonList((StubResponse) response));
            }
            return asCheckedLinkedList(this.response, StubResponse.class);
        }
    }
}
