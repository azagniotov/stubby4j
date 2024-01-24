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

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import java.util.Optional;
import org.junit.Test;

public class CacheTest {

    @Test
    public void shouldBuildNoOpCache() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(true);

        assertThat(cache).isInstanceOf(NoOpStubHttpLifecycleCache.class);
    }

    @Test
    public void shouldBuildDefaultCache() throws Exception {
        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);

        assertThat(cache).isInstanceOf(StubHttpLifecycleCache.class);
    }

    @Test
    public void shouldClearCacheByKey() throws Exception {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);

        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetKey = "/some/url";

        cache.putIfAbsent(targetKey, stubHttpLifeCycle);
        assertThat(cache.size().get()).isEqualTo(1);

        assertThat(cache.get(targetKey)).isEqualTo(Optional.of(stubHttpLifeCycle));

        assertThat(cache.clearByKey(targetKey)).isTrue();
        assertThat(cache.size().get()).isEqualTo(0);
        assertThat(cache.get(targetKey)).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldNotClearCacheByKey() throws Exception {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(false);

        final StubHttpLifecycle stubHttpLifeCycle = new StubHttpLifecycle.Builder().build();
        final String targetHashCodeKey = "-124354548";

        cache.putIfAbsent(targetHashCodeKey, stubHttpLifeCycle);
        assertThat(cache.size().get()).isEqualTo(1);

        assertThat(cache.get(targetHashCodeKey)).isEqualTo(Optional.of(stubHttpLifeCycle));

        assertThat(cache.clearByKey("99999")).isFalse();
        assertThat(cache.size().get()).isEqualTo(1);
        assertThat(cache.get(targetHashCodeKey)).isEqualTo(Optional.of(stubHttpLifeCycle));
    }
}
