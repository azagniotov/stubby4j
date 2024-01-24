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

package io.github.azagniotov.stubby4j.utils;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.StringUtils.encodeBase16;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.junit.Test;

@SuppressWarnings("serial")
public class CollectionUtilsTest {

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryStringGiven() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramTwo", "two");
                put("paramOne", "one");
            }
        };

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne=one&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryParamHasNoValue() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramTwo", "two");
                put("paramOne", "");
            }
        };

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne=&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenQueryParamHasNoValueNorEqualSign() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramTwo", "two");
                put("paramOne", "");
            }
        };

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne&paramTwo=two");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldConstructParamMap_WhenSingleQueryParamHasNoValueNorEqualSign()
            throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramOne", "");
            }
        };

        final Map<String, String> actualParams = CollectionUtils.constructParamMap("paramOne");

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructQueryString_ShouldConstructQueryString_WhenParamMapGiven() throws Exception {

        final Map<String, String> expectedParams = new LinkedHashMap<String, String>() {
            {
                put("paramTwo", "two");
                put("paramOne", "one");
            }
        };

        final String actualQueryString = CollectionUtils.constructQueryString(expectedParams);
        final String expectedQueryString = "paramTwo=two&paramOne=one";

        assertThat(expectedQueryString).isEqualTo(actualQueryString);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArray() throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramOne", "[id,uuid,created,lastUpdated,displayName,email,givenName,familyName]");
            }
        };

        final String queryString = String.format(
                "paramOne=%s", "%5Bid,uuid,created,lastUpdated,displayName,email,givenName,familyName%5D");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithQuotedElements()
            throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put(
                        "paramOne",
                        "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]");
            }
        };

        final String queryString = String.format(
                "paramOne=%s",
                "%5B%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22%5D");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void constructParamMap_ShouldUrlDecodeQueryString_WhenQueryParamsAreAnArrayWithSingleQuoteElements()
            throws Exception {

        final Map<String, String> expectedParams = new HashMap<String, String>() {
            {
                put("paramOne", "['id','uuid','created','lastUpdated','displayName','email','givenName','familyName']");
            }
        };

        final String queryString = String.format(
                "paramOne=%s",
                "[%27id%27,%27uuid%27,%27created%27,%27lastUpdated%27,%27displayName%27,%27email%27,%27givenName%27,%27familyName%27]");
        final Map<String, String> actualParams = CollectionUtils.constructParamMap(queryString);

        assertThat(expectedParams).isEqualTo(actualParams);
    }

    @Test
    public void givenTwoStringArrays_whenConcatWithCopy_thenGetExpectedResult() {
        final String[] args = new String[] {"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] flags = new String[] {"enable_tls_with_alpn_and_http_2"};

        final String[] expected = new String[] {
            "-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443", "enable_tls_with_alpn_and_http_2"
        };
        final String[] actual = CollectionUtils.concatWithArrayCopy(args, flags);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void givenTwoStringArraysWithOneEmpty_whenConcatWithCopy_thenGetExpectedResult() {
        final String[] args = new String[] {"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] flags = new String[] {};

        final String[] expected = new String[] {"-m", "-l", "127.0.0.1", "-s", "8882", "-a", "8889", "-t", "7443"};
        final String[] actual = CollectionUtils.concatWithArrayCopy(args, flags);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void chunkifyByteArrayAndQueue() {
        final String originalString = "The Japanese raccoon dog is mainly nocturnal, but they are known to be active"
                + "during daylight. They vocalize by growling or with groans that have pitches resembling those of"
                + "domesticated cats. Like cats, the Japanese raccoon dog arches its back when it is trying to intimidate"
                + "other animals; however, they assume a defensive posture similar to that of other canids, lowering their"
                + "bodies and showing their bellies to submit.";
        final byte[] originalStringBytes = originalString.getBytes(StandardCharsets.UTF_8);
        final BlockingQueue<ByteBuffer> byteArrayQueue =
                CollectionUtils.chunkifyByteArrayAndQueue(originalStringBytes, 100);

        ByteBuffer allocatedByteBuffer = ByteBuffer.allocate(originalStringBytes.length);
        while (!byteArrayQueue.isEmpty()) {
            allocatedByteBuffer = allocatedByteBuffer.put(byteArrayQueue.poll());
        }
        final byte[] actualStringBytes = allocatedByteBuffer.array();

        assertThat(encodeBase16(originalStringBytes)).isEqualTo(encodeBase16(actualStringBytes));
        assertThat(originalStringBytes).isEqualTo(actualStringBytes);
        assertThat(originalString).isEqualTo(new String(actualStringBytes, StandardCharsets.UTF_8));
    }

    @Test
    public void chunkifyTinyByteArrayAndQueue() {
        final String originalString = "The Japanese raccoon dog.";
        final byte[] originalStringBytes = originalString.getBytes(StandardCharsets.UTF_8);
        final BlockingQueue<ByteBuffer> byteArrayQueue =
                CollectionUtils.chunkifyByteArrayAndQueue(originalStringBytes, 100);

        ByteBuffer allocatedByteBuffer = ByteBuffer.allocate(originalStringBytes.length);
        while (!byteArrayQueue.isEmpty()) {
            allocatedByteBuffer = allocatedByteBuffer.put(byteArrayQueue.poll());
        }
        final byte[] actualStringBytes = allocatedByteBuffer.array();

        assertThat(encodeBase16(originalStringBytes)).isEqualTo(encodeBase16(actualStringBytes));
        assertThat(originalStringBytes).isEqualTo(actualStringBytes);
        assertThat(originalString).isEqualTo(new String(actualStringBytes, StandardCharsets.UTF_8));
    }
}
