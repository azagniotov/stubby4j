package io.github.azagniotov.stubby4j.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class CollectionUtils {

    private CollectionUtils() {

    }

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
                final String cleansedValue = StringUtils.decodeUrlEncodedQuotes(StringUtils.removeSquareBrackets(splittedPairValue));
                final String bracketedQueryValueAsCSV = Arrays.asList(cleansedValue.split(",")).toString();
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

    public static List<byte[]> chunkifyByteArray(final byte[] source, final int numberOfChunks) {
        final int byteSizePerFrame = (source.length / numberOfChunks);
        final List<byte[]> result = new ArrayList<>();

        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + byteSizePerFrame);
            result.add(Arrays.copyOfRange(source, start, end));
            start += byteSizePerFrame;
        }

        return result;
    }
}
