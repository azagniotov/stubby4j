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

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Alexander Zagniotov
 * @since 10/27/12, 12:09 AM
 */
public final class StringUtils {

   public static final String NOT_PROVIDED = "Not provided";

   private StringUtils() {

   }

   public static final int PAD_LIMIT = 8192;
   public static final String TEMPLATE_TOKEN_LEFT = "<%";
   public static final String TEMPLATE_TOKEN_RIGHT = "%>";
   public static final String UTF_8 = "UTF-8";
   public static final String FAILED = "Failed to load response content using relative path specified in 'file' during YAML parse time. Check terminal for warnings, and that response content exists in relative path specified in 'file'";

   private static final CharsetEncoder US_ASCII_ENCODER = Charset.forName("US-ASCII").newEncoder();

   public static boolean isUSAscii(final String toTest) {
      return US_ASCII_ENCODER.canEncode(toTest);
   }

   public static boolean isSet(final String toTest) {
      return (ObjectUtils.isNotNull(toTest) && toTest.trim().length() > 0);
   }

   public static String toUpper(final String toUpper) {
      if (!isSet(toUpper)) {
         return "";
      }
      return toUpper.toUpperCase(Locale.US);
   }

   public static String toLower(final String toLower) {
      if (!isSet(toLower)) {
         return "";
      }
      return toLower.toLowerCase(Locale.US);
   }

   public static Charset charsetUTF8() {
      return Charset.forName(StringUtils.UTF_8);
   }

   public static String newStringUtf8(final byte[] bytes) {
      return new String(bytes, StringUtils.charsetUTF8());
   }

   public static byte[] getBytesUtf8(final String string) {
      return string.getBytes(StringUtils.charsetUTF8());
   }

   public static String inputStreamToString(final InputStream inputStream) {
      if (ObjectUtils.isNull(inputStream)) {
         return "Could not convert empty or null input stream to string";
      }
      // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
      // the entire stream, from beginning to (illogical) next beginning.
      return new Scanner(inputStream, StringUtils.UTF_8).useDelimiter("\\A").next().trim();
   }

   public static String buildToken(final String propertyName, final int capturingGroupIdx) {
      return String.format("%s.%s", propertyName, capturingGroupIdx);
   }

   public static String replaceTokens(final byte[] stringBytes, Map<String, String> tokensAndValues) {
      return replaceTokensInString(StringUtils.newStringUtf8(stringBytes), tokensAndValues);
   }

   public static String replaceTokensInString(String template, Map<String, String> tokensAndValues) {
      for (Map.Entry<String, String> entry : tokensAndValues.entrySet()) {
         final String regexifiedKey = String.format("%s\\s{0,}%s\\s{0,}%s", StringUtils.TEMPLATE_TOKEN_LEFT, entry.getKey(), StringUtils.TEMPLATE_TOKEN_RIGHT);
         template = template.replaceAll(regexifiedKey, entry.getValue());
      }
      return template;
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

      if (toCheck.startsWith("%5B") && toCheck.endsWith("%5D")) {
         return true;
      }

      return toCheck.startsWith("[") && toCheck.endsWith("]");
   }

   public static String decodeUrlEncodedQuotes(final String toBeFiltered) {
      return toBeFiltered.replaceAll("%22", "\"").replaceAll("%27", "'");
   }

   public static String encodeSingleQuotes(final String toBeEncoded) {
      return toBeEncoded.replaceAll("'", "%27");
   }

   public static String extractFilenameExtension(final String filename) {
      final int dotLocation = filename.lastIndexOf('.');

      return filename.substring(dotLocation);
   }

   @CoberturaIgnore
   public static String constructUserAgentName() {
      final Package pkg = StringUtils.class.getPackage();
      final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
         pkg.getImplementationVersion() : "x.x.xx";

      return String.format("stubby4j/%s (HTTP stub client request)", implementationVersion);
   }

   public static String encodeBase64(final String toEncode) {
      return Base64.encodeBase64String(StringUtils.getBytesUtf8(toEncode));
   }

   public static int calculateStringLength(String post) {
      if (StringUtils.isSet(post)) {
         return post.getBytes(StringUtils.charsetUTF8()).length;
      }
      return 0;
   }

   public static String objectToString(final Object fieldObject) {
      if (ObjectUtils.isNull(fieldObject)) {
         return NOT_PROVIDED;
      }

      if (fieldObject instanceof byte[]) {
         final byte[] objectBytes = (byte[]) fieldObject;
         final String toTest = StringUtils.newStringUtf8(objectBytes);

         if (!StringUtils.isUSAscii(toTest)) {
            return "Loaded file is binary - it's content is not displayable";
         } else if (toTest.equals(StringUtils.FAILED)) {
            return StringUtils.FAILED;
         }

         try {
            return new String(objectBytes, StringUtils.UTF_8);
         } catch (UnsupportedEncodingException e) {
            return new String(objectBytes);
         }
      } else {
         final String valueAsStr = (ObjectUtils.isNotNull(fieldObject) ? fieldObject.toString().trim() : "");

         return (!valueAsStr.equalsIgnoreCase("null") ? valueAsStr : "");
      }
   }

   /*
      http://commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html
    */
   public static String join(final String[] array, final char separator) {
      if (array == null) {
         return null;
      }
      final int startIndex = 0;
      final int endIndex = array.length;
      final int noOfItems = endIndex - startIndex;
      if (noOfItems <= 0) {
         return "";
      }
      final StringBuilder buf = new StringBuilder(noOfItems * 16);
      for (int i = startIndex; i < endIndex; i++) {
         if (i > startIndex) {
            buf.append(separator);
         }
         if (array[i] != null) {
            buf.append(array[i]);
         }
      }
      return buf.toString();
   }

   /*
      http://commons.apache.org/proper/commons-lang/apidocs/src-html/org/apache/commons/lang3/StringUtils.html
    */
   public static String repeat(final String str, final int repeat) {
      // Performance tuned for 2.0 (JDK1.4)

      if (str == null) {
         return null;
      }
      if (repeat <= 0) {
         return "";
      }
      final int inputLength = str.length();
      if (repeat == 1 || inputLength == 0) {
         return str;
      }
      if (inputLength == 1 && repeat <= PAD_LIMIT) {
         return repeat(str.charAt(0), repeat);
      }

      final int outputLength = inputLength * repeat;
      switch (inputLength) {
         case 1:
            return repeat(str.charAt(0), repeat);
         case 2:
            final char ch0 = str.charAt(0);
            final char ch1 = str.charAt(1);
            final char[] output2 = new char[outputLength];
            for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
               output2[i] = ch0;
               output2[i + 1] = ch1;
            }
            return new String(output2);
         default:
            final StringBuilder buf = new StringBuilder(outputLength);
            for (int i = 0; i < repeat; i++) {
               buf.append(str);
            }
            return buf.toString();
      }
   }

   private static String repeat(final char ch, final int repeat) {
      final char[] buf = new char[repeat];
      for (int i = repeat - 1; i >= 0; i--) {
         buf[i] = ch;
      }
      return new String(buf);
   }
}
