package by.stub.yaml.stubs.matcher;


import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonBodyMatcher {

   /*
    * Matching rules:
    *  - A null value in a pattern only matches a null or missing request value
    *
    *  - A string value in a pattern is treated as a regular expression and can match strings,
    *    booleans and numbers in the request (they are first converted to strings).
    *
    *  - A number value in a pattern can only match numbers in the request and must match exactly.
    *
    *  - A boolean value in a pattern can only match boolean in the request and must match exactly.
    *
    *  - An array in a pattern can only match an array in the request. See 'matchArray()' for more detail.
    *
    *  - An object in the pattern can only match an object in the request. See 'matchObject()' for more detail.
    */

   public boolean matches(String pattern, String request) {
      try {
         Object patternObject = deserialize(pattern, Object.class);
         Object requestObject = deserialize(request, Object.class);
         return matchValue(patternObject, requestObject);
      } catch (Exception e) {
         return false;
      }
   }

   @SuppressWarnings("unchecked")
   private boolean matchValue(Object pattern, Object request) { // TODO: add better debugging information
      if (pattern == null) {
         if (request == null) {
            return matchSuccess(); // only match if both are null
         } else {
            return matchFailure("Expected null value");
         }
      } else if (pattern instanceof String) {
         if (request instanceof String // allow regexp to match any scalar value
            || request instanceof Number
            || request instanceof Boolean) {
            if (request.toString().matches(pattern.toString())) { // assume pattern is a regular expression
               return matchSuccess();
            } else {
               return matchFailure(String.format("Expected '%s' to match '%s'", request, pattern));
            }
         } else {
            return matchFailure("Scalar value (string, number or boolean) expected");
         }
      } else if (pattern instanceof Number) {
         if (request instanceof Number) {
            if (pattern.equals(request)) {
               return matchSuccess();
            } else {
               return matchFailure(String.format("Expected %s, was %s", pattern, request));
            }
         } else {
            return matchFailure("Number expected");
         }
      } else if (pattern instanceof Boolean) {
         if (request instanceof Boolean) {
            if (pattern.equals(request)) {
               return matchSuccess();
            } else {
               return matchFailure(String.format("Expected %s, was %s", pattern, request));
            }
         } else {
            return matchFailure("Boolean expected");
         }
      } else if (pattern instanceof List) {
         if (request instanceof List) {
            return matchArray((List<Object>) pattern, (List<Object>) request);
         } else {
            return matchFailure("Array expected");
         }
      } else if (pattern instanceof Map) {
         if (request instanceof Map) {
            return matchObject((Map<String, Object>) pattern, (Map<String, Object>) request); // recursively match objects
         } else {
            return matchFailure("Object expected");
         }
      } else {
         throw new RuntimeException("Unexpected type in pattern: " + pattern.getClass());
      }
   }


   private Object deserialize(final String postBody, final Class type) throws IOException {
      return new ObjectMapper().readValue(postBody, type);
   }

   private boolean matchObject(Map<String, Object> pattern, Map<String, Object> request) {
      for (String key : pattern.keySet()) {
         if (!matchValue(pattern.get(key), request.get(key)))
            return false;
      }
      return true;
   }

   private boolean matchArray(List<Object> pattern, List<Object> request) {
      int r = 0; // current search position in request array
      for (int p = 0; p < pattern.size(); p++) {
         while (r < request.size() && !matchValue(pattern.get(p), request.get(r))) {
            r++;
         }
         if (r == request.size()) { // reached end of request and no match
            return matchFailure("Matching array element not found");
         }
      }
      return matchSuccess(); // empty pattern or all matched
   }

   private boolean matchFailure(String reason) {
      return false;
   }

   private boolean matchSuccess() {
      return true;
   }

}
