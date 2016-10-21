package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 11:14 AM
 */
public class StringUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldConvertObjectToString_WhenObjectIsNotNull() throws Exception {

        final String result = StringUtils.objectToString(new Integer(888));

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

        final String originalElementsWithQuotes = "[\"alex\", \"tracy\", \"logan\", \"charlie\", \"isa\"]";
        final String expectedElementsWithQuotes = "[\"alex\",\"tracy\",\"logan\",\"charlie\",\"isa\"]";

        final String filteredElementsWithQuotes = StringUtils.trimSpacesBetweenCSVElements(originalElementsWithQuotes);

        assertThat(expectedElementsWithQuotes).isEqualTo(filteredElementsWithQuotes);
    }

    @Test
    public void shouldFilterOutSpacesBetweenElementsWithoutQuotes() throws Exception {

        final String originalElements = "[alex, tracy, logan, charlie, isa]";
        final String expectedElements = "[alex,tracy,logan,charlie,isa]";

        final String filteredElements = StringUtils.trimSpacesBetweenCSVElements(originalElements);

        assertThat(expectedElements).isEqualTo(filteredElements);
    }

    @Test
    public void shouldRemoveEncodedSquareBracketsFromString() throws Exception {

        final String originalElements = "%5Balex,tracy,logan,charlie,isa%5D";
        final String expectedElements = "alex,tracy,logan,charlie,isa";

        final String filteredElements = StringUtils.removeSquareBrackets(originalElements);

        assertThat(expectedElements).isEqualTo(filteredElements);
    }

    @Test
    public void shouldRemoveSquareBracketsFromString() throws Exception {

        final String originalElements = "[alex,tracy,logan,charlie,isa]";
        final String expectedElements = "alex,tracy,logan,charlie,isa";

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

        final String originalElements = "%5Balex,tracy,logan,charlie,isa%5D";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isTrue();
    }


    @Test
    public void shouldReturnFalseWhenStringWithinNotPairOfEscapedSquareBracket() throws Exception {

        final String originalElements = "%5Balex,tracy,logan,charlie,isa";

        final boolean isWithinSquareBrackets = StringUtils.isWithinSquareBrackets(originalElements);

        assertThat(isWithinSquareBrackets).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenStringWithinNotPairOfSquareBracket() throws Exception {

        final String originalElements = "[alex,tracy,logan,charlie,isa";

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
        assertThat(StringUtils.inputStreamToString(null)).isEqualTo("Could not convert empty or null input stream to string");
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
        tokensAndValues.put("url.1", "ALEX");
        tokensAndValues.put("url.2", "JOHN");
        tokensAndValues.put("url.3", "TRACY");
        tokensAndValues.put("query.1", "KOKO");
        final String template = "This is a response <% url.1 %> content <%url.2%> that going to be <%query.1    %> returned";

        final String replacedTemplate = StringUtils.replaceTokens(StringUtils.getBytesUtf8(template), tokensAndValues);
        assertThat(replacedTemplate).isEqualTo("This is a response ALEX content JOHN that going to be KOKO returned");
    }

    @Test
    public void shouldReplaceTokensInATemplateWhenNotAllTokenValuesPresent() throws Exception {

        final Map<String, String> tokensAndValues = new HashMap<String, String>();
        tokensAndValues.put("url.1", "ALEX");
        tokensAndValues.put("url.2", "JOHN");
        final String template = "This is a response <% url.1 %> content <%url.2%> that going to be <% query.1 %> returned";

        final String replacedTemplate = StringUtils.replaceTokens(StringUtils.getBytesUtf8(template), tokensAndValues);
        assertThat(replacedTemplate).isEqualTo("This is a response ALEX content JOHN that going to be <% query.1 %> returned");
    }


    /*
       https://github.com/apache/commons-lang/blob/master/src/test/java/org/apache/commons/lang3/StringUtilsTest.java
     */
    private static final char SEPARATOR = ',';
    private static final String TEXT_LIST = "foo,bar,baz";
    private static final String[] EMPTY_ARRAY_LIST = {};
    private static final String[] ARRAY_LIST = {"foo", "bar", "baz"};
    private static final String[] MIXED_ARRAY_LIST = {null, "", "foo"};

    @Test
    public void testJoin_ArrayString() {
        assertEquals("", StringUtils.join(EMPTY_ARRAY_LIST, SEPARATOR));
        assertEquals(TEXT_LIST, StringUtils.join(ARRAY_LIST, SEPARATOR));
        assertEquals(",,foo", StringUtils.join(MIXED_ARRAY_LIST, SEPARATOR));
    }

    @Test
    public void testRepeat_StringInt() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("ab", 0));
        assertEquals("", StringUtils.repeat("", 3));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("", StringUtils.repeat("a", -2));
        assertEquals("ababab", StringUtils.repeat("ab", 3));
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
    }
}
