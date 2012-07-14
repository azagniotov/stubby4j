package org.stubby.client;

/**
 * @author Alexander Zagniotov
 * @since 7/14/12, 9:56 AM
 */
public final class Stubby4JResponse {

   private final int responseCode;
   private final String content;

   public Stubby4JResponse(final int responseCode, final String content) {
      this.responseCode = responseCode;
      this.content = content;
   }

   public int getResponseCode() {
      return responseCode;
   }

   public String getContent() {
      return content;
   }
}