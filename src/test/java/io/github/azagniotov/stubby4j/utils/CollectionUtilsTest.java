package io.github.azagniotov.stubby4j.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
}
