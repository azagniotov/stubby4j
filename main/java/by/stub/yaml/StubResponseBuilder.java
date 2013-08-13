package by.stub.yaml;

import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.StubResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
final class StubResponseBuilder implements StubBuilder<StubResponse> {

   private final Map<String, Object> fieldNameAndValues;
   private String status;
   private String body;
   private File file;
   private String latency;
   private Map<String, String> headers = new HashMap<String, String>();

   StubResponseBuilder() {
      this.status = null;
      this.body = null;
      this.file = null;
      this.latency = null;
      this.headers = new HashMap<String, String>();
      this.fieldNameAndValues = new HashMap<String, Object>();
   }

   @Override
   public void store(final String fieldName, final Object fieldValue) {
      fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
   }

   @Override
   public StubResponse build()  throws Exception {
      ReflectionUtils.setValuesToObjectFields(this, fieldNameAndValues);
      return new StubResponse(status, body, file, latency, headers);
   }
}
