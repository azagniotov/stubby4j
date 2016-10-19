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
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.CollectionUtils;
import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.github.azagniotov.stubby4j.utils.StringUtils.buildToken;
import static io.github.azagniotov.stubby4j.utils.StringUtils.escapeCurlyBraces;
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
        this.method = ObjectUtils.isNull(method) ? new ArrayList<String>() : method;
        this.headers = ObjectUtils.isNull(headers) ? new LinkedHashMap<String, String>() : headers;
        this.query = ObjectUtils.isNull(query) ? new LinkedHashMap<String, String>() : query;
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

            return urlsMatch(stubbedRequest.url, this.url)
                    && arraysIntersect(stubbedRequest.getMethod(), this.getMethod())
                    && postBodiesMatch(stubbedRequest.isPostStubbed(), stubbedRequest.getPostBody(), this.getPostBody())
                    && headersMatch(stubbedRequest.getHeaders(), this.getHeaders())
                    && queriesMatch(stubbedRequest.getQuery(), this.getQuery());
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
            final boolean isAssertingValueSet = isSet(assertingPostBody);
            if (!isAssertingValueSet) {
                return false;
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_JSON)) {
                try {
                    boolean passed = JSONCompare.compareJSON(stubbedPostBody, assertingPostBody, JSONCompareMode.NON_EXTENSIBLE).passed();
                    if (passed) {
                        return true;
                    } else {
                        final String escapedStubbedPostBody = escapeCurlyBraces(stubbedPostBody);
                        return regexMatch(escapedStubbedPostBody, assertingPostBody, YamlProperties.POST);
                    }
                } catch (final JSONException e) {
                    final String escapedStubbedPostBody = escapeCurlyBraces(stubbedPostBody);
                    return regexMatch(escapedStubbedPostBody, assertingPostBody, YamlProperties.POST);
                }
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_XML)) {
                try {
                    final Diff diff = new Diff(stubbedPostBody, assertingPostBody);
                    diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

                    return (diff.similar() || diff.identical());
                } catch (SAXException | IOException e) {
                    return false;
                }
            } else {
                final String escapedStubbedPostBody = escapeCurlyBraces(stubbedPostBody);
                boolean regexMatch = regexMatch(escapedStubbedPostBody, assertingPostBody, YamlProperties.POST);

                return regexMatch || stringsMatch(stubbedPostBody, assertingPostBody, YamlProperties.POST);
            }
        } else {
            return true;
        }
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

        for (Map.Entry<String, String> stubbedMappingEntry : stubbedMappingsCopy.entrySet()) {
            final boolean containsRequiredParam = assertingMappingsCopy.containsKey(stubbedMappingEntry.getKey());
            if (!containsRequiredParam) {
                return false;
            } else {
                final String assertingValue = assertingMappingsCopy.get(stubbedMappingEntry.getKey());
                final String templateTokenName = String.format("%s.%s", mapName, stubbedMappingEntry.getKey());
                final String stubbedValue = stubbedMappingEntry.getValue();
                //final String stubbedValue = StringUtils.urlEncode(stubbedMappingEntry.getValue());
                if (!stringsMatch(stubbedValue, assertingValue, templateTokenName)) {
                    return false;
                }
            }
        }

        return true;
    }

    @VisibleForTesting
    boolean stringsMatch(final String stubbedValue, final String assertingValue, final String templateTokenName) {
        final boolean stubbedValueSet = isSet(stubbedValue);
        final boolean assertingValueSet = isSet(assertingValue);

        if (!stubbedValueSet) {
            return true;
        } else if (!assertingValueSet) {
            return false;
        } else if (isWithinSquareBrackets(stubbedValue)) {
            return stubbedValue.equals(assertingValue);
        } else {
            return regexMatch(stubbedValue, assertingValue, templateTokenName);
        }
    }

    private boolean regexMatch(final String stubbedValue, final String assertingValue, final String templateTokenName) {
        try {
            // Pattern.MULTILINE changes the behavior of '^' and '$' characters,
            // it does not mean that newline feeds and carriage return will be matched by default
            // You need to make sure that you regex pattern covers both \r (carriage return) and \n (linefeed).
            // It is achievable by using symbol '\s+' which covers both \r (carriage return) and \n (linefeed).
            final Matcher matcher = Pattern.compile(stubbedValue, Pattern.MULTILINE).matcher(assertingValue);
            final boolean isMatch = matcher.matches();
            if (isMatch) {
                // group(0) holds the full regex match
                regexGroups.put(buildToken(templateTokenName, 0), matcher.group(0));

                //Matcher.groupCount() returns the number of explicitly defined capturing groups in the pattern regardless
                // of whether the capturing groups actually participated in the match. It does not include matcher.group(0)
                final int groupCount = matcher.groupCount();
                if (groupCount > 0) {
                    for (int idx = 1; idx <= groupCount; idx++) {
                        regexGroups.put(buildToken(templateTokenName, idx), matcher.group(idx));
                    }
                }
            }
            return isMatch;
        } catch (PatternSyntaxException e) {
            return stubbedValue.equals(assertingValue);
        }
    }

    @VisibleForTesting
    boolean arraysIntersect(final ArrayList<String> stubbedArray, final ArrayList<String> assertingArray) {
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
        final StringBuffer sb = new StringBuffer();
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