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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.ehcache.UserManagedCache;

class NoOpStubHttpLifecycleCache implements Cache<String, StubHttpLifecycle> {

    private static final AtomicInteger ATOMIC_INTEGER_ZERO = new AtomicInteger();

    NoOpStubHttpLifecycleCache() {}

    @Override
    public Optional<StubHttpLifecycle> get(final String key) {
        return Optional.empty();
    }

    @Override
    public void putIfAbsent(final String key, final StubHttpLifecycle value) {
        // NO-OP
    }

    @Override
    public boolean clearByKey(final String key) {
        return true;
    }

    @Override
    public void clear() {
        // NO-OP
    }

    @Override
    public UserManagedCache<String, StubHttpLifecycle> cache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AtomicInteger size() {
        return ATOMIC_INTEGER_ZERO;
    }
}
