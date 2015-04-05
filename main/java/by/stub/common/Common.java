package by.stub.common;

import org.eclipse.jetty.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

public final class Common {

   private Common() {

   }

   public static final Set<String> POSTING_METHODS = new HashSet<String>() {{
      add(HttpMethod.PUT.asString());
      add(HttpMethod.POST.asString());
   }};
   public static final String HEADER_APPLICATION_JSON = "application/json";
}
