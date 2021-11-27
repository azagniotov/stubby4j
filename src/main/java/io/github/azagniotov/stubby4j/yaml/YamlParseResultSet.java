package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.proxy.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.websocket.StubWebSocketConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YamlParseResultSet {

    private final List<StubHttpLifecycle> stubs;
    private final Map<String, StubHttpLifecycle> uuidToStubs;
    private final Map<String, StubProxyConfig> proxyConfigs;
    private final Map<String, StubWebSocketConfig> webSocketConfigs;

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs,
                              final Map<String, StubHttpLifecycle> uuidToStubs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
        this.proxyConfigs = new HashMap<>();
        this.webSocketConfigs = new LinkedHashMap<>();
    }

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs,
                              final Map<String, StubHttpLifecycle> uuidToStubs,
                              final Map<String, StubProxyConfig> proxyConfigs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
        this.proxyConfigs = proxyConfigs;
        this.webSocketConfigs = new LinkedHashMap<>();
    }

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs,
                              final Map<String, StubHttpLifecycle> uuidToStubs,
                              final Map<String, StubProxyConfig> proxyConfigs,
                              final Map<String, StubWebSocketConfig> webSocketConfigs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
        this.proxyConfigs = proxyConfigs;
        this.webSocketConfigs = webSocketConfigs;
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

    public Map<String, StubWebSocketConfig> getWebSocketConfigs() {
        return new HashMap<>(webSocketConfigs);
    }
}
