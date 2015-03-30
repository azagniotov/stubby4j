package by.stub.yaml;

import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.StubRequest;

import java.io.File;
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

   private final Map<String, Object> fieldNameAndValues;
   private String url;
   private List<String> method;
   private String post;
   private Map<String, String> headers;
   private Map<String, String> query;
   private File file;

   StubRequestBuilder() {
      this.url = null;
      this.method = new ArrayList<>();
      this.post = null;
      this.file = null;
      this.headers = new LinkedHashMap<>();
      this.query = new LinkedHashMap<>();
      this.fieldNameAndValues = new HashMap<>();
   }

   @Override
   public void store(final String fieldName, final Object fieldValue) {
      fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
   }

   @Override
   public StubRequest build() throws Exception {
      ReflectionUtils.injectObjectFields(this, fieldNameAndValues);
      return new StubRequest(url, post, file, method, headers, query);
   }
}
