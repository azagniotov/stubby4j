package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.http.HttpMethodExtended;
import io.github.azagniotov.stubby4j.utils.CollectionUtils;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedArrayList;
import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNotNull;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.newStringUtf8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.METHOD;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.POST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.QUERY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.URL;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toCollection;


public class StubRequest implements ReflectableStub {

    static final String HTTP_HEADER_AUTHORIZATION = "authorization";

    private final String url;
    private final String post;
    private final File file;
    private final byte[] fileBytes;
    private final List<String> method;
    private final Map<String, String> headers;
    private final Map<String, String> query;
    private final Map<String, String> regexGroups;

    private StubRequest(final String url,
                        final String post,
                        final File file,
                        final List<String> method,
                        final Map<String, String> headers,
                        final Map<String, String> query) {
        this.url = url;
        this.post = post;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.method = method;
        this.headers = headers;
        this.query = query;
        this.regexGroups = new TreeMap<>();
    }

    public final ArrayList<String> getMethod() {
        return method.stream().map(StringUtils::toUpper).collect(toCollection(ArrayList::new));
    }

    public String getUri() {
        return url;
    }

    /**
     * Returns url and query string parameters (if present), separated by a question mark.
     *
     * @return {@link String url}
     */
    public String getUrl() {
        if (getQuery().isEmpty()) {
            return url;
        }

        final String queryString = CollectionUtils.constructQueryString(query);

        return String.format("%s?%s", url, queryString);
    }

