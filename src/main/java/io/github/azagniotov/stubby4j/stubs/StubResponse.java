package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus.Code;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.utils.FileUtils.fileToBytes;
import static io.github.azagniotov.stubby4j.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNull;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.LATENCY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.STATUS;
import static java.lang.Integer.parseInt;
import static org.eclipse.jetty.http.HttpStatus.getCode;


public class StubResponse implements ReflectableStub {

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
        this.fileBytes = isNull(file) ? new byte[]{} : getFileBytes();
        this.latency = latency;
        this.headers = isNull(headers) ? new LinkedHashMap<>() : headers;
    }

    public static StubResponse okResponse() {
        return new StubResponse.Builder().build();
    }

    public static StubResponse notFoundResponse() {
        return new StubResponse.Builder().withHttpStatusCode(Code.NOT_FOUND).build();
    }

    public static StubResponse unauthorizedResponse() {
        return new StubResponse.Builder().withHttpStatusCode(Code.UNAUTHORIZED).build();
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
            // This checks if the 'file' key that was stubbed(!) is tokenized, i.e.:
            // file: ../html/<% url.1 %>.html
            return isFilePathContainTemplateTokens(file);
        } catch (Exception e) {
            return false;
        }
    }


    private boolean isTemplateFile() {
        try {
            return FileUtils.isTemplateFile(file);
        } catch (Exception e) {
            return false;
        }
    }


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
        getHeaders().put(Common.HEADER_X_STUBBY_RESOURCE_ID, String.valueOf(resourceIndex));
    }

    String getResourceIDHeader() {
        return getHeaders().get(Common.HEADER_X_STUBBY_RESOURCE_ID);
    }

    public static final class Builder extends AbstractBuilder<StubResponse> {

        private String status;
        private String body;
        private File file;
        private String latency;
        private Map<String, String> headers;

        public Builder() {
            super();
            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
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

        public Builder withHeaders(final Map<String, String> headers) {
            this.headers = headers;

            return this;
        }

        @Override
        public StubResponse build() {
            this.status = getStaged(String.class, STATUS, status);
            this.body = getStaged(String.class, BODY, body);
            this.file = getStaged(File.class, FILE, file);
            this.latency = getStaged(String.class, LATENCY, latency);
            this.headers = asCheckedLinkedHashMap(getStaged(Map.class, HEADERS, headers), String.class, String.class);

            final StubResponse stubResponse = new StubResponse(getHttpStatusCode(), body, file, latency, headers);

            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
            this.fieldNameAndValues.clear();

            return stubResponse;
        }

        @VisibleForTesting
        Code getHttpStatusCode() {
            return isNull(this.status) ? Code.OK : getCode(parseInt(this.status));
        }
    }
}
