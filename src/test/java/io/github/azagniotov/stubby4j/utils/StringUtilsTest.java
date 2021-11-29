package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.truth.Truth.assertThat;

public class StringUtilsTest {

    private static final String SEPARATOR = ",";
    private static final String TEXT_LIST = "foo,bar,baz";
    private static final String[] EMPTY_ARRAY_LIST = {};
    private static final String[] ARRAY_LIST = {"foo", "bar", "baz"};
    private static final String[] MIXED_ARRAY_LIST = {null, "", "foo"};

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isNotSet() throws Exception {
        assertThat(StringUtils.isNotSet(null)).isTrue();
        assertThat(StringUtils.isNotSet("")).isTrue();
        assertThat(StringUtils.isNotSet(" ")).isTrue();

        assertThat(StringUtils.isNotSet(" a ")).isFalse();
        assertThat(StringUtils.isNotSet("null")).isFalse();
    }

    @Test
    public void isNumeric() throws Exception {
        assertThat(StringUtils.isNumeric("9136d8b7-f7a7-478d-97a5-53292484aaf6")).isFalse();
        assertThat(StringUtils.isNumeric("8-88")).isFalse();
        assertThat(StringUtils.isNumeric("888")).isTrue();
    }

    @Test
    public void encodeBase16() throws Exception {
        assertThat(StringUtils.encodeBase16("".getBytes(StandardCharsets.UTF_8))).isEqualTo("");
        assertThat(StringUtils.encodeBase16(".".getBytes(StandardCharsets.UTF_8))).isEqualTo("2e");
        assertThat(StringUtils.encodeBase16("a".getBytes(StandardCharsets.UTF_8))).isEqualTo("61");

        final String originalString = "The Japanese raccoon dog is mainly nocturnal, but they are known to be active " +
                "during daylight. They vocalize by growling or with groans that have pitches resembling those of " +
                "domesticated cats. Like cats, the Japanese raccoon dog arches its back when it is trying to intimidate " +
                "other animals; however, they assume a defensive posture similar to that of other canids, lowering their " +
                "bodies and showing their bellies to submit.";
        byte[] originalStringBytes = originalString.getBytes(StandardCharsets.UTF_8);

        assertThat(StringUtils.encodeBase16(originalStringBytes)).isEqualTo("546865204a6170616e65736520726163636f6f6e20646f67206973206d61696e6c79206e6f637475726e616c2c20627574207468657920617265206b6e6f776e20746f2062652061637469766520647572696e67206461796c696768742e205468657920766f63616c697a652062792067726f776c696e67206f7220776974682067726f616e7320746861742068617665207069746368657320726573656d626c696e672074686f7365206f6620646f6d65737469636174656420636174732e204c696b6520636174732c20746865204a6170616e65736520726163636f6f6e20646f672061726368657320697473206261636b207768656e20697420697320747279696e6720746f20696e74696d6964617465206f7468657220616e696d616c733b20686f77657665722c207468657920617373756d65206120646566656e7369766520706f73747572652073696d696c617220746f2074686174206f66206f746865722063616e6964732c206c6f776572696e6720746865697220626f6469657320616e642073686f77696e672074686569722062656c6c69657320746f207375626d69742e");
    }

    @Test
    public void removeValueFromCsv() throws Exception {
        final String one = StringUtils.removeValueFromCsv("SLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL, include jdk.disabled.namedCurves", "SLv3");
        assertThat(one).isEqualTo("RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL, include jdk.disabled.namedCurves");

        final String two = StringUtils.removeValueFromCsv("SLv3", "SLv3");
        assertThat(two).isEqualTo("");

        final String three = StringUtils.removeValueFromCsv("SLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL, include jdk.disabled.namedCurves", "SLv3", "EC", "non-existent", "DES");
        assertThat(three).isEqualTo("RC4, MD5withRSA, DH keySize < 1024, anon, NULL, include jdk.disabled.namedCurves");

        final String four = StringUtils.removeValueFromCsv("", "SLv3", "EC", "non-existent", "DES");
        assertThat(four).isEqualTo("");

        assertThat(StringUtils.removeValueFromCsv(null, "SLv3")).isEqualTo("");
    }

