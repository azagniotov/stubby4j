package org.stubby.yaml.stubs;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class NotFoundStubResponse extends StubResponse {

   public NotFoundStubResponse() {
      super();
   }

   @Override
   public StubResponseTypes getStubResponseType() {
      return StubResponseTypes.NOTFOUND;
   }
}