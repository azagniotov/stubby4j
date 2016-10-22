/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.CollectionUtils;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlProperties;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.github.azagniotov.stubby4j.utils.StringUtils.escapeSpecialRegexCharacters;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isNotSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isWithinSquareBrackets;
import static io.github.azagniotov.stubby4j.utils.StringUtils.newStringUtf8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;
import static io.github.azagniotov.stubby4j.utils.StringUtils.toUpper;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BASIC;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BEARER;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.CUSTOM;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubRequest {

    public static final String HTTP_HEADER_AUTHORIZATION = "authorization";

    private final RegexParser regexParser = RegexParser.INSTANCE;

    private final String url;
    private final String post;
    private final File file;
    private final byte[] fileBytes;
    private final List<String> method;
    private final Map<String, String> headers;
    private final Map<String, String> query;
    private final Map<String, String> regexGroups;

    public StubRequest(final String url,
                       final String post,
                       final File file,
                       final List<String> method,
                       final Map<String, String> headers,
                       final Map<String, String> query) {
        this.url = url;
        this.post = post;
        this.file = file;
        this.fileBytes = ObjectUtils.isNull(file) ? new byte[]{} : getFileBytes();
        this.method = ObjectUtils.isNull(method) ? new ArrayList<>() : method;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<>() : headers;
        this.query = ObjectUtils.isNull(query) ? new LinkedHashMap<>() : query;
        this.regexGroups = new TreeMap<>();
    }

    public final ArrayList<String> getMethod() {
        final ArrayList<String> uppercase = new ArrayList<>(method.size());

        for (final String string : method) {
            uppercase.add(toUpper(string));
        }

        return uppercase;
    }

    public void addMethod(final String newMethod) {
        if (isSet(newMethod)) {
            method.add(newMethod);
        }
    }

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

    public boolean isSecured() {
        return getHeaders().containsKey(BASIC.asYamlProp()) ||
                getHeaders().containsKey(BEARER.asYamlProp()) ||
                getHeaders().containsKey(CUSTOM.asYamlProp());
    }

    @VisibleForTesting
    StubAuthorizationTypes getStubbedAuthorizationTypeHeader() {
        if (getHeaders().containsKey(BASIC.asYamlProp())) {
            return BASIC;
        } else if (getHeaders().containsKey(BEARER.asYamlProp())) {
            return BEARER;
        } else {
            return CUSTOM;
        }
    }

    String getStubbedAuthorizationHeaderValue(final StubAuthorizationTypes stubbedAuthorizationHeaderType) {
        return getHeaders().get(stubbedAuthorizationHeaderType.asYamlProp());
    }

    public String getRawAuthorizationHttpHeader() {
        return getHeaders().get(HTTP_HEADER_AUTHORIZATION);
    }

    public static StubRequest newStubRequest() {
        return new StubRequest(null, null, null, null, null, null);
    }

    public static StubRequest newStubRequest(final String url, final String post) {
        return new StubRequest(url, post, null, null, null, null);
    }

    public static StubRequest createFromHttpServletRequest(final HttpServletRequest request) throws IOException {
        final StubRequest assertionRequest = StubRequest.newStubRequest(request.getPathInfo(),
                HandlerUtils.extractPostRequestBody(request, "stubs"));
        assertionRequest.addMethod(request.getMethod());

        final Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
        final List<String> headerNames = ObjectUtils.isNotNull(headerNamesEnumeration)
                ? Collections.list(request.getHeaderNames()) : new LinkedList<String>();
        for (final String headerName : headerNames) {
            final String headerValue = request.getHeader(headerName);
            assertionRequest.getHeaders().put(toLower(headerName), headerValue);
        }

        assertionRequest.getQuery().putAll(CollectionUtils.constructParamMap(request.getQueryString()));
        ConsoleUtils.logAssertingRequest(assertionRequest);

        return assertionRequest;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof StubRequest) {
            final StubRequest stubbedRequest = (StubRequest) o;

            if (!urlsMatch(stubbedRequest.url, this.url)) {
                ANSITerminal.error(String.format("Failed match for URL %s WITH %s", stubbedRequest.url, this.url));
                return false;
            }
            if (!listsIntersect(stubbedRequest.getMethod(), this.getMethod())) {
                ANSITerminal.error(String.format("Failed match for METHOD %s WITH %s", stubbedRequest.getMethod(), this.getMethod()));
                return false;
            }
            if (!postBodiesMatch(stubbedRequest.isPostStubbed(), stubbedRequest.getPostBody(), this.getPostBody())) {
                ANSITerminal.error(String.format("Failed match for POST BODY %s WITH %s", stubbedRequest.getPostBody(), this.getPostBody()));
                return false;
            }
            if (!headersMatch(stubbedRequest.getHeaders(), this.getHeaders())) {
                ANSITerminal.error(String.format("Failed match for HEADERS %s WITH %s", stubbedRequest.getHeaders(), this.getHeaders()));
                return false;
            }
            if (!queriesMatch(stubbedRequest.getQuery(), this.getQuery())) {
                ANSITerminal.error(String.format("Failed match for QUERY %s WITH %s", stubbedRequest.getQuery(), this.getQuery()));
                return false;
            }
            return true;
        }

        return false;
    }

    @VisibleForTesting
    boolean isPostStubbed() {
        return isSet(this.getPostBody()) && (getMethod().contains("POST") || getMethod().contains("PUT"));
    }

    private boolean urlsMatch(final String stubbedUrl, final String assertingUrl) {
        return stringsMatch(stubbedUrl, assertingUrl, YamlProperties.URL);
    }

    private boolean postBodiesMatch(final boolean isPostStubbed, final String stubbedPostBody, final String assertingPostBody) {
        if (isPostStubbed) {
            final String assertingContentType = this.getHeaders().get("content-type");
            if (isNotSet(assertingPostBody)) {
                return false;
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_JSON)) {
                return jsonMatch(stubbedPostBody, assertingPostBody);
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_XML)) {
                return xmlMatch(stubbedPostBody, assertingPostBody);
            } else {
                return stringsMatch(stubbedPostBody, assertingPostBody, YamlProperties.POST);
            }
        }

        return true;
    }

    private boolean queriesMatch(final Map<String, String> stubbedQuery, final Map<String, String> assertingQuery) {
        return mapsMatch(stubbedQuery, assertingQuery, YamlProperties.QUERY);
    }

    private boolean headersMatch(final Map<String, String> stubbedHeaders, final Map<String, String> assertingHeaders) {
        final Map<String, String> stubbedHeadersCopy = new HashMap<>(stubbedHeaders);
        for (final StubAuthorizationTypes authorizationType : StubAuthorizationTypes.values()) {
            // auth header is dealt with in StubbedDataManager after request is matched
            stubbedHeadersCopy.remove(authorizationType.asYamlProp());
        }
        return mapsMatch(stubbedHeadersCopy, assertingHeaders, YamlProperties.HEADERS);
    }

    @VisibleForTesting
    boolean mapsMatch(final Map<String, String> stubbedMappings, final Map<String, String> assertingMappings, final String mapName) {
        if (stubbedMappings.isEmpty()) {
            return true;
        } else if (assertingMappings.isEmpty()) {
            return false;
        }

        final Map<String, String> stubbedMappingsCopy = new HashMap<>(stubbedMappings);
        final Map<String, String> assertingMappingsCopy = new HashMap<>(assertingMappings);

        for (final Map.Entry<String, String> stubbedMappingEntry : stubbedMappingsCopy.entrySet()) {
            final boolean containsRequiredParam = assertingMappingsCopy.containsKey(stubbedMappingEntry.getKey());
            if (!containsRequiredParam) {
                return false;
            } else {
                final String assertingValue = assertingMappingsCopy.get(stubbedMappingEntry.getKey());
                final String templateTokenName = String.format("%s.%s", mapName, stubbedMappingEntry.getKey());

                if (!stringsMatch(stubbedMappingEntry.getValue(), assertingValue, templateTokenName)) {
                    return false;
                }
            }
        }

        return true;
    }

    @VisibleForTesting
    boolean stringsMatch(final String stubbedValue, final String assertingValue, final String templateTokenName) {
        if (isNotSet(stubbedValue)) {
            return true;
        } else if (isNotSet(assertingValue)) {
            return false;
        } else if (isWithinSquareBrackets(stubbedValue)) {
            return stubbedValue.equals(assertingValue);
        } else {
            return regexMatch(stubbedValue, assertingValue, templateTokenName);
        }
    }

    private boolean regexMatch(final String stubbedValue, final String assertingValue, final String templateTokenName) {
        return regexParser.match(stubbedValue, assertingValue, templateTokenName, regexGroups);
    }

    @VisibleForTesting
    boolean listsIntersect(final List<String> stubbedArray, final List<String> assertingArray) {
        if (stubbedArray.isEmpty()) {
            return true;
        } else if (!assertingArray.isEmpty()) {
            for (final String entry : assertingArray) {
                if (stubbedArray.contains(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean jsonMatch(final String stubbedJsony, final String assertingJson) {
        try {
            boolean passed = JSONCompare.compareJSON(stubbedJsony, assertingJson, JSONCompareMode.NON_EXTENSIBLE).passed();
            if (passed) {
                return true;
            } else {
                final String escapedStubbedPostBody = escapeSpecialRegexCharacters(stubbedJsony);
                return regexMatch(escapedStubbedPostBody, assertingJson, YamlProperties.POST);
            }
        } catch (final JSONException e) {
            final String escapedStubbedPostBody = escapeSpecialRegexCharacters(stubbedJsony);
            return regexMatch(escapedStubbedPostBody, assertingJson, YamlProperties.POST);
        }
    }

    private boolean xmlMatch(final String stubbedXml, final String assertingXml) {
        try {
            final Diff diff = new Diff(stubbedXml, assertingXml);
            diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

            return (diff.similar() || diff.identical());
        } catch (SAXException | IOException e) {
            return false;
        }
    }

    public void computeRegexPatterns() {
        if (StringUtils.isSet(this.url)) {
            regexParser.compilePatternAndCache(this.url);
        }
        if (isPostStubbed()) {
            regexParser.compilePatternAndCache(getPostBody());
        }

        this.getQuery().values().forEach(regexParser::compilePatternAndCache);
    }

    @Override
    @CoberturaIgnore
    public int hashCode() {
        int result = (ObjectUtils.isNotNull(url) ? url.hashCode() : 0);
        result = 31 * result + method.hashCode();
        result = 31 * result + (ObjectUtils.isNotNull(post) ? post.hashCode() : 0);
        result = 31 * result + (ObjectUtils.isNotNull(fileBytes) && fileBytes.length != 0 ? Arrays.hashCode(fileBytes) : 0);
        result = 31 * result + headers.hashCode();
        result = 31 * result + query.hashCode();

        return result;
    }

    @Override
    @CoberturaIgnore
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
}
