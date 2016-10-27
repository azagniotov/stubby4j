package io.github.azagniotov.stubby4j.parser.json;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JSONAssertTest {

    @Test
    public void shouldCompareTwoJsonArraysWithDifferentContentOrder() throws Exception {
        JSONAssert.assertEquals(
                StringUtils.inputStreamToString(JSONAssertTest.class.getResourceAsStream("/json/array.1.json")),
                StringUtils.inputStreamToString(JSONAssertTest.class.getResourceAsStream("/json/array.2.json")),
                JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldCompareTwoJsonComplexGraphsWithDifferentContentOrder() throws Exception {
        JSONAssert.assertEquals(
                StringUtils.inputStreamToString(JSONAssertTest.class.getResourceAsStream("/json/graph.1.json")),
                StringUtils.inputStreamToString(JSONAssertTest.class.getResourceAsStream("/json/graph.2.json")),
                JSONCompareMode.NON_EXTENSIBLE);
    }
}
