package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.caching.Cache;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.github.azagniotov.stubby4j.utils.StringUtils.buildToken;

enum RegexParser {

    INSTANCE;

    // Pattern.MULTILINE changes the behavior of '^' and '$' characters by telling Java to accept the
    // anchors '^' and '$' to match at the start and end of each line (otherwise they only match at the
    // start/end of the entire string). In other words, if the string contains newlines, you can choose
    // for '^' and '$' to match at the start and end of any logical line, not just the start and end of
    // the whole string, by setting the MULTILINE flag.
    //
    // You need to make sure that you regex pattern covers both \r (carriage return) and \n (linefeed).
    // It is achievable by using symbol '\s+', which covers both \r (carriage return) and \n (linefeed).
    static final int[] REGEX_FLAGS = new int[]{Pattern.MULTILINE, Pattern.DOTALL};
    /**
     * ASCII character decimal values
     * '$'    - 36
     * '('    - 40
     * ')'    - 41
     * '*'    - 42
     * '+'    - 43
     * '.'    - 46
     * '?'    - 63
     * '['    - 91
     * ']'    - 93
     * '\'    - 92
     * '^'    - 94
     * '{'    - 123
     * '|'    - 124
     * '}'    - 125
     */

    @VisibleForTesting
    static final char[] REGEX_CHARS = new char[]{'$', '(', ')', '*', '+', '.', '?', '[', ']', '\\', '^', '{', '|', '}'};

    @VisibleForTesting
    static final Cache<Integer, Pattern> REGEX_PATTERN_CACHE = Cache.regexPatternCache();
    private static final boolean[] SPECIAL_CHARS;
    private static final int REGEX_CHAR_LENGTH_THRESHOLD = 2;

    static {
        SPECIAL_CHARS = new boolean[127];
        for (final char c : REGEX_CHARS) {
            SPECIAL_CHARS[c] = true;
        }
    }

    /**
     * A very primitive way to test if string is *maybe* a regex pattern, instead of compiling a Pattern
     *
     * @param pattern to check for presence of regex special characters
     */
    static boolean potentialRegex(final String pattern) {
        return potentialRegex(pattern, REGEX_CHAR_LENGTH_THRESHOLD);
    }

    private static boolean potentialRegex(final String pattern, int threshold) {

        char[] chars = pattern.toCharArray();

        if (chars.length < threshold) {
            return false;
        }

        if (chars[0] == '^' || chars[chars.length - 1] == '$') {
            return true;
        }

        for (final char currentChar : chars) {

            if (threshold == 0) {
                return true;
            }

            if ((currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= 'a' && currentChar <= 'z')) {
                continue;
            }

            if (currentChar - REGEX_CHARS[0] < 0 || currentChar > REGEX_CHARS[REGEX_CHARS.length - 1]) {
                continue;
            }

            if (SPECIAL_CHARS[currentChar]) {
                --threshold;
            }
        }

        return threshold == 0;
    }

    void compilePatternAndCache(final String value) {
        int currentFlags = 0;
        for (int flag : REGEX_FLAGS) {
            compilePatternAndCache(value, currentFlags |= flag);
        }
    }

    private void compilePatternAndCache(final String value, final int flags) {
        try {
            if (potentialRegex(value)) {
                final int patternHashCodeRegexFlagKey = value.hashCode() + flags;
                final Pattern computedPattern = Pattern.compile(value, flags);

                REGEX_PATTERN_CACHE.putIfAbsent(patternHashCodeRegexFlagKey, computedPattern);
            }
        } catch (final PatternSyntaxException e) {
            // We could not compile the pattern, probably because of some unescaped
            // characters that are special for regex, i.e.: JSON string literal
            compilePatternAndCache(value, Pattern.LITERAL);
        }
    }

    boolean match(final String patternCandidate, final String subject, final String templateTokenName, final Map<String, String> regexGroups) {
        int currentFlags = 0;
        for (int flag : REGEX_FLAGS) {
            if (match(patternCandidate, subject, templateTokenName, regexGroups, currentFlags |= flag)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(final String patternCandidate, final String subject, final String templateTokenName, final Map<String, String> regexGroups, final int flags) {
        try {
            final Pattern computedPattern = getCachedPatternOrCacheNewCompiled(patternCandidate, flags);
            final Matcher matcher = computedPattern.matcher(subject);
            final boolean isMatch = matcher.matches();
            if (isMatch) {
                // group(0) holds the full regex matchStubByIndex
                regexGroups.put(buildToken(templateTokenName, 0), matcher.group(0));

                //Matcher.groupCount() returns the number of explicitly defined capturing groups in the pattern regardless
                // of whether the capturing groups actually participated in the matchStubByIndex. It does not include matcher.group(0)
                final int groupCount = matcher.groupCount();
                if (groupCount > 0) {
                    for (int idx = 1; idx <= groupCount; idx++) {
                        regexGroups.put(buildToken(templateTokenName, idx), matcher.group(idx));
                    }
                }
            }
            return isMatch;
        } catch (final PatternSyntaxException e) {
            // We could not compile the pattern, probably because of some unescaped
            // characters that are special for regex, i.e.: JSON string literal
            return match(patternCandidate, subject, templateTokenName, regexGroups, Pattern.LITERAL);
        }
    }

    private Pattern getCachedPatternOrCacheNewCompiled(final String patternCandidate, int flags) {
        final int patternHashCodeRegexFlagKey = patternCandidate.hashCode() + flags;
        final Optional<Pattern> compiledPatternOptional = REGEX_PATTERN_CACHE.get(patternHashCodeRegexFlagKey);

        return compiledPatternOptional.orElseGet(() -> {
            final Pattern computedPattern = Pattern.compile(patternCandidate, flags);
            REGEX_PATTERN_CACHE.putIfAbsent(patternHashCodeRegexFlagKey, computedPattern);

            return computedPattern;
        });
    }
}
