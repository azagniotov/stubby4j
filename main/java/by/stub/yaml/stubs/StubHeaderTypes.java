package by.stub.yaml.stubs;

public enum StubHeaderTypes {

   AUTHORIZATION_BASIC("authorization-basic"),
   AUTHORIZATION_BEARER("authorization-bearer"),
   AUTHORIZATION_TYPE_UNSUPPORTED("authorization-unsupported");

   private final String name;
   StubHeaderTypes(final String name) {
      this.name = name;
   }

   public String asString() {
      return name;
   }
}
