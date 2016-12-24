package io.github.azagniotov.stubby4j.yaml.stubs;

import org.junit.Test;

import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;


public class RegexParserTest {

    @Test
    public void shouldDetermineStringAsPotentialRegexPatterns() throws Exception {
        final Pattern pattern = RegexParser.SPECIAL_REGEX_CHARS;

        assertThat(pattern.matcher("Hello^").matches()).isTrue();
        assertThat(pattern.matcher("[Array]").matches()).isTrue();
        assertThat(pattern.matcher("{JsonObject}").matches()).isTrue();
        assertThat(pattern.matcher("{JsonObject: [Array]}").matches()).isTrue();
        assertThat(pattern.matcher("This|That").matches()).isTrue();
        assertThat(pattern.matcher("I have a lot of $, how about you?").matches()).isTrue();
        assertThat(pattern.matcher("I have a lot of, how about you?").matches()).isTrue();
        assertThat(pattern.matcher("^[a-zA-Z]$").matches()).isTrue();
    }
}