package io.github.azagniotov.stubby4j.common;

import io.github.azagniotov.stubby4j.http.HttpMethodExtended;
import org.eclipse.jetty.http.HttpMethod;

import java.util.HashSet;
import java.util.Set;

public final class Common {

   public static final Set<String> POSTING_METHODS = new HashSet<String>() {{
      add(HttpMethod.PUT.asString());
      add(HttpMethod.POST.asString());
      // PATCH is not a part of org.eclipse.jetty.http.HttpMethod
      add(HttpMethodExtended.PATCH.asString());
   }};
   public static final String HEADER_APPLICATION_JSON = "application/json";
   public static final String HEADER_APPLICATION_XML = "application/xml";

   private Common() {

   }
}
