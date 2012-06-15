package org.stubby.yaml;

/**
 * @author Alexander Zagniotov
 * @since 6/15/12, 8:41 AM
 */
public enum YamlParentNodes {
   REQUEST("request"),
   RESPONSE("response"),
   HEADERS("headers"),
   HTTPLIFECYCLE("httplifecycle");

   private final String description;

   private YamlParentNodes(final String description) {
      this.description = description;
   }

   public String desc() {
      return description.toLowerCase();
   }
}