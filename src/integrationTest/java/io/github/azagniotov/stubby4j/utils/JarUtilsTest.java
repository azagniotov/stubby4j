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

import org.junit.Test;

public class JarUtilsTest {

    @Test
    public void shouldReadManifestImplementationVersion() throws Exception {
        final String result = JarUtils.readManifestImplementationVersion();

        assertThat(result).isNotEqualTo("x.x.xx");
    }

    @Test
    public void shouldReadManifestBuiltDate() throws Exception {
        final String result = JarUtils.readManifestBuiltDate();

        assertThat(result).contains("20");
    }
}
