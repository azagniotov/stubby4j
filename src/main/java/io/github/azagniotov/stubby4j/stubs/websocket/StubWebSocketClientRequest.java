package io.github.azagniotov.stubby4j.stubs.websocket;

import io.github.azagniotov.stubby4j.stubs.AbstractBuilder;
import io.github.azagniotov.stubby4j.stubs.ReflectableStub;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static io.github.azagniotov.stubby4j.utils.StringUtils.newStringUtf8;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.MESSAGE_TYPE;

public class StubWebSocketClientRequest implements ReflectableStub {

    private final StubWebSocketMessageType messageType;
    private final String body;
    private final File file;
    private final byte[] fileBytes;
    private final String webSocketClientRequestAsYAML;

    private StubWebSocketClientRequest(final StubWebSocketMessageType messageType,
                                       final String body,
                                       final File file,
                                       final String webSocketClientRequestAsYAML) {
        this.messageType = messageType;
        this.body = body;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.webSocketClientRequestAsYAML = webSocketClientRequestAsYAML;
    }

    public StubWebSocketMessageType getMessageType() {
        return messageType;
    }

    public String getBodyAsString() {
        if (fileBytes.length == 0) {
            return FileUtils.enforceSystemLineSeparator(body);
        }
        final String utf8FileContent = newStringUtf8(fileBytes);
        return FileUtils.enforceSystemLineSeparator(utf8FileContent);
    }

    public byte[] getBodyAsBytes() {
        if (fileBytes.length == 0) {
            return StringUtils.getBytesUtf8(body);
        }
        return fileBytes;
    }

    public byte[] getFile() {
        return fileBytes;
    }

    public String getWebSocketClientRequestAsYAML() {
        return webSocketClientRequestAsYAML;
    }

    private byte[] getFileBytes() {
        try {
            return FileUtils.fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubWebSocketClientRequest that = (StubWebSocketClientRequest) o;
        return messageType == that.messageType &&
                body.equals(that.body) &&
                file.equals(that.file) &&
                Arrays.equals(fileBytes, that.fileBytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(messageType, body, file);
        result = 31 * result + Arrays.hashCode(fileBytes);
        return result;
    }

    public static final class Builder extends AbstractBuilder<StubWebSocketClientRequest> {

        private StubWebSocketMessageType messageType;
        private String body;
        private File file;
        private String webSocketClientRequestAsYAML;

        public Builder() {
            super();
            reset();
        }

        public Builder withMessageType(final String messageType) {
            this.messageType = StubWebSocketMessageType.ofNullableProperty(messageType)
                    .orElseThrow(() -> new IllegalArgumentException(messageType));
            ;

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

        public Builder withWebSocketClientRequestAsYAML(final String webSocketClientRequestAsYAML) {
            this.webSocketClientRequestAsYAML = webSocketClientRequestAsYAML;

            return this;
        }

        @Override
        public String yamlFamilyName() {
            return ConfigurableYAMLProperty.CLIENT_REQUEST.toString();
        }

        @Override
        public StubWebSocketClientRequest build() {
            this.body = getStaged(String.class, BODY, body);
            this.file = getStaged(File.class, FILE, file);
            this.messageType = getStaged(StubWebSocketMessageType.class, MESSAGE_TYPE, messageType);

            final StubWebSocketClientRequest webSocketClientRequest = new StubWebSocketClientRequest(
                    messageType,
                    body,
                    file,
                    webSocketClientRequestAsYAML);

            reset();

            this.fieldNameAndValues.clear();

            return webSocketClientRequest;
        }

        private void reset() {
            this.body = null;
            this.file = null;
            this.messageType = null;
            this.webSocketClientRequestAsYAML = null;
        }
    }
}
