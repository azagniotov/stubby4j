package by.stub.yaml;

import by.stub.yaml.stubs.StubRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
final class StubRequestBuilder implements StubBuilder<StubRequest> {

   private String url = null;
   private List<String> methods = new ArrayList<String>();
   private String post = null;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> query = new LinkedHashMap<String, String>();
   private byte[] fileBytes;

   StubRequestBuilder() {

   }

   StubRequestBuilder withMethod(final List<String> value) {
      this.methods = value;

      return this;
   }

   StubRequestBuilder withUrl(final String value) {
      this.url = value;

      return this;
   }

   StubRequestBuilder withHeaders(final Map<String, String> headers) {
      this.headers = headers;

      return this;
   }

   StubRequestBuilder withPost(final String post) {
      this.post = post;

      return this;
   }

   StubRequestBuilder withFile(final byte[] fileBytes) {
      this.fileBytes = fileBytes;

      return this;
   }

   StubRequestBuilder withQuery(final Map<String, String> query) {
      this.query = query;

      return this;
   }

   @Override
   public StubRequest build() {
      final StubRequest stubRequest = new StubRequest(url, post, fileBytes, methods, headers, query);

      this.url = null;
      this.methods = new ArrayList<String>();
      this.post = null;
      this.fileBytes = null;
      this.headers = new HashMap<String, String>();
      this.query = new LinkedHashMap<String, String>();

      return stubRequest;
   }
}
