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

package io.github.azagniotov.stubby4j.caching;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import java.util.Optional;
import org.junit.Test;

public class NoOpStubHttpLifecycleCacheTest {

    @Test
    public void putIfAbsentAndGet() {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        assertThat(cache.size().get()).isEqualTo(0);

        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";

        cache.putIfAbsent(targetKey, stubHttpLifeCycle);

        assertThat(cache.size().get()).isEqualTo(0);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
    }

    @Test
    public void clearByKey() {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);

        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";

        assertThat(cache.clearByKey(targetKey)).isTrue();
        cache.putIfAbsent(targetKey, stubHttpLifeCycle);

        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
        assertThat(cache.clearByKey(targetKey)).isTrue();
    }

    @Test
    public void cache() {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);
        assertThrows(UnsupportedOperationException.class, cache::cache);
    }
}
