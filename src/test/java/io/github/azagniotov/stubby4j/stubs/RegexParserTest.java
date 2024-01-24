/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.stubs;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.RegexParser.REGEX_CHARS;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RegexParserTest {

    @Test
    public void shouldDetermineStringAsPotentialRegexPatterns() throws Exception {

        assertThat(RegexParser.potentialRegex(".*")).isTrue();
        assertThat(RegexParser.potentialRegex("[^")).isTrue();
        assertThat(RegexParser.potentialRegex("(.*)")).isTrue();
        assertThat(RegexParser.potentialRegex("\\s+")).isTrue();
        assertThat(RegexParser.potentialRegex("\\w+")).isTrue();
        assertThat(RegexParser.potentialRegex("[Hello^]")).isTrue();
        assertThat(RegexParser.potentialRegex("[Array]")).isTrue();
        assertThat(RegexParser.potentialRegex("{JsonObject}")).isTrue();
        assertThat(RegexParser.potentialRegex("{JsonObject: [Array]}")).isTrue();
        assertThat(RegexParser.potentialRegex("This|That$")).isTrue();
        assertThat(RegexParser.potentialRegex("I have a lot of $, how about you?"))
                .isTrue();
        assertThat(RegexParser.potentialRegex("^I have a lot of, how about you?"))
                .isTrue();
        assertThat(RegexParser.potentialRegex("^[a-zA-Z]$")).isTrue();
        assertThat(RegexParser.potentialRegex("^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                .isTrue();
        assertThat(RegexParser.potentialRegex("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$"))
                .isTrue();
        assertThat(RegexParser.potentialRegex("^\\\\")).isTrue();
        assertThat(RegexParser.potentialRegex("^\\")).isTrue();
        assertThat(RegexParser.potentialRegex("**")).isTrue();
        assertThat(RegexParser.potentialRegex("()")).isTrue();

        assertThat(RegexParser.potentialRegex("!\"~")).isFalse();
        assertThat(RegexParser.potentialRegex("This is a sentence ending with a dot."))
                .isFalse();
        assertThat(RegexParser.potentialRegex("Is this a sentence ending with a question?"))
                .isFalse();
        assertThat(RegexParser.potentialRegex("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                .isFalse();
        assertThat(RegexParser.potentialRegex("")).isFalse();
        assertThat(RegexParser.potentialRegex(")")).isFalse();
        assertThat(RegexParser.potentialRegex(" ")).isFalse();
        assertThat(RegexParser.potentialRegex("^")).isFalse();
        assertThat(RegexParser.potentialRegex("\\")).isFalse();
        assertThat(RegexParser.potentialRegex("////////")).isFalse();
        assertThat(RegexParser.potentialRegex("<A>")).isFalse();
        assertThat(RegexParser.potentialRegex("abcdeABCDE")).isFalse();
        assertThat(RegexParser.potentialRegex(Character.toString((char) (REGEX_CHARS[0] - 1))))
                .isFalse();
        assertThat(RegexParser.potentialRegex(Character.toString((char) (REGEX_CHARS[REGEX_CHARS.length - 1] + 1))))
                .isFalse();
    }

    @Test
    public void shouldNotMatchWhenRegexPatternWithSyntaxError() throws Exception {
        final String patternWithSyntaxError = "^abc[xyz{*";
        boolean match = RegexParser.INSTANCE.match(
                patternWithSyntaxError, "someStringToMatch", "templateTokenName", new HashMap<>());

        assertThat(match).isFalse();
    }

    @Test
    public void shouldMatchSubjectWithMultiline() throws Exception {
        final String testSubject = "Biggest tech companies in the world by their market value as of 2018:" + BR
                + "Apple: $741.8 billion. In 2014, Apple, Inc. introduced a programming language called Swift."
                + BR + "In 2015, the company made Swift open-source, which allowed non-Apple developers to work "
                + "on the project."
                + BR + "Record-breaking three-day sales of 13 million units of the company’s "
                + "iPhone 6s and iPhone 6s Plus were announced in October 2015."
                + BR + BR
                + "Alphabet: $367.6 billion. In October 2015, Google restructured the company so that Alphabet, Inc."
                + BR
                + "became the parent company under which Google operates. Alphabet owns all of Google's side projects,"
                + BR
                + "such as life-extension company Calico, innovative technology developer Google X, high-speed Internet"
                + BR + "provider Fiber and Google's smart home project Nest.";

        final String regexPattern = "(.*)\\s+Apple:\\s+(.*)\\s+Alphabet:\\s+(.*)";

        final Map<String, String> regexGroups = new HashMap<>();
        boolean match = RegexParser.INSTANCE.match(regexPattern, testSubject, "token", regexGroups);

        assertThat(match).isTrue();
        assertThat(regexGroups.get("token.1").trim())
                .isEqualTo("Biggest tech companies in the world by their market value as of 2018:");
        assertThat(regexGroups.get("token.2").trim())
                .isEqualTo("$741.8 billion. In 2014, Apple, Inc. introduced a programming language called Swift." + BR
                        + "In 2015, the company made Swift open-source, which allowed non-Apple developers to work "
                        + "on the project."
                        + BR + "Record-breaking three-day sales of 13 million units of the company’s "
                        + "iPhone 6s and iPhone 6s Plus were announced in October 2015.");
        assertThat(regexGroups.get("token.3").trim())
                .isEqualTo(
                        "$367.6 billion. In October 2015, Google restructured the company so that Alphabet, Inc." + BR
                                + "became the parent company under which Google operates. Alphabet owns all of Google's side projects,"
                                + BR
                                + "such as life-extension company Calico, innovative technology developer Google X, high-speed Internet"
                                + BR + "provider Fiber and Google's smart home project Nest.");
    }
}
