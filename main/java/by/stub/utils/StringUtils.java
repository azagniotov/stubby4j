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

import by.stub.annotations.CoberturaIgnore;
import by.stub.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.IOException;
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
      String template = StringUtils.newStringUtf8(stringBytes);
      //template = template.replaceAll("<%\\s+", StringUtils.TEMPLATE_TOKEN_LEFT);
      //template = template.replaceAll("\\s+%>", StringUtils.TEMPLATE_TOKEN_RIGHT);
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

   public static String objectToString(final Object value) throws IOException {
      final String valueAsStr = (ObjectUtils.isNotNull(value) ? value.toString().trim() : "");

      return (!valueAsStr.equalsIgnoreCase("null") ? valueAsStr : "");
   }

   public static String determineObjectStringValue(final Object fieldObject) throws UnsupportedEncodingException {
      if (ObjectUtils.isNull(fieldObject)) {
         return NOT_PROVIDED;
      }

      if (fieldObject instanceof byte[]) {
         final byte[] objectBytes = (byte[]) fieldObject;
         final String toTest = StringUtils.newStringUtf8(objectBytes);

         if (!StringUtils.isUSAscii(toTest)) {
            return "Local binary file, not able to display";
         } else if (toTest.equals(StringUtils.FAILED)) {
            return StringUtils.FAILED;
         }

         return new String(objectBytes, StringUtils.UTF_8);
      }

      return fieldObject.toString();

   }
}
