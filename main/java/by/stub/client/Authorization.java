package by.stub.client;

import by.stub.annotations.CoberturaIgnore;
import by.stub.utils.ObjectUtils;

public final class Authorization {

   private final AuthorizationType type;
   private final String value;

   enum AuthorizationType {
      BASIC("Basic"),
      BEARER("Bearer");
      private final String type;

      AuthorizationType(final String type) {
         this.type = type;
      }

      public String asString() {
         return type;
      }
   }

   public Authorization(final AuthorizationType type, final String value) {
      this.type = type;
      this.value = value;
   }

   public AuthorizationType getType() {
      return type;
   }

   public String getValue() {
      return value;
   }

   public String asFullValue() {
       return String.format("%s %s", type.asString(), value);
   }

   @Override
   @CoberturaIgnore
   public final String toString() {
      final StringBuffer sb = new StringBuffer();
      sb.append("Authorization");
      sb.append("{type=").append(type);
      sb.append(", value=").append(value);
      sb.append('}');

      return sb.toString();
   }
}