    @Test
    public void splitCsv() throws Exception {
        assertThat(StringUtils.splitCsv(null)).isEqualTo(new HashSet<>());
        assertThat(StringUtils.splitCsv("")).isEqualTo(new HashSet<>());
        assertThat(StringUtils.splitCsv(" ")).isEqualTo(new HashSet<>());
        assertThat(StringUtils.splitCsv("SLv3")).isEqualTo(new HashSet<>(Collections.singletonList("SLv3")));
        assertThat(StringUtils.splitCsv("SLv3, RC4")).isEqualTo(new HashSet<>(Arrays.asList("SLv3", "RC4")));
    }

    @Test
    public void shouldConvertObjectToString_WhenObjectIsNotNull() throws Exception {

        final String result = StringUtils.objectToString(888);

        assertThat(result).isEqualTo("888");
    }

    @Test
    public void shouldConvertObjectToString_WhenObjectIsNull() throws Exception {

        final String result = StringUtils.objectToString(null);

        assertThat(result).isEqualTo(StringUtils.NOT_PROVIDED);
    }

    @Test
    public void shouldConvertObjectToString_WhenObjectIsStringNull() throws Exception {

        final String result = StringUtils.objectToString("null");

        assertThat(result).isEqualTo("");
    }

    @Test
    public void shouldDetermineObjectStringValue_WhenObjectIsFailedMessage() throws Exception {

        final String result = StringUtils.objectToString(StringUtils.getBytesUtf8(StringUtils.FAILED));

        assertThat(result).isEqualTo(StringUtils.FAILED);
    }

    @Test
    public void shouldFilterOutSpacesBetweenElementsWithQuotes() throws Exception {

        final String originalElementsWithQuotes = "[\"cheburashka\", \"wendy\", \"logan\", \"charlie\", \"isa\"]";
        final String expectedElementsWithQuotes = "[\"cheburashka\",\"wendy\",\"logan\",\"charlie\",\"isa\"]";

        final String filteredElementsWithQuotes = StringUtils.trimSpacesBetweenCSVElements(originalElementsWithQuotes);

        assertThat(expectedElementsWithQuotes).isEqualTo(filteredElementsWithQuotes);
    }

    @Test
    public void shouldFilterOutSpacesBetweenElementsWithoutQuotes() throws Exception {

        final String originalElements = "[cheburashka, wendy, logan, charlie, isa]";
        final String expectedElements = "[cheburashka,wendy,logan,charlie,isa]";

        final String filteredElements = StringUtils.trimSpacesBetweenCSVElements(originalElements);

        assertThat(expectedElements).isEqualTo(filteredElements);
    }

    @Test
    public void shouldRemoveEncodedSquareBracketsFromString() throws Exception {

        final String originalElements = "%5Bcheburashka,wendy,logan,charlie,isa%5D";
        final String expectedElements = "cheburashka,wendy,logan,charlie,isa";

        final String filteredElements = StringUtils.removeSquareBrackets(originalElements);

        assertThat(expectedElements).isEqualTo(filteredElements);
    }

    @Test
    public void shouldRemoveSquareBracketsFromString() throws Exception {

        final String originalElements = "[cheburashka,wendy,logan,charlie,isa]";
        final String expectedElements = "cheburashka,wendy,logan,charlie,isa";

        final String filteredElements = StringUtils.removeSquareBrackets(originalElements);

        assertThat(expectedElements).isEqualTo(filteredElements);
    }

