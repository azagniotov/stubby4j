package org.stubby.yaml.stubs;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:12 AM
 */
public final class StubHeader {

   private final String param;
   private final String value;

   public StubHeader(final String param, final String value) {
      this.param = param;
      this.value = value;
   }

   public String getParam() {
      return param;
   }

   public String getValue() {
      return value;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof StubHeader)) return false;

      final StubHeader that = (StubHeader) o;

      if (!param.equals(that.param)) return false;
      if (!value.equals(that.value)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = param.hashCode();
      result = 31 * result + value.hashCode();
      return result;
   }
}
