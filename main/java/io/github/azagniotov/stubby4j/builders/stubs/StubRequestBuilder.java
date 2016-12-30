package io.github.azagniotov.stubby4j.builders.stubs;

import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.stubs.StubAuthorizationTypes;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import org.eclipse.jetty.http.HttpMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class StubRequestBuilder implements StubReflectiveBuilder<StubRequest> {

    private Map<String, Object> fieldNameAndValues;
    private String url;
    private List<String> method;
    private String post;
    private File file;
    private Map<String, String> headers;
    private Map<String, String> query;

    public StubRequestBuilder() {
        this.url = null;
        this.method = new ArrayList<>();
        this.post = null;
        this.file = null;
        this.headers = new LinkedHashMap<>();
        this.query = new LinkedHashMap<>();
        this.fieldNameAndValues = new HashMap<>();
    }

    public StubRequestBuilder withMethod(final String value) {
        this.method.add(value);

        return this;
    }

    public StubRequestBuilder withMethodGet() {
        this.method.add(HttpMethod.GET.asString());

        return this;
    }

    public StubRequestBuilder withMethodPut() {
        this.method.add(HttpMethod.PUT.asString());

        return this;
    }

    public StubRequestBuilder withMethodPost() {
        this.method.add(HttpMethod.POST.asString());

        return this;
    }

    public StubRequestBuilder withMethodHead() {
        this.method.add(HttpMethod.HEAD.asString());

        return this;
    }

    public StubRequestBuilder withUrl(final String value) {
        this.url = value;

        return this;
    }

    public StubRequestBuilder withHeaders(final String key, final String value) {
        this.headers.put(key, value);

        return this;
    }

    public StubRequestBuilder withHeaderContentType(final String value) {
        this.headers.put("content-type", value);

        return this;
    }

    public StubRequestBuilder withApplicationJsonContentType() {
        this.headers.put("content-type", Common.HEADER_APPLICATION_JSON);

        return this;
    }

    public StubRequestBuilder withApplicationXmlContentType() {
        this.headers.put("content-type", Common.HEADER_APPLICATION_XML);

        return this;
    }

    public StubRequestBuilder withHeaderContentLength(final String value) {
        this.headers.put("content-length", value);

        return this;
    }

    public StubRequestBuilder withHeaderContentLanguage(final String value) {
        this.headers.put("content-language", value);

        return this;
    }

    public StubRequestBuilder withHeaderContentEncoding(final String value) {
        this.headers.put("content-encoding", value);

        return this;
    }

    public StubRequestBuilder withHeaderPragma(final String value) {
        this.headers.put("pragma", value);

        return this;
    }

    public StubRequestBuilder withHeaderAuthorizationBasic(final String value) {
        this.headers.put(StubAuthorizationTypes.BASIC.asYamlProp(), value);

        return this;
    }

    public StubRequestBuilder withHeaderAuthorizationBearer(final String value) {
        this.headers.put(StubAuthorizationTypes.BEARER.asYamlProp(), value);

        return this;
    }

    public StubRequestBuilder withHeaderLocation(final String value) {
        this.headers.put("location", value);

        return this;
    }

    public StubRequestBuilder withPost(final String post) {
        this.post = post;

        return this;
    }

    public StubRequestBuilder withFile(final File file) {
        this.file = file;

        return this;
    }

    public StubRequestBuilder withQuery(final String key, final String value) {
        this.query.put(key, value);

        return this;
    }

    @Override
    public void stage(final String fieldName, final Object fieldValue) {
        fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
    }

    @Override
    public StubRequest build() throws Exception {
        ReflectionUtils.injectObjectFields(this, fieldNameAndValues);
        final StubRequest stubRequest = new StubRequest(url, post, file, method, headers, query);

        this.url = null;
        this.method = new ArrayList<>();
        this.post = null;
        this.file = null;
        this.headers = new LinkedHashMap<>();
        this.query = new LinkedHashMap<>();
        this.fieldNameAndValues = new HashMap<>();

        return stubRequest;
    }
}
