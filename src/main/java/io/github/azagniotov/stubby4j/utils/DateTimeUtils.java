package io.github.azagniotov.stubby4j.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ssZ")
            .withZone(ZoneOffset.systemDefault());

    public static String systemDefault() {
        return DATE_TIME_FORMATTER.format(Instant.now());
    }

    public static String systemDefault(final long epochMilli) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMilli));
    }
}