    private byte[] getFileBytes() {
        try {
            return FileUtils.fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public String getPostBody() {
        if (fileBytes.length == 0) {
            return FileUtils.enforceSystemLineSeparator(post);
        }
        final String utf8FileContent = newStringUtf8(fileBytes);
        return FileUtils.enforceSystemLineSeparator(utf8FileContent);
    }

    //Used by reflection when populating stubby admin page with stubbed information
    public String getPost() {
        return post;
    }

    public final Map<String, String> getHeaders() {
        final Map<String, String> headersCopy = new LinkedHashMap<>(headers);
        final Set<Map.Entry<String, String>> entrySet = headersCopy.entrySet();
        this.headers.clear();
        for (final Map.Entry<String, String> entry : entrySet) {
            this.headers.put(toLower(entry.getKey()), entry.getValue());
        }

        return headers;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public byte[] getFile() {
        return fileBytes;
    }

    // Just a shallow copy that protects collection from modification, the points themselves are not copied
    public Map<String, String> getRegexGroups() {
        return new TreeMap<>(regexGroups);
    }

    public File getRawFile() {
        return file;
    }

    public boolean hasHeaders() {
        return !getHeaders().isEmpty();
    }

    public boolean hasQuery() {
        return !getQuery().isEmpty();
    }

    public boolean hasPostBody() {
        return isSet(getPostBody());
    }

    boolean isSecured() {
        return getHeaders().containsKey(BASIC.asYAMLProp()) ||
                getHeaders().containsKey(BEARER.asYAMLProp()) ||
                getHeaders().containsKey(CUSTOM.asYAMLProp());
    }

    @VisibleForTesting
    StubbableAuthorizationType getStubbedAuthorizationType() {
        if (getHeaders().containsKey(BASIC.asYAMLProp())) {
            return BASIC;
        } else if (getHeaders().containsKey(BEARER.asYAMLProp())) {
            return BEARER;
        } else {
            return CUSTOM;
        }
    }

    String getStubbedHeaderAuthorization(final StubbableAuthorizationType stubbableAuthorizationType) {
        return getHeaders().get(stubbableAuthorizationType.asYAMLProp());
    }

    public String getRawHeaderAuthorization() {
        return getHeaders().get(HTTP_HEADER_AUTHORIZATION);
    }

    @VisibleForTesting
    boolean isRequestBodyStubbed() {
        return isSet(this.getPostBody()) &&
                (getMethod().contains(HttpMethod.POST.asString()) ||
                        getMethod().contains(HttpMethod.PUT.asString()) ||
                        getMethod().contains(HttpMethodExtended.PATCH.asString()));
    }

    public void compileRegexPatternsAndCache() {
        if (isSet(this.url)) {
            RegexParser.INSTANCE.compilePatternAndCache(this.url);
        }
        if (isRequestBodyStubbed()) {
            RegexParser.INSTANCE.compilePatternAndCache(getPostBody());
        }

        this.getQuery().values().forEach(RegexParser.INSTANCE::compilePatternAndCache);
        this.getHeaders().values().forEach(RegexParser.INSTANCE::compilePatternAndCache);
    }

    @Override
    public boolean equals(final Object that) {
        // The 'this' is actually the incoming asserting StubRequest, the 'that' is the stubbed one
        if (this == that) {
            return true;
        } else if (that instanceof StubRequest) {
            final StubRequest stubbedRequest = (StubRequest) that;

            if (new StubMatcher(regexGroups).matches(stubbedRequest, this)) {
                return true;
            }
        }

        return false;
    }

    @Override

    public int hashCode() {
        int result = (isNotNull(url) ? url.hashCode() : 0);
        result = 31 * result + method.hashCode();
        result = 31 * result + (isNotNull(post) ? post.hashCode() : 0);
        result = 31 * result + (isNotNull(fileBytes) && fileBytes.length != 0 ? Arrays.hashCode(fileBytes) : 0);
        result = 31 * result + headers.hashCode();
        result = 31 * result + query.hashCode();

        return result;
    }

    @Override

    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StubRequest");
        sb.append("{url=").append(url);
        sb.append(", method=").append(method);

        if (!ObjectUtils.isNull(post)) {
            sb.append(", post=").append(post);
        }
        sb.append(", query=").append(query);
        sb.append(", headers=").append(getHeaders());
        sb.append('}');

        return sb.toString();
    }

    public static final class Builder extends AbstractBuilder<StubRequest> {

        private String url;
        private List<String> method;
        private String post;
        private File file;
        private Map<String, String> headers;
        private Map<String, String> query;

        public Builder() {
            super();
            this.url = null;
            this.method = new ArrayList<>();
            this.post = null;
            this.file = null;
            this.headers = new LinkedHashMap<>();
            this.query = new LinkedHashMap<>();
        }

        public Builder withMethod(final String value) {
            if (isSet(value)) {
                method.add(value);
            }

            return this;
        }

        public Builder withMethodGet() {
            this.method.add(HttpMethod.GET.asString());

            return this;
        }

        public Builder withMethodPut() {
            this.method.add(HttpMethod.PUT.asString());

            return this;
        }

        public Builder withMethodPost() {
            this.method.add(HttpMethod.POST.asString());

            return this;
        }

        public Builder withMethodHead() {
            this.method.add(HttpMethod.HEAD.asString());

            return this;
        }

        public Builder withUrl(final String value) {
            this.url = value;

            return this;
        }

        public Builder withHeader(final String key, final String value) {
            // Although it is weird to see .valueOf invoked on string, it will convert a null value to "null"
            this.headers.put(valueOf(key), valueOf(value));

            return this;
        }

        public Builder withHeaderContentType(final String value) {
            this.headers.put("content-type", value);

            return this;
        }

        public Builder withApplicationJsonContentType() {
            this.headers.put("content-type", Common.HEADER_APPLICATION_JSON);

            return this;
        }

        public Builder withApplicationXmlContentType() {
            this.headers.put("content-type", Common.HEADER_APPLICATION_XML);

            return this;
        }

        public Builder withHeaderContentLength(final String value) {
            this.headers.put("content-length", value);

            return this;
        }

        public Builder withHeaderContentLanguage(final String value) {
            this.headers.put("content-language", value);

            return this;
        }

        public Builder withHeaderContentEncoding(final String value) {
            this.headers.put("content-encoding", value);

            return this;
        }

        public Builder withHeaderPragma(final String value) {
            this.headers.put("pragma", value);

            return this;
        }

        public Builder withYAMLHeaderAuthorizationBasic(final String value) {
            this.headers.put(StubbableAuthorizationType.BASIC.asYAMLProp(), value);

            return this;
        }

        public Builder withYAMLHeaderAuthorizationBearer(final String value) {
            this.headers.put(StubbableAuthorizationType.BEARER.asYAMLProp(), value);

            return this;
        }

        public Builder withPost(final String post) {
            this.post = post;

            return this;
        }

        public Builder withFile(final File file) {
            this.file = file;

            return this;
        }

        public Builder withQuery(final String key, final String value) {
            this.query.put(key, value);

            return this;
        }

        public Builder withQuery(final Map<String, String> query) {
            this.query.putAll(query);

            return this;
        }

        @Override
        public StubRequest build() {
            this.url = getStaged(String.class, URL, url);
            this.post = getStaged(String.class, POST, post);
            this.file = getStaged(File.class, FILE, file);
            this.method = asCheckedArrayList(getStaged(List.class, METHOD, method), String.class);
            this.headers = asCheckedLinkedHashMap(getStaged(Map.class, HEADERS, headers), String.class, String.class);
            this.query = asCheckedLinkedHashMap(getStaged(Map.class, QUERY, query), String.class, String.class);

            final StubRequest stubRequest = new StubRequest(url, post, file, method, headers, query);

            this.url = null;
            this.method = new ArrayList<>();
            this.post = null;
            this.file = null;
            this.headers = new LinkedHashMap<>();
            this.query = new LinkedHashMap<>();
            this.fieldNameAndValues.clear();

            return stubRequest;
        }
    }
}
