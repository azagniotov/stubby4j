package io.github.azagniotov.stubby4j.caching;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import org.junit.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

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
