package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
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
    public void isNumeric() throws Exception {
        assertThat(StringUtils.isNumeric("9136d8b7-f7a7-478d-97a5-53292484aaf6")).isFalse();
        assertThat(StringUtils.isNumeric("8-88")).isFalse();
        assertThat(StringUtils.isNumeric("888")).isTrue();
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
