package io.github.azagniotov.stubby4j.stubs.websocket;

import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.github.azagniotov.stubby4j.utils.StringUtils.splitCsv;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.DESCRIPTION;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.ON_OPEN_SERVER_RESPONSE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.SUB_PROTOCOLS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.URL;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.UUID;

public class StubWebSocketConfig implements ReflectableStub {

    private final String uuid;
    private final String description;
    private final Set<String> subProtocols;
    private final String url;
    private final StubWebSocketServerResponse onOpenServerResponse;
    private final List<StubWebSocketOnMessageLifeCycle> onMessageLifeCycles;
    private final String webSocketConfigAsYAML;

    private StubWebSocketConfig(final String uuid,
                                final String description,
                                final String url,
                                final Set<String> subProtocols,
                                final StubWebSocketServerResponse onOpenServerResponse,
                                final List<StubWebSocketOnMessageLifeCycle> onMessageLifeCycles,
                                final String webSocketConfigAsYAML) {
        this.uuid = uuid;
        this.description = description;
        this.url = url;
        this.subProtocols = Collections.unmodifiableSet(subProtocols);
        this.onOpenServerResponse = onOpenServerResponse;
        this.onMessageLifeCycles = onMessageLifeCycles;
        this.webSocketConfigAsYAML = webSocketConfigAsYAML;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getSubProtocols() {
        return subProtocols;
    }

    public String getUrl() {
        return url;
    }

    public StubWebSocketServerResponse getOnOpenServerResponse() {
        return onOpenServerResponse;
    }

    public final List<StubWebSocketOnMessageLifeCycle> getOnMessage() {
        return onMessageLifeCycles;
    }

    public String getWebSocketConfigAsYAML() {
        return webSocketConfigAsYAML;
    }

    public static final class Builder extends AbstractBuilder<StubWebSocketConfig> {

        private String uuid;
        private String description;
        private String subProtocols;
        private String url;
        private StubWebSocketServerResponse onOpenServerResponse;
        private List<StubWebSocketOnMessageLifeCycle> onMessageLifeCycles;
        private String webSocketConfigAsYAML;

        public Builder() {
            super();
            reset();
        }

        public Builder withUuid(final String uuid) {
            this.uuid = uuid;

            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;

            return this;
        }

        public Builder withSubProtocols(final String subProtocols) {
            this.subProtocols = subProtocols;

            return this;
        }

        public Builder withUrl(final String url) {
            this.url = url;

            return this;
        }

        public Builder withOnOpenServerResponse(final StubWebSocketServerResponse onOpenServerResponse) {
            this.onOpenServerResponse = onOpenServerResponse;

            return this;
        }

        public Builder withOnMessage(final List<StubWebSocketOnMessageLifeCycle> onMessageLifeCycles) {
            this.onMessageLifeCycles = onMessageLifeCycles;

            return this;
        }

        public Builder withWebSocketConfigAsYAML(final String webSocketConfigAsYAML) {
            this.webSocketConfigAsYAML = webSocketConfigAsYAML;

            return this;
        }

        @Override
        public String yamlFamilyName() {
            return ConfigurableYAMLProperty.WEB_SOCKET.toString();
        }

        @Override
        public StubWebSocketConfig build() {
            this.description = getStaged(String.class, DESCRIPTION, description);
            this.uuid = getStaged(String.class, UUID, uuid);
            this.url = getStaged(String.class, URL, url);
            this.subProtocols = getStaged(String.class, SUB_PROTOCOLS, subProtocols);
            this.onOpenServerResponse = getStaged(StubWebSocketServerResponse.class, ON_OPEN_SERVER_RESPONSE, onOpenServerResponse);

            final StubWebSocketConfig stubWebSocketConfig = new StubWebSocketConfig(
                    uuid,
                    description,
                    url,
                    splitCsv(this.subProtocols),
                    onOpenServerResponse,
                    onMessageLifeCycles,
                    webSocketConfigAsYAML);

            reset();

            this.fieldNameAndValues.clear();

            return stubWebSocketConfig;
        }

        private void reset() {
            this.uuid = null;
            this.description = null;
            this.webSocketConfigAsYAML = null;
            this.url = null;
            this.subProtocols = null;
            this.onOpenServerResponse = null;
            this.onMessageLifeCycles = new ArrayList<>();
        }
    }
}
