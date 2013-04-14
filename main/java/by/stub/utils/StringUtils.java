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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Locale;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 10/27/12, 12:09 AM
 */
public final class StringUtils {

   public static final String UTF_8 = "UTF-8";

   private static final CharsetEncoder US_ASCII_ENCODER = Charset.forName("US-ASCII").newEncoder();

   private StringUtils() {

   }

   public static boolean isUSAscii(final String toTest) {
      return US_ASCII_ENCODER.canEncode(toTest);
   }

   public static boolean isSet(final String toTest) {
      return (toTest != null && toTest.trim().length() > 0);
   }

   public static boolean isObjectSet(final Object toTest) {
      return (toTest != null && toTest.toString().trim().length() > 0);
   }

   public static String toUpper(final String toUpper) {
      if (!isSet(toUpper)) {
         return null;
      }
      return toUpper.toUpperCase(Locale.US);
   }

   public static String toLower(final String toLower) {
      if (!isSet(toLower)) {
         return null;
      }
      return toLower.toLowerCase(Locale.US);
   }

   public static Charset utf8Charset() {
      return Charset.forName(StringUtils.UTF_8);
   }

   public static String utf8String(final byte[] bytes) {
      return new String(bytes, StringUtils.utf8Charset());
   }

   public static String inputStreamToString(final InputStream inputStream) {
      if (inputStream == null || !StringUtils.isSet(inputStream.toString())) {
         return "Could not convert empty or null input stream to string";
      }
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, StringUtils.UTF_8).useDelimiter("\\A").next().trim();
   }

   public static Reader constructReader(final String filePath) throws FileNotFoundException {
      final InputStream is = new FileInputStream(filePath);
      return new InputStreamReader(is, StringUtils.utf8Charset());
   }

   public static String escapeHtmlEntities(final String toBeEscaped) {
      return toBeEscaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   public static String trimSpacesBetweenCSVElements(final String toBeFiltered) {
      return toBeFiltered.replaceAll("\",\\s+\"", "\",\"").replaceAll(",\\s+", ",");
   }

   public static String removeSquareBrackets(final String toBeFiltered) {
      return toBeFiltered.replaceAll("%5B|%5D|\\[|]", "");
   }

   public static boolean isWithinSquareBrackets(final String toCheck) {
      final boolean isEncodedBrackets = toCheck.startsWith("%5B") && toCheck.endsWith("%5D");
      final int lastCharIdx = toCheck.length() - 1;
      final boolean isBrackets = toCheck.charAt(0) == '[' && toCheck.charAt(lastCharIdx) == ']';

      return isEncodedBrackets || isBrackets;
   }

   public static String decodeUrlEncodedQuotes(final String toBeFiltered) {
      return toBeFiltered.replaceAll("%22", "\"");
   }

   public static String constructUserAgentName() {
      final Package pkg = StringUtils.class.getPackage();
      final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
         pkg.getImplementationVersion() : "x.x.xx";

      return String.format("stubby4j/%s (HTTP stub client request)", implementationVersion);
   }
}
