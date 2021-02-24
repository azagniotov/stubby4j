/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.caching;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import org.junit.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

public class CacheTest {

    @Test
    public void shouldClearCacheByKey() throws Exception {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(1000L);

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
    public void shouldClearCacheByRegexKey() throws Exception {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(1000L);

        final String keyRegex = "^/resources/asn/.*$";

        final StubHttpLifecycle stubHttpLifeCycleOne = new StubHttpLifecycle.Builder().build();
        final StubHttpLifecycle stubHttpLifeCycleTwo = new StubHttpLifecycle.Builder().build();
        final StubHttpLifecycle stubHttpLifeCycleThree = new StubHttpLifecycle.Builder().build();

        cache.putIfAbsent("/resources/asn/1", stubHttpLifeCycleOne);
        cache.putIfAbsent("/resources/asn/2", stubHttpLifeCycleTwo);
        cache.putIfAbsent("/resources/asn/3", stubHttpLifeCycleThree);

        assertThat(cache.size().get()).isEqualTo(3);

        cache.clearByRegexKey(keyRegex);

        assertThat(cache.size().get()).isEqualTo(0);
        assertThat(cache.get("/resources/asn/1")).isEqualTo(Optional.empty());
        assertThat(cache.get("/resources/asn/2")).isEqualTo(Optional.empty());
        assertThat(cache.get("/resources/asn/3")).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldNotClearCacheByRegexKeyWhenRegexDoesNotMatch() throws Exception {

        final Cache<String, StubHttpLifecycle> cache = Cache.stubHttpLifecycleCache(1000L);

        final String keyRegex = "^/resources/non-matching-regex-key/.*$";

        final StubHttpLifecycle stubHttpLifeCycleOne = new StubHttpLifecycle.Builder()
                .withRequest(new StubRequest.Builder().build())
                .build();
        final StubHttpLifecycle stubHttpLifeCycleTwo = new StubHttpLifecycle.Builder()
                .withRequest(new StubRequest.Builder().build())
                .build();
        final StubHttpLifecycle stubHttpLifeCycleThree = new StubHttpLifecycle.Builder()
                .withRequest(new StubRequest.Builder().build())
                .build();

        cache.putIfAbsent("/resources/asn/1", stubHttpLifeCycleOne);
        cache.putIfAbsent("/resources/asn/2", stubHttpLifeCycleTwo);
        cache.putIfAbsent("/resources/asn/3", stubHttpLifeCycleThree);

        assertThat(cache.size().get()).isEqualTo(3);

        cache.clearByRegexKey(keyRegex);

        assertThat(cache.size().get()).isEqualTo(3);
        assertThat(cache.get("/resources/asn/1")).isEqualTo(Optional.of(stubHttpLifeCycleOne));
        assertThat(cache.get("/resources/asn/2")).isEqualTo(Optional.of(stubHttpLifeCycleTwo));
        assertThat(cache.get("/resources/asn/3")).isEqualTo(Optional.of(stubHttpLifeCycleThree));
    }
}
