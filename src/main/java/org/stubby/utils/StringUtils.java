package org.stubby.utils;

import java.util.Locale;

/**
 * @author Alexander Zagniotov
 * @since 10/27/12, 12:09 AM
 */
public final class StringUtils {

   private StringUtils() {

   }

   public static boolean isSet(final String toTest) {
      return (toTest != null && toTest.trim().length() > 0);
   }

   public static String toUpper(final String toUpper) {
      return toUpper.toUpperCase(Locale.US);
   }

   public static String toLower(final String toLower) {
      return toLower.toLowerCase(Locale.US);
   }
}
