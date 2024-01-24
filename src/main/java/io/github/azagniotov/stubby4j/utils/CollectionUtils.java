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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static Map<String, String> constructParamMap(final String requestQueryString) {
        if (!StringUtils.isSet(requestQueryString)) {
            return new LinkedHashMap<>();
        }

        final Map<String, String> paramMap = new LinkedHashMap<>();
        final String[] pairs = requestQueryString.split("&");
        for (final String pair : pairs) {
            final String[] splittedPair = pair.split("=");

            final String splittedPairKey = splittedPair[0];
            String splittedPairValue = splittedPair.length > 1 ? splittedPair[1] : "";

            if (StringUtils.isWithinSquareBrackets(splittedPairValue)) {
                final String cleansedValue =
                        StringUtils.decodeUrlEncodedQuotes(StringUtils.removeSquareBrackets(splittedPairValue));
                final String bracketedQueryValueAsCSV =
                        Arrays.asList(cleansedValue.split(",")).toString();
                splittedPairValue = StringUtils.trimSpacesBetweenCSVElements(bracketedQueryValueAsCSV);
            }

            paramMap.put(splittedPairKey, StringUtils.decodeUrlEncoded(splittedPairValue));
        }
        return paramMap;
    }

    public static String constructQueryString(final Map<String, String> query) {
        final Set<Map.Entry<String, String>> mapEntries = query.entrySet();
        final Iterator<Map.Entry<String, String>> iterator = mapEntries.iterator();
        final StringBuilder queryStringBuilder = new StringBuilder();

        while (iterator.hasNext()) {
            final Map.Entry<String, String> entry = iterator.next();
            final String pair = String.format("%s=%s", entry.getKey(), entry.getValue());
            queryStringBuilder.append(pair);
            if (iterator.hasNext()) {
                queryStringBuilder.append('&');
            }
        }
        return queryStringBuilder.toString();
    }

    public static <T> T[] concatWithArrayCopy(T[] one, T[] two) {
        final T[] result = Arrays.copyOf(one, one.length + two.length);
        System.arraycopy(two, 0, result, one.length, two.length);
        return result;
    }

    public static BlockingQueue<ByteBuffer> chunkifyByteArrayAndQueue(final byte[] source, final int numberOfChunks) {
        final int byteSizePerFrame = source.length <= numberOfChunks ? 1 : (source.length / numberOfChunks);
        final BlockingQueue<ByteBuffer> queue = new ArrayBlockingQueue<>(numberOfChunks * 2);

        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + byteSizePerFrame);
            queue.add(ByteBuffer.wrap(Arrays.copyOfRange(source, start, end)));
            start += byteSizePerFrame;
        }

        return queue;
    }
}
