package io.github.azagniotov.stubby4j.utils;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.google.common.truth.Truth.assertThat;

public class DateTimeUtilsTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ssZ")
            .withZone(ZoneOffset.systemDefault());

    @Test
    public void systemDefault() throws Exception {
        final String localNow = DATE_TIME_FORMATTER.format(Instant.now());
        final String systemDefault = DateTimeUtils.systemDefault();

        assertThat(localNow).isEqualTo(systemDefault);
    }

    @Test
    public void systemDefaultFromMillis() throws Exception {
        final long currentTimeMillis = System.currentTimeMillis();
        final String localNow = DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(currentTimeMillis));

        final String systemDefault = DateTimeUtils.systemDefault(currentTimeMillis);

        assertThat(localNow).isEqualTo(systemDefault);
    }
}
