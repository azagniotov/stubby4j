package io.github.azagniotov.stubby4j.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Pattern;


public final class StringUtils {

    public static final String UTF_8 = "UTF-8";

    static final String NOT_PROVIDED = "Not provided";
    static final String FAILED = "Failed to retrieveLoadedStubs response content using relative path specified in 'file' during YAML parse time. Check terminal for warnings, and that response content exists in relative path specified in 'file'";

    private static final CharsetEncoder US_ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();

    private static final String TEMPLATE_TOKEN_LEFT = "<%";
    private static final String TEMPLATE_TOKEN_RIGHT = "%>";
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getEncoder();

    private StringUtils() {

    }

    public static boolean isSet(final String toTest) {
        return (ObjectUtils.isNotNull(toTest) && toTest.trim().length() > 0);
    }

    public static String trimIfSet(final String toTest) {
        return StringUtils.isSet(toTest) ? toTest.trim() : toTest;
    }

    public static boolean isNotSet(final String toTest) {
        return !isSet(toTest);
    }

    public static String toUpper(final String toUpper) {
        if (isNotSet(toUpper)) {
            return "";
        }
        return toUpper.toUpperCase(Locale.US);
    }

    public static String toLower(final String toLower) {
        if (isNotSet(toLower)) {
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
            return "Could not convert null input stream to string";
        }
        // Regex \A matches the beginning of input. This effectively tells Scanner to tokenize
        // the entire stream, from beginning to (illogical) next beginning.
        if (inputStream instanceof BufferedInputStream) {
            return new Scanner(inputStream, StringUtils.UTF_8).useDelimiter("\\A").next().trim();
        }

        return new Scanner(FileUtils.makeBuffered(inputStream), StringUtils.UTF_8).useDelimiter("\\A").next().trim();
    }

    public static String buildToken(final String propertyName, final int capturingGroupIdx) {
        return String.format("%s.%s", propertyName, capturingGroupIdx);
    }

    public static String replaceTokens(final byte[] stringBytes, final Map<String, String> tokensAndValues) {
        return replaceTokensInString(StringUtils.newStringUtf8(stringBytes), tokensAndValues);
    }

    public static String replaceTokensInString(String template, final Map<String, String> tokensAndValues) {
        for (final Map.Entry<String, String> entry : tokensAndValues.entrySet()) {
            final String regexifiedKey = String.format("%s\\s{0,}%s\\s{0,}%s", TEMPLATE_TOKEN_LEFT, entry.getKey(), TEMPLATE_TOKEN_RIGHT);
            template = template.replaceAll(regexifiedKey, entry.getValue());
        }
        return template;
    }

    public static boolean isTokenized(final String target) {
        return target.contains(TEMPLATE_TOKEN_LEFT);
    }

    public static boolean isNumeric(final String target) {
        for (char c : target.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String escapeHtmlEntities(final String toBeEscaped) {
        return toBeEscaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static String escapeSpecialRegexCharacters(final String toEscape) {
        return toEscape.replaceAll(Pattern.quote("{"), "\\\\{")
                .replaceAll(Pattern.quote("}"), "\\\\}")
                .replaceAll(Pattern.quote("["), "\\\\[")
                .replaceAll(Pattern.quote("]"), "\\\\]");
    }


    public static String constructUserAgentName() {
        final Package pkg = StringUtils.class.getPackage();
        final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
                pkg.getImplementationVersion() : "x.x.xx";

        return String.format("stubby4j/%s (HTTP stub client request)", implementationVersion);
    }

    public static String encodeBase64(final String toEncode) {
        return BASE_64_ENCODER.encodeToString(StringUtils.getBytesUtf8(toEncode));
    }

    public static int calculateStringLength(final String post) {
        if (StringUtils.isSet(post)) {
            return StringUtils.getBytesUtf8(post).length;
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

            return new String(objectBytes, StandardCharsets.UTF_8);
        } else {
            final String valueAsStr = fieldObject.toString().trim();

            return (!valueAsStr.equalsIgnoreCase("null") ? valueAsStr : "");
        }
    }

    static String join(final String[] segments, final String delimiter) {
        final StringJoiner stringJoiner = new StringJoiner(delimiter);
        for (final String segment : segments) {
            stringJoiner.add(segment);
        }
        return stringJoiner.toString();
    }

    static String repeat(final String repeatable, final int times) {
        if (isNotSet(repeatable) || times < 0) {
            return "";
        }
        return new String(new char[times]).replace("\0", repeatable);
    }

    static String decodeUrlEncodedQuotes(final String toBeFiltered) {
        return toBeFiltered.replaceAll("%22", "\"").replaceAll("%27", "'");
    }

    static String encodeSingleQuotes(final String toBeEncoded) {
        return toBeEncoded.replaceAll("'", "%27");
    }

    static String extractFilenameExtension(final String filename) {
        final int dotLocation = filename.lastIndexOf('.');

        return filename.substring(dotLocation);
    }

    static String trimSpacesBetweenCSVElements(final String toBeFiltered) {
        return toBeFiltered.replaceAll("\",\\s+\"", "\",\"").replaceAll(",\\s+", ",");
    }

    static String removeSquareBrackets(final String toBeFiltered) {
        return toBeFiltered.replaceAll("%5B|%5D|\\[|]", "");
    }

    static boolean isWithinSquareBrackets(final String toCheck) {
        return toCheck.startsWith("%5B") && toCheck.endsWith("%5D") || toCheck.startsWith("[") && toCheck.endsWith("]");
    }

    static String decodeUrlEncoded(final String toCheck) {
        if (toCheck.contains("%2B")) {
            return toCheck.replaceAll("%2B", " ").replaceAll("\\s+", " ");
        } else if (toCheck.contains("%20")) {
            return toCheck.replaceAll("%20", " ").replaceAll("\\s+", " ");
        } else if (toCheck.contains("+")) {
            return toCheck.replaceAll(Pattern.quote("+"), " ").replaceAll("\\s+", " ");
        }

        return toCheck;
    }

    static String pluralize(final long timeUnit) {
        return timeUnit == 1 ? "" : "s";
    }

    private static boolean isUSAscii(final String toTest) {
        return US_ASCII_ENCODER.canEncode(toTest);
    }
}
