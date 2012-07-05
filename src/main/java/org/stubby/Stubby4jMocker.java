package org.stubby;

import org.eclipse.jetty.http.HttpMethods;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/4/12, 11:00 PM
 */
final class Stubby4jMocker {

   private final List<StubHttpLifecycle> httpLifecycles = new LinkedList<StubHttpLifecycle>();

   private StubRequest stubRequest;
   private StubResponse stubResponse;

   Stubby4jMocker() {

   }

   void whenRequest() {
      stubRequest = new StubRequest();
   }

   void thenResponse() {
      stubResponse = new StubResponse();
   }

   void hasMethod(final String method) {
      stubRequest.setMethod(method);
   }

   void hasUri(final String uri) {
      stubRequest.setUrl(uri);
   }

   void hasPostBody(final String postBody) {
      stubRequest.setPostBody(postBody);
   }

   void withStatus(final String status) {
      stubResponse.setStatus(status);
   }

   void withBody(final String body) {
      stubResponse.setBody(body);
   }

   void withHeader(final String header, final String value) {
      stubResponse.getHeaders().put(header, value);
   }

   void configure() {
      httpLifecycles.add(new StubHttpLifecycle(stubRequest, stubResponse));
   }

   void clear() {
      httpLifecycles.clear();
   }

   public Map<String, String> simulateGetOnURI(final String uri) {
      final Map<String, String> results = new HashMap<String, String>();

      final StubRequest assertionStubRequest = new StubRequest();
      assertionStubRequest.setMethod(HttpMethods.GET);
      assertionStubRequest.setUrl(uri);

      if (assertStubHttpCycle(results, assertionStubRequest)) {
         return results;
      }

      final String errorMessage = String.format("No response found for GET request on %s", uri);
      handleNotFound(results, errorMessage);

      return results;
   }

   public Map<String, String> simulatePostOnURI(final String uri, final String postData) {
      final Map<String, String> results = new HashMap<String, String>();

      final StubRequest assertionStubRequest = new StubRequest();
      assertionStubRequest.setMethod(HttpMethods.POST);
      assertionStubRequest.setUrl(uri);
      assertionStubRequest.setPostBody(postData);

      if (assertStubHttpCycle(results, assertionStubRequest)) {
         return results;
      }

      final String errorMessage = String.format("No response found for POST request on %s with post data %s", uri, postData);
      handleNotFound(results, errorMessage);

      return results;
   }

   private void handleNotFound(final Map<String, String> results, final String message) {
      results.put(Stubby4J.KEY_STATUS, "404");
      results.put(Stubby4J.KEY_RESPONSE, message);
   }

   private boolean assertStubHttpCycle(final Map<String, String> results, final StubRequest assertionStubRequest) {
      final StubHttpLifecycle assertionStubHttpLifecycle = new StubHttpLifecycle(assertionStubRequest, new StubResponse());

      if (httpLifecycles.contains(assertionStubHttpLifecycle)) {
         final int indexOf = httpLifecycles.indexOf(assertionStubHttpLifecycle);
         StubResponse foundStubResponse = httpLifecycles.get(indexOf).getResponse();

         results.put(Stubby4J.KEY_STATUS, foundStubResponse.getStatus());
         results.put(Stubby4J.KEY_RESPONSE, foundStubResponse.getBody());
         results.put(Stubby4J.KEY_RESPONSE_HEADERS, foundStubResponse.getHeaders().toString());

         return true;
      }
      return false;
   }
}