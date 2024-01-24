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

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;

class StubHttpLifecycleCache implements Cache<String, StubHttpLifecycle> {

    private final AtomicInteger cacheSize;
    private final UserManagedCache<String, StubHttpLifecycle> localCache;

    StubHttpLifecycleCache(final long cacheEntryLifetimeSeconds) {
        final Duration timeToLiveExpiration = Duration.ofSeconds(cacheEntryLifetimeSeconds);

        this.localCache = UserManagedCacheBuilder.newUserManagedCacheBuilder(String.class, StubHttpLifecycle.class)
                .withResourcePools(ResourcePoolsBuilder.heap(500L))
                .identifier(this.getClass().getSimpleName())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(timeToLiveExpiration))
                .build(true);

        this.cacheSize = new AtomicInteger(0);
    }

    @Override
    public UserManagedCache<String, StubHttpLifecycle> cache() {
        return localCache;
    }

    @Override
    public AtomicInteger size() {
        return cacheSize;
    }
}
