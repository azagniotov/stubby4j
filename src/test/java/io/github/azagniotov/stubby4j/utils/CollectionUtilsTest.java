package io.github.azagniotov.stubby4j.utils;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;


@SuppressWarnings("serial")
public class CollectionUtilsTest {

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryStringGiven() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramTwo", "two");
            put("paramOne", "one");
        }};

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne=one&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryParamHasNoValue() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramTwo", "two");
            put("paramOne", "");
        }};

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne=&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryParamHasNoValueNorEqualSign() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramTwo", "two");
            put("paramOne", "");
        }};

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenSingleQueryParamHasNoValueNorEqualSign() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramOne", "");
        }};

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructQueryString_ShouldConstructQueryString_WhenParamMapGiven() throws Exception {

        final Map<String, String> expectedParams = new LinkedHashMap<String, String>() {{
            put("paramTwo", "two");
            put("paramOne", "one");
        }};

        final String actualQueryString = CollectionUtils.constructQueryString(expectedParams);
        final String expectedQueryString = "paramTwo=two&paramOne=one";

        assertThat(expectedQueryString).isEqualTo(actualQueryString);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArray() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramOne", "[id,uuid,created,lastUpdated,displayName,email,givenName,familyName]");
        }};


        final String queryString = String.format("paramOne=%s", "%5Bid,uuid,created,lastUpdated,displayName,email,givenName,familyName%5D");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithQuotedElements() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramOne", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]");
        }};


        final String queryString = String.format("paramOne=%s", "%5B%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22%5D");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithSingleQuoteElements() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {{
            put("paramOne", "['id','uuid','created','lastUpdated','displayName','email','givenName','familyName']");
        }};


        final String queryString = String.format("paramOne=%s", "[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void givenTwoStringArrays_whenConcatWithCopy_thenGetExpectedResult() {
        final String[] args = new String[]{"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] flags = new String[]{"enable_tls_with_alpn_and_http_2"};

        final String[] expected = new String[]{"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443", "enable_tls_with_alpn_and_http_2"};
        final String[] actual = CollectionUtils.concatWithArrayCopy(args, flags);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void givenTwoStringArraysWithOneEmpty_whenConcatWithCopy_thenGetExpectedResult() {
        final String[] args = new String[]{"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] flags = new String[]{};

        final String[] expected = new String[]{"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] actual = CollectionUtils.concatWithArrayCopy(args, flags);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void chunkifyByteArray() {
        final String originalString = "The Japanese raccoon dog is mainly nocturnal, but they are known to be active" +
                "during daylight. They vocalize by growling or with groans that have pitches resembling those of" +
                "domesticated cats. Like cats, the Japanese raccoon dog arches its back when it is trying to intimidate" +
                "other animals; however, they assume a defensive posture similar to that of other canids, lowering their" +
                "bodies and showing their bellies to submit.";
        final byte[] originalStringBytes = originalString.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> chunkifiedBytes = CollectionUtils.chunkifyByteArray(originalStringBytes, 100);

        ByteBuffer allocatedByteBuffer = ByteBuffer.allocate(originalStringBytes.length);
        for (final byte[] chunk : chunkifiedBytes) {
            allocatedByteBuffer = allocatedByteBuffer.put(chunk);
        }
        final byte[] actualStringBytes = allocatedByteBuffer.array();

        assertThat(originalStringBytes).isEqualTo(actualStringBytes);
        assertThat(originalString).isEqualTo(new String(actualStringBytes, StandardCharsets.UTF_8));
    }
}
