package io.github.azagniotov.stubby4j.stubs.websocket;

import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;

import java.io.File;

import static io.github.azagniotov.stubby4j.utils.StringUtils.newStringUtf8;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.DELAY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.MESSAGE_TYPE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.SERVER_RESPONSE_POLICY;

public class StubWebSocketServerResponse implements ReflectableStub {

    private final StubWebSocketMessageType messageType;
    private final StubWebSocketServerResponsePolicy strategy;
    private final String body;
    private final File file;
    private final long delay;
    private final byte[] fileBytes;
    private final String webSocketServerResponseAsYAML;

    private StubWebSocketServerResponse(
            final StubWebSocketMessageType messageType,
            final StubWebSocketServerResponsePolicy strategy,
            final String body,
            final File file,
            final long delay,
            final String webSocketServerResponseAsYAML) {
        this.messageType = messageType;
        this.strategy = strategy;
        this.body = body;
        this.file = file;
        this.delay = delay;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.webSocketServerResponseAsYAML = webSocketServerResponseAsYAML;
    }

    public StubWebSocketMessageType getMessageType() {
        return messageType;
    }

    public StubWebSocketServerResponsePolicy getStrategy() {
        return strategy;
    }

    public String getBodyAsString() {
        if (fileBytes.length == 0) {
            return FileUtils.enforceSystemLineSeparator(body);
        }
        final String utf8FileContent = newStringUtf8(fileBytes);
        return FileUtils.enforceSystemLineSeparator(utf8FileContent);
    }

    public byte[] getBodyAsBytes() {
        return fileBytes;
    }

    public byte[] getFile() {
        return fileBytes;
    }

    public String getWebSocketServerResponseAsYAML() {
        return webSocketServerResponseAsYAML;
    }

    private byte[] getFileBytes() {
        try {
            return FileUtils.fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public long getDelay() {
        return delay;
    }

    public static final class Builder extends AbstractBuilder<StubWebSocketServerResponse> {

        private StubWebSocketMessageType messageType;
        private StubWebSocketServerResponsePolicy strategy;
        private String body;
        private File file;
        private String delay;
        private String webSocketServerResponseAsYAML;

        public Builder() {
            super();
            reset();
        }

        public Builder withMessageType(final String messageType) {
            this.messageType = StubWebSocketMessageType.ofNullableProperty(messageType)
                    .orElseThrow(() -> new IllegalArgumentException(messageType));

            return this;
        }

        public Builder withStrategy(final String strategy) {
            this.strategy = StubWebSocketServerResponsePolicy.ofNullableProperty(strategy)
                    .orElseThrow(() -> new IllegalArgumentException(strategy));

            return this;
        }

        public Builder withBody(final String body) {
            this.body = body;

            return this;
        }

        public Builder withFile(final File file) {
            this.file = file;

            return this;
        }

        public Builder withDelay(final String delay) {
            this.delay = delay;

            return this;
        }

        public Builder withWebSocketServerResponseAsYAML(final String webSocketServerResponseAsYAML) {
            this.webSocketServerResponseAsYAML = webSocketServerResponseAsYAML;

            return this;
        }

        @Override
        public String yamlFamilyName() {
            return ConfigurableYAMLProperty.SERVER_RESPONSE.toString();
        }

        @Override
        public StubWebSocketServerResponse build() {
            this.body = getStaged(String.class, BODY, body);
            this.file = getStaged(File.class, FILE, file);
            this.delay = getStaged(String.class, DELAY, delay);
            this.messageType = getStaged(StubWebSocketMessageType.class, MESSAGE_TYPE, messageType);
            this.strategy = getStaged(StubWebSocketServerResponsePolicy.class, SERVER_RESPONSE_POLICY, strategy);

            final long delayAsLong = this.delay.trim().equals("") ? 0 : Long.parseLong(this.delay);
            final StubWebSocketServerResponse webSocketServerResponse = new StubWebSocketServerResponse(
                    messageType,
                    strategy,
                    body,
                    file,
                    delayAsLong,
                    webSocketServerResponseAsYAML);

            reset();

            this.fieldNameAndValues.clear();

            return webSocketServerResponse;
        }

        private void reset() {
            this.body = null;
            this.file = null;
            this.delay = "";
            this.messageType = null;
            this.strategy = null;
            this.webSocketServerResponseAsYAML = null;
        }
    }
}
