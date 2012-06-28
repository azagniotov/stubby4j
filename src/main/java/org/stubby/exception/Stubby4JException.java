package org.stubby.exception;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 8:38 PM
 */
public final class Stubby4JException extends RuntimeException {
   private static final long serialVersionUID = 8L;

   public Stubby4JException(final String strMessage) {
      super(strMessage);
   }
}