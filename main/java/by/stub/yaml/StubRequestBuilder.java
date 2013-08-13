package by.stub.yaml;

import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.StubRequest;
import org.eclipse.jetty.http.HttpMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
public final class StubRequestBuilder implements StubBuilder<StubRequest> {

   private String url = null;
   private List<String> methods = new ArrayList<String>();
   private String post = null;
   private Map<String, String> headers = new HashMap<String, String>();
   private Map<String, String> query = new LinkedHashMap<String, String>();
   private byte[] fileBytes;

   public StubRequestBuilder() {

   }

   public StubRequestBuilder withMethod(final List<String> value) {
      this.methods = value;

      return this;
   }

   public StubRequestBuilder withUrl(final String value) {
      this.url = value;

      return this;
   }

   public StubRequestBuilder withHeaders(final Map<String, String> headers) {
      this.headers = headers;

      return this;
   }

   public StubRequestBuilder withPost(final String post) {
      this.post = post;

      return this;
   }

   public StubRequestBuilder withFile(final byte[] fileBytes) {
      this.fileBytes = fileBytes;

      return this;
   }

   public StubRequestBuilder withQuery(final Map<String, String> query) {
      this.query = query;

      return this;
   }

   @Override
   public StubRequest build() {
      final StubRequest stubRequest = new StubRequest();

      try {
         ReflectionUtils.setPropertyValue(stubRequest, "post", post);
         ReflectionUtils.setPropertyValue(stubRequest, "file", fileBytes);
         ReflectionUtils.setPropertyValue(stubRequest, "url", url);
         ReflectionUtils.setPropertyValue(stubRequest, "headers", new HashMap<String, String>(headers));
         ReflectionUtils.setPropertyValue(stubRequest, "query", new LinkedHashMap<String, String>(query));
         ReflectionUtils.setPropertyValue(stubRequest, "method", methods);
      } catch (Exception ignored) {}

      this.url = null;
      this.methods = new ArrayList<String>();
      this.post = null;
      this.fileBytes = null;
      this.headers = new HashMap<String, String>();
      this.query = new LinkedHashMap<String, String>();

      return stubRequest;
   }
}
