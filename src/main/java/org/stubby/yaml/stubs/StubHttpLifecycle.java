package org.stubby.yaml.stubs;

import org.stubby.yaml.YamlParentNodes;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:21 AM
 */
public final class StubHttpLifecycle {

   private final StubRequest request;
   private final StubResponse response;
   private YamlParentNodes currentlyPopulated = YamlParentNodes.REQUEST;

   public StubHttpLifecycle(final StubRequest request, final StubResponse response) {
      this.request = request;
      this.response = response;
   }

   public StubRequest getRequest() {
      return request;
   }

   public StubResponse getResponse() {
      return response;
   }

   public YamlParentNodes getCurrentlyPopulated() {
      return currentlyPopulated;
   }

   public void setCurrentlyPopulated(final YamlParentNodes currentlyPopulated) {
      this.currentlyPopulated = currentlyPopulated;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubHttpLifecycle)) return false;

      final StubHttpLifecycle that = (StubHttpLifecycle) o;

      if (!request.equals(that.request)) return false;
      if (!response.equals(that.response)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = request.hashCode();
      result = 31 * result + response.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return "StubHttpLifecycle{" +
            "request=" + request +
            ", response=" + response +
            '}';
   }
}
