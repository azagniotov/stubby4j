package io.github.azagniotov.stubby4j.yaml.stubs;


import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.github.azagniotov.stubby4j.utils.StringUtils.buildToken;
import static java.util.regex.Pattern.quote;

enum RegexParser {

    INSTANCE;

    @VisibleForTesting
    static final Map<Integer, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    // A very primitive way to test if string is *maybe* a regex pattern, instead of compiling a Pattern
    private final Pattern SPECIAL_REGEX_CHARS = Pattern.compile(String.format(".*([%s%s%s%s%s%s%s%s%s%s]).*",
            quote("^"),
            quote("$"),
            quote("["),
            quote("]"),
            quote("{"),
            quote("}"),
            quote("*"),
            quote("|"),
            quote("\\"),
            quote("?")));

    void compilePatternAndCache(final String value) {
        try {
            if (SPECIAL_REGEX_CHARS.matcher(value).matches()) {
                PATTERN_CACHE.computeIfAbsent(value.hashCode(), hashCode -> Pattern.compile(value, Pattern.MULTILINE));
            }
        } catch (final PatternSyntaxException e) {
            PATTERN_CACHE.computeIfAbsent(value.hashCode(), hashCode -> Pattern.compile(value, Pattern.LITERAL | Pattern.MULTILINE));
        }
    }

    boolean match(final String patternCandidate, final String subject, final String templateTokenName, final Map<String, String> regexGroups) {
        return match(patternCandidate, subject, templateTokenName, regexGroups, Pattern.MULTILINE);
    }

    private boolean match(final String patternCandidate, final String subject, final String templateTokenName, final Map<String, String> regexGroups, final int flags) {
        try {
            // Pattern.MULTILINE changes the behavior of '^' and '$' characters,
            // it does not mean that newline feeds and carriage return will be matched by default
            // You need to make sure that you regex pattern covers both \r (carriage return) and \n (linefeed).
            // It is achievable by using symbol '\s+' which covers both \r (carriage return) and \n (linefeed).

            final Pattern pattern = PATTERN_CACHE.computeIfAbsent(
                    patternCandidate.hashCode(), hashCode -> Pattern.compile(patternCandidate, flags));

            final Matcher matcher = pattern.matcher(subject);
            final boolean isMatch = matcher.matches();
            if (isMatch) {
                // group(0) holds the full regex match
                regexGroups.put(buildToken(templateTokenName, 0), matcher.group(0));

                //Matcher.groupCount() returns the number of explicitly defined capturing groups in the pattern regardless
                // of whether the capturing groups actually participated in the match. It does not include matcher.group(0)
                final int groupCount = matcher.groupCount();
                if (groupCount > 0) {
                    for (int idx = 1; idx <= groupCount; idx++) {
                        regexGroups.put(buildToken(templateTokenName, idx), matcher.group(idx));
                    }
                }
            }
            return isMatch;
        } catch (PatternSyntaxException e) {
            return match(patternCandidate, subject, templateTokenName, regexGroups, Pattern.LITERAL | Pattern.MULTILINE);
        }
    }
}
