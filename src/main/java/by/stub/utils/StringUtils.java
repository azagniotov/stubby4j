/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.utils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 10/27/12, 12:09 AM
 */
public final class StringUtils {

   public static final String UTF_8 = "UTF-8";

   private StringUtils() {

   }

   public static boolean isSet(final String toTest) {
      return (toTest != null && toTest.trim().length() > 0);
   }

   public static boolean isObjectSet(final Object toTest) {
      return (toTest != null && toTest.toString().trim().length() > 0);
   }

   public static String toUpper(final String toUpper) {
      return toUpper.toUpperCase(Locale.US);
   }

   public static String toLower(final String toLower) {
      return toLower.toLowerCase(Locale.US);
   }

   public static Charset utf8Charset() {
      return Charset.forName(StringUtils.UTF_8);
   }

   public static String inputStreamToString(final InputStream inputStream) {
      if (inputStream == null || !StringUtils.isSet(inputStream.toString())) {
         return null;
      }
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, StringUtils.UTF_8).useDelimiter("\\A").next();
   }

   public static String escapeHtmlEntities(final String toBeEscaped) {
      return toBeEscaped
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
   }
}
