package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty;
import org.eclipse.jetty.http.HttpStatus.Code;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.generics.TypeSafeConverter.as;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.utils.FileUtils.fileToBytes;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNotNull;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.LATENCY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.STATUS;
import static java.lang.Integer.parseInt;
import static org.eclipse.jetty.http.HttpStatus.getCode;


public class StubResponse implements ReflectableStub {

    public static final String STUBBY_RESOURCE_ID_HEADER = "x-stubby-resource-id";

    private final Code httpStatusCode;
    private final String body;
    private final File file;
    private final byte[] fileBytes;
    private final String latency;
    private final Map<String, String> headers;

    private StubResponse(final Code httpStatusCode,
                         final String body,
                         final File file,
                         final String latency,
                         final Map<String, String> headers) {
        this.httpStatusCode = httpStatusCode;
        this.body = body;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.latency = latency;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<>() : headers;
    }

    public static StubResponse okResponse() {
        return new StubResponse(Code.OK, null, null, null, null);
    }

    public static StubResponse notFoundResponse() {
        return new StubResponse(Code.NOT_FOUND, null, null, null, null);
    }

    public static StubResponse unauthorizedResponse() {
        return new StubResponse(Code.UNAUTHORIZED, null, null, null, null);
    }

    public static StubResponse redirectResponse(final Optional<StubResponse> stubResponseOptional) {
        if (!stubResponseOptional.isPresent()) {
            return new StubResponse(Code.MOVED_TEMPORARILY, null, null, null, null);
        }
        final StubResponse foundStubResponse = stubResponseOptional.get();
        return new StubResponse(
                foundStubResponse.getHttpStatusCode(),
                foundStubResponse.getBody(),
                foundStubResponse.getRawFile(),
                foundStubResponse.getLatency(),
                foundStubResponse.getHeaders());
    }

    public Code getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getBody() {
        return (StringUtils.isSet(body) ? body : "");
    }

    public boolean isRecordingRequired() {
        final String body = getBody();
        return StringUtils.toLower(body).startsWith("http");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getLatency() {
        return latency;
    }

    /**
     * Used by reflection when populating stubby admin page with stubbed information
     */
    public byte[] getFile() {
        return fileBytes;
    }

    public File getRawFile() {
        return file;
    }

    public String getRawFileAbsolutePath() {
        return file.getAbsolutePath();
    }

    public byte[] getResponseBodyAsBytes() {

        if (fileBytes.length == 0) {
            return StringUtils.getBytesUtf8(getBody());
        }
        return fileBytes;
    }

    public boolean isBodyContainsTemplateTokens() {
        final boolean isFileTemplate = fileBytes.length != 0 && isTemplateFile();
        return isFileTemplate || StringUtils.isTokenized(getBody());
    }

    public boolean isFilePathContainsTemplateTokens() {
        try {
            return isFilePathContainTemplateTokens(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private boolean isTemplateFile() {
        try {
            return FileUtils.isTemplateFile(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private byte[] getFileBytes() {
        try {
            return fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public boolean hasHeaderLocation() {
        return getHeaders().containsKey("location");
    }

    void addResourceIDHeader(final int resourceIndex) {
        getHeaders().put(STUBBY_RESOURCE_ID_HEADER, String.valueOf(resourceIndex));
    }

    String getResourceIDHeader() {
        return getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER);
    }

    public static final class Builder implements ReflectableStubBuilder<StubResponse> {

        private Map<ConfigurableYAMLProperty, Object> fieldNameAndValues;
        private String status;
        private String body;
        private File file;
        private String latency;
        private Map<String, String> headers;

        public Builder() {
            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
            this.fieldNameAndValues = new HashMap<>();
        }

        public Builder emptyWithBody(final String body) {
            this.status = String.valueOf(Code.OK.getCode());
            this.body = body;

            return this;
        }

        public Builder withHttpStatusCode(final Code httpStatusCode) {
            this.status = String.valueOf(httpStatusCode.getCode());

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

        @Override
        public void stage(final Optional<ConfigurableYAMLProperty> fieldNameOptional, final Object fieldValue) {
            if (fieldNameOptional.isPresent() && isNotNull(fieldValue)) {
                this.fieldNameAndValues.put(fieldNameOptional.get(), fieldValue);
            }
        }

        @Override
        public <E> E get(final Class<E> clazzor, final ConfigurableYAMLProperty property, E orElse) {
            return this.fieldNameAndValues.containsKey(property) ? as(clazzor, fieldNameAndValues.get(property)) : orElse;
        }

        @Override
        public StubResponse build() {
            this.status = get(String.class, STATUS, status);
            this.body = get(String.class, BODY, body);
            this.file = get(File.class, FILE, file);
            this.latency = get(String.class, LATENCY, latency);
            this.headers = asCheckedLinkedHashMap(get(Map.class, HEADERS, headers), String.class, String.class);

            final StubResponse stubResponse = new StubResponse(getHttpStatusCode(), body, file, latency, headers);

            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
            this.fieldNameAndValues = new HashMap<>();

            return stubResponse;
        }

        @VisibleForTesting
        Code getHttpStatusCode() {
            return ObjectUtils.isNull(this.status) ? Code.OK : getCode(parseInt(this.status));
        }
    }
}
