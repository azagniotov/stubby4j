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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

public class DateTimeUtilsTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ").withZone(ZoneOffset.systemDefault());

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
