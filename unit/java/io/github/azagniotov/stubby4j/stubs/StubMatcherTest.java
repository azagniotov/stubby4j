package io.github.azagniotov.stubby4j.stubs;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class StubMatcherTest {

    private StubMatcher stubMatcher;

    @Before
    public void setUp() throws Exception {
        stubMatcher = new StubMatcher(new HashMap<>());
    }

    @Test
    public void arraysIntersect_ShouldReturnTrue_WhenDataStoreArrayEmpty() throws Exception {
        final boolean isArraysIntersect = stubMatcher.listsIntersect(new ArrayList<>(), new ArrayList<String>() {{
            add("apple");
        }});

        assertThat(isArraysIntersect).isTrue();
    }

    @Test
    public void arraysIntersect_ShouldReturnFalse_WhenAssertingArrayEmpty() throws Exception {
        final boolean isArraysIntersect = stubMatcher.listsIntersect(new ArrayList<String>() {{
            add("apple");
        }}, new ArrayList<>());

        assertThat(isArraysIntersect).isFalse();
    }

    @Test
    public void arraysIntersect_ShouldReturnTrue_WhenTwoArraysHaveTheSameElements() throws Exception {
        final boolean isArraysIntersect = stubMatcher.listsIntersect(
                new ArrayList<String>() {{
                    add("apple");
                }}, new ArrayList<String>() {{
                    add("apple");
                }}
        );

        assertThat(isArraysIntersect).isTrue();
    }

    @Test
    public void arraysIntersect_ShouldReturnFalse_WhenTwoArraysDontHaveTheSameElements() throws Exception {
        final boolean isArraysIntersect = stubMatcher.listsIntersect(
                new ArrayList<String>() {{
                    add("apple");
                }}, new ArrayList<String>() {{
                    add("orange");
                }}
        );

        assertThat(isArraysIntersect).isFalse();
    }

    @Test
    public void stringsMatch_ShouldReturnTrue_WhenDataStoreValueNull() throws Exception {
        final String dataStoreVlaue = null;
        final String assertingValue = "blah";
        final boolean isStringsMatch = stubMatcher.stringsMatch(dataStoreVlaue, assertingValue, "arbitrary template token name");

        assertThat(isStringsMatch).isTrue();
    }

    @Test
    public void stringsMatch_ShouldReturnTrue_WhenDataStoreValueEmpty() throws Exception {
        final String dataStoreVlaue = "";
        final String assertingValue = "blah";
        final boolean isStringsMatch = stubMatcher.stringsMatch(dataStoreVlaue, assertingValue, "arbitrary template token name");

        assertThat(isStringsMatch).isTrue();
    }

    @Test
    public void stringsMatch_ShouldReturnFalse_WhenAssertingValueNull() throws Exception {
        final String dataStoreVlaue = "stubbedValue";
        final String assertingValue = null;
        final boolean isStringsMatch = stubMatcher.stringsMatch(dataStoreVlaue, assertingValue, "arbitrary template token name");

        assertThat(isStringsMatch).isFalse();
    }

    @Test
    public void stringsMatch_ShouldReturnFalse_WhenAssertingValueEmpty() throws Exception {
        final String dataStoreVlaue = "stubbedValue";
        final String assertingValue = "";
        final boolean isStringsMatch = stubMatcher.stringsMatch(dataStoreVlaue, assertingValue, "arbitrary template token name");

        assertThat(isStringsMatch).isFalse();
    }

    @Test
    public void mapsMatch_ShouldReturnTrue_WhenDataStoreMapEmptyAndAssertingMapEmpty() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<>();
        final Map<String, String> assertingMap = new HashMap<>();
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isTrue();
    }

    @Test
    public void mapsMatch_ShouldReturnTrue_WhenDataStoreMapEmpty() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<>();
        final Map<String, String> assertingMap = new HashMap<String, String>() {{
            put("key", "value");
        }};
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isTrue();
    }

    @Test
    public void mapsMatch_ShouldReturnFalse_WhenDataStoreMapNotEmptyAndAssertingMapEmpty() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<String, String>() {{
            put("key", "value");
        }};
        final Map<String, String> assertingMap = new HashMap<>();
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isFalse();
    }

    @Test
    public void mapsMatch_ShouldReturnFalse_WhenAssertingMapDoesNotContainDataStoreKey() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<String, String>() {{
            put("requiredKey", "requiredValue");
        }};
        final Map<String, String> assertingMap = new HashMap<String, String>() {{
            put("someKey", "someValue");
        }};
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isFalse();
    }

    @Test
    public void mapsMatch_ShouldReturnFalse_WhenAssertingMapDoesNotContainDataStoreValue() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<String, String>() {{
            put("requiredKey", "requiredValue");
        }};
        final Map<String, String> assertingMap = new HashMap<String, String>() {{
            put("requiredKey", "someValue");
        }};
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isFalse();
    }

    @Test
    public void mapsMatch_ShouldReturnTrue_WhenAssertingMapMatchesDataStoreMap() throws Exception {
        final Map<String, String> dataStoreMap = new HashMap<String, String>() {{
            put("requiredKey", "requiredValue");
        }};
        final Map<String, String> assertingMap = new HashMap<String, String>() {{
            put("requiredKey", "requiredValue");
        }};
        final boolean isMapsMatch = stubMatcher.mapsMatch(dataStoreMap, assertingMap, "arbitrary template token name");

        assertThat(isMapsMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenIsPostStubbedFalse() {
        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(false, null, null);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenAssertingPostBodyNull() {
        StubRequest request = new StubRequest.Builder().build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, null, request);

        assertThat(isBodiesMatch).isFalse();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenAssertingBodyWhitespace() {
        StubRequest request = new StubRequest.Builder()
                .withPost("  ")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, null, request);

        assertThat(isBodiesMatch).isFalse();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenDifferentJson() {
        StubRequest request = new StubRequest.Builder()
                .withPost("{\"name\":\"bob\"}")
                .withHeaderContentType("application/json")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "{\"name\":\"tod\"}", request);

        assertThat(isBodiesMatch).isFalse();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentJson() {
        StubRequest request = new StubRequest.Builder()
                .withPost("{\"a\":\"b\",\"c\":\"d\"}")
                .withHeaderContentType("application/json")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "{\"c\":\"d\",\"a\":\"b\"}", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentCustomJson() {
        StubRequest request = new StubRequest.Builder()
                .withPost("{\"a\":\"b\",\"c\":\"d\"}")
                .withHeaderContentType("something/vnd.com+json")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "{\"c\":\"d\",\"a\":\"b\"}", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentCustomJsonCharset() {
        StubRequest request = new StubRequest.Builder()
                .withPost("{\"a\":\"b\",\"c\":\"d\"}")
                .withHeaderContentType("something/vnd.com+json;charset=stuff")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "{\"c\":\"d\",\"a\":\"b\"}", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenDifferentXml() {
        StubRequest request = new StubRequest.Builder()
                .withPost("<xml>one</xml>")
                .withHeaderContentType("application/xml")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "<xml>two</xml>", request);

        assertThat(isBodiesMatch).isFalse();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentXml() {
        StubRequest request = new StubRequest.Builder()
                .withPost("<xml><a>a</a><b>b</b></xml>")
                .withHeaderContentType("application/xml")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "<xml><b>b</b><a>a</a></xml>", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentCustomXml() {
        StubRequest request = new StubRequest.Builder()
                .withPost("<xml><a>a</a><b>b</b></xml>")
                .withHeaderContentType("something/vnd.com+xml")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "<xml><b>b</b><a>a</a></xml>", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenEquivalentCustomXmlCharset() {
        StubRequest request = new StubRequest.Builder()
                .withPost("<xml><a>a</a><b>b</b></xml>")
                .withHeaderContentType("something/vnd.com+xml;charset=something")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "<xml><b>b</b><a>a</a></xml>", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenStringMatch() {
        StubRequest request = new StubRequest.Builder()
                .withPost("wibble")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "wibble", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenStringNotMatch() {
        StubRequest request = new StubRequest.Builder()
                .withPost("wibble")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "wobble", request);

        assertThat(isBodiesMatch).isFalse();
    }

    @Test
    public void postBodiesMatch_ShouldReturnTrue_WhenStringMatchWeirdContentType() {
        StubRequest request = new StubRequest.Builder()
                .withPost("wibble")
                .withHeaderContentType("whut")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "wibble", request);

        assertThat(isBodiesMatch).isTrue();
    }

    @Test
    public void postBodiesMatch_ShouldReturnFalse_WhenStringMatchWeirdContentType() {
        StubRequest request = new StubRequest.Builder()
                .withPost("wobble")
                .withHeaderContentType("whut")
                .build();

        final boolean isBodiesMatch = stubMatcher.postBodiesMatch(true, "wibble", request);

        assertThat(isBodiesMatch).isFalse();
    }
}
