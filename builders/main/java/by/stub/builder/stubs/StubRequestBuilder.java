package by.stub.builder.stubs;

import by.stub.yaml.stubs.StubRequest;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
public final class StubRequestBuilder {

   private String url = null;
   private ArrayList<String> methods = new ArrayList<String>();
   private String post = null;
   private byte[] file = null;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> query = new HashMap<String, String>();

   public StubRequestBuilder() {

   }

   public StubRequestBuilder withMethod(final String value) {
      this.methods.add(value);

      return this;
   }

   public StubRequestBuilder withMethodGet() {
      this.methods.add("GET");

      return this;
   }

   public StubRequestBuilder withMethodPut() {
      this.methods.add("PUT");

      return this;
   }

   public StubRequestBuilder withMethodPost() {
      this.methods.add("POST");

      return this;
   }

   public StubRequestBuilder withMethodHead() {
      this.methods.add("HEAD");

      return this;
   }

   public StubRequestBuilder withUrl(final String value) {
      this.url = value;

      return this;
   }

   public StubRequestBuilder withHeaders(final String key, final String value) {
      this.headers.put(key, value);

      return this;
   }

   public StubRequestBuilder withPost(final String post) {
      this.post = post;

      return this;
   }

   public StubRequestBuilder withQuery(final String key, final String value) {
      this.query.put(key, value);

      return this;
   }

   public StubRequest build() {
      final StubRequest stubRequest = new StubRequest();

      stubRequest.setPost(post);
      stubRequest.setUrl(url);
      stubRequest.setHeaders(new HashMap<String, String>(headers));
      stubRequest.setQuery(new HashMap<String, String>(query));

      try {
         final Field field = stubRequest.getClass().getDeclaredField("method");
         AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
               field.setAccessible(true);
               return true;
            }
         });
         field.set(stubRequest, this.methods);
      } catch (Exception ignored) {

      }

      this.url = null;
      this.methods = new ArrayList<String>();
      this.post = null;
      this.file = null;
      this.headers = new HashMap<String, String>();
      this.query = new HashMap<String, String>();

      return stubRequest;
   }
}
