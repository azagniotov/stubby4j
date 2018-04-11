package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YamlParseResultSet {

    private final List<StubHttpLifecycle> stubs;
    private final Map<String, StubHttpLifecycle> uuidToStubs;

    public YamlParseResultSet(final List<StubHttpLifecycle> stubs, final Map<String, StubHttpLifecycle> uuidToStubs) {
        this.stubs = stubs;
        this.uuidToStubs = uuidToStubs;
    }

    public List<StubHttpLifecycle> getStubs() {
        return new LinkedList<>(stubs);
    }

    public Map<String, StubHttpLifecycle> getUuidToStubs() {
        return new HashMap<>(uuidToStubs);
    }
}
