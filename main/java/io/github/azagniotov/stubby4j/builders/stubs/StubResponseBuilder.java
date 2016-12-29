package io.github.azagniotov.stubby4j.builders.stubs;

import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus.Code;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static org.eclipse.jetty.http.HttpStatus.getCode;


public final class StubResponseBuilder implements StubReflectiveBuilder<StubResponse> {

    private Code httpStatusCode;
    private Map<String, Object> fieldNameAndValues;
    private String status;
    private String body;
    private File file;
    private String latency;
    private Map<String, String> headers;

    public StubResponseBuilder() {
        this.status = null;
        this.httpStatusCode = null;
        this.body = null;
        this.file = null;
        this.latency = null;
        this.headers = new LinkedHashMap<>();
        this.fieldNameAndValues = new HashMap<>();
    }

    public StubResponseBuilder okResponseWithBody(final String body) {
        this.httpStatusCode = Code.OK;
        this.body = body;

        return this;
    }

    public StubResponseBuilder withHttpStatusCode(final Code httpStatusCode) {
        this.httpStatusCode = httpStatusCode;

        return this;
    }

    public StubResponseBuilder withBody(final String body) {
        this.body = body;

        return this;
    }

    public StubResponseBuilder withFile(final File file) {
        this.file = file;

        return this;
    }

    @Override
    public void stage(final String fieldName, final Object fieldValue) {
        fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
    }

    @Override
    public StubResponse build() throws Exception {
        ReflectionUtils.injectObjectFields(this, fieldNameAndValues);
        final StubResponse stubResponse = new StubResponse(getHttpStatusCode(), body, file, latency, headers);

        this.status = null;
        this.httpStatusCode = null;
        this.body = null;
        this.file = null;
        this.latency = null;
        this.headers = new LinkedHashMap<>();
        this.fieldNameAndValues = new HashMap<>();

        return stubResponse;
    }

    private Code getHttpStatusCode() {
        return ObjectUtils.isNull(this.status) ?
                (ObjectUtils.isNull(this.httpStatusCode) ? Code.OK : this.httpStatusCode) :
                getCode(parseInt(this.status));
    }
}