    @Test
    public void shouldReturnTrueWhenStringWithinSquareBrackets() throws Exception {

        final String originalElements = "[%22id%22,%20%22uuid%22,%20%22created%22,%20%22lastUpdated%22,%20%22displayName%22,%20%22email%22,%20%22givenName%22,%20%22familyName%22]";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenStringWithinEncodedSquareBrackets() throws Exception {

        final String originalElements = "%5Bcheburashka,wendy,logan,charlie,isa%5D";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isTrue();
    }


    @Test
    public void shouldReturnFalseWhenStringWithinNotPairOfEscapedSquareBracket() throws Exception {

        final String originalElements = "%5Bcheburashka,wendy,logan,charlie,isa";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenStringWithinNotPairOfSquareBracket() throws Exception {

        final String originalElements = "[cheburashka,wendy,logan,charlie,isa";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isFalse();
    }

    @Test
    public void shouldReturnNullWhenTryingTolowerEmptyString() throws Exception {
        assertThat(StringUtils.toLower("")).isEmpty();
    }

    @Test
    public void shouldReturnNullWhenTryingToUpperEmptyString() throws Exception {
        assertThat(StringUtils.toUpper("")).isEmpty();
    }

    @Test
    public void shouldReturnErrorWhenTryingToConvertNullInputStreamToString() throws Exception {
        assertThat(StringUtils.inputStreamToString(null)).isEqualTo("Could not convert null input stream to string");
    }

    @Test
    public void shouldReturnErrorWhenTryingToConvertEmptyInputStreamToString() throws Exception {

        expectedException.expect(NoSuchElementException.class);

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(StringUtils.getBytesUtf8(""));
        assertThat(StringUtils.inputStreamToString(byteArrayInputStream)).isEqualTo("");
    }

    @Test
    public void shouldReturnErrorWhenTryingToConvertSpaceInputStreamToString() throws Exception {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(StringUtils.getBytesUtf8(" "));
        assertThat(StringUtils.inputStreamToString(byteArrayInputStream)).isEqualTo("");
    }


    @Test
    public void shouldCorrectlyEncodeSingleQuotesInURL() throws Exception {

        final String originaUrl = "http://localhost:8882/entity.find.single.quote?client_secret=secret&attributes=['id','uuid','created','lastUpdated','displayName','email','givenName','familyName']";
        final String expectedEncodedUrl = "http://localhost:8882/entity.find.single.quote?client_secret=secret&attributes=[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]";
        final String actualEncodedUrl = StringUtils.encodeSingleQuotes(originaUrl);

        assertThat(actualEncodedUrl).isEqualTo(expectedEncodedUrl);
    }

    @Test
    public void shouldConstructUserAgentNameWhenImplementationTitleNotSet() throws Exception {

        final String userAgentName = StringUtils.constructUserAgentName();

        assertThat(userAgentName).contains("stubby4j");
        assertThat(userAgentName).contains("(HTTP stub client request)");
    }

    @Test
    public void shouldEscapeCurlyBraces() throws Exception {

        final String escaped = StringUtils.escapeSpecialRegexCharacters("[{'key': 'value'}, {'key': 'value'}]");

        assertThat(escaped).isEqualTo("\\[\\{'key': 'value'\\}, \\{'key': 'value'\\}\\]");
    }

    @Test
    public void shouldReplaceTokensInATemplateWhenAllTokensPresent() throws Exception {

        final Map<String, String> tokensAndValues = new HashMap<>();
        tokensAndValues.put("url.1", "cheburashka");
        tokensAndValues.put("url.2", "JOHN");
        tokensAndValues.put("url.3", "wendy");
        tokensAndValues.put("query.1", "KOKO");
        final String template = "This is a response <% url.1 %> content <%url.2%> that going to be <%query.1    %> returned";

        final String replacedTemplate = StringUtils.replaceTokens(StringUtils.getBytesUtf8(template), tokensAndValues);
        assertThat(replacedTemplate).isEqualTo("This is a response cheburashka content JOHN that going to be KOKO returned");
    }

    @Test
    public void shouldReplaceTokensInATemplateWhenNotAllTokenValuesPresent() throws Exception {

        final Map<String, String> tokensAndValues = new HashMap<String, String>();
        tokensAndValues.put("url.1", "cheburashka");
        tokensAndValues.put("url.2", "JOHN");
        final String template = "This is a response <% url.1 %> content <%url.2%> that going to be <% query.1 %> returned";

        final String replacedTemplate = StringUtils.replaceTokens(StringUtils.getBytesUtf8(template), tokensAndValues);
        assertThat(replacedTemplate).isEqualTo("This is a response cheburashka content JOHN that going to be <% query.1 %> returned");
    }

    @Test
    public void shouldJoinArrayString() {
        assertThat("").isEqualTo(StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR));
        assertThat(TEXT_LIST).isEqualTo(StringUtils.join(ARRAY_LIST, SEPARATOR));
        assertThat("null,,foo").isEqualTo(StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR));
    }

    @Test
    public void shouldRepeatString() {
        assertThat("").isEqualTo(StringUtils.repeat(null, 2));
        assertThat("").isEqualTo(StringUtils.repeat("ab", 0));
        assertThat("").isEqualTo(StringUtils.repeat("", 3));
        assertThat("aaa").isEqualTo(StringUtils.repeat("a", 3));
        assertThat("").isEqualTo(StringUtils.repeat("a", -2));
        assertThat("ababab").isEqualTo(StringUtils.repeat("ab", 3));
        assertThat("abcabcabc").isEqualTo(StringUtils.repeat("abc", 3));
    }
}
