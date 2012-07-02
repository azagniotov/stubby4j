package org.stubby.database;

import org.stubby.yaml.stubs.NullStubResponse;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import java.util.List;

/**
 * @author Alexander Zagniotov
 * @since 7/1/12, 11:22 PM
 */
public class DataStore {

   private final List<StubHttpLifecycle> stubHttpLifecycles;

   public DataStore(final List<StubHttpLifecycle> stubHttpLifecycles) {
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
