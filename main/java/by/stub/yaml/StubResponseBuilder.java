package by.stub.yaml;

import by.stub.yaml.stubs.StubResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
final class StubResponseBuilder implements StubBuilder<StubResponse> {

   private String status;
   private String body;
   private byte[] file;
   private String latency;
   private Map<String, String> headers = new HashMap<String, String>();

   StubResponseBuilder() {

   }

   StubResponseBuilder withStatus(final String value) {
      this.status = value;

      return this;
   }

   StubResponseBuilder withBody(final String value) {
      this.body = value;

      return this;
   }

   StubResponseBuilder withHeaders(final Map<String, String> headers) {
      this.headers = headers;

      return this;
   }

   StubResponseBuilder withFile(final byte[] file) {
      this.file = file;

      return this;
   }

   StubResponseBuilder withLatency(final String latency) {
      this.latency = latency;

      return this;
   }


   @Override
   public StubResponse build() {
      final StubResponse stubResponse = new StubResponse(status, body, file, latency, headers);
      this.status = null;
      this.body = null;
      this.file = null;
      this.latency = null;
      this.headers = new HashMap<String, String>();

      return stubResponse;
   }
}
