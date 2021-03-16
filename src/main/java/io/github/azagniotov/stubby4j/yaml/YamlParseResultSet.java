package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YamlParseResultSet {

    private final List<StubHttpLifecycle> stubs;
    private final Map<String, StubHttpLifecycle> uuidToStubs;
    private final Map<String, StubProxyConfig> proxyConfigs;

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs,
                              final Map<String, StubHttpLifecycle> uuidToStubs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
        this.proxyConfigs = new HashMap<>();
    }

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs,
                              final Map<String, StubHttpLifecycle> uuidToStubs,
                              final Map<String, StubProxyConfig> proxyConfigs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
        this.proxyConfigs = proxyConfigs;
    }

    public List<StubHttpLifecycle> getStubs() {
        return new LinkedList<>(stubs);
    }

    public Map<String, StubHttpLifecycle> getUuidToStubs() {
        return new HashMap<>(uuidToStubs);
    }

    public Map<String, StubProxyConfig> getProxyConfigs() {
        return new HashMap<>(proxyConfigs);
    }
}
