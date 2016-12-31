package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.common.Common;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.azagniotov.stubby4j.utils.StringUtils.escapeSpecialRegexCharacters;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isNotSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.POST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.QUERY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.URL;

class StubMatcher {

    private final Map<String, String> regexGroups;

    StubMatcher(final Map<String, String> regexGroups) {
        this.regexGroups = regexGroups;
    }

    boolean matches(final StubRequest stubbedRequest, final StubRequest assertingRequest) {
        if (!urlsMatch(stubbedRequest.getUri(), assertingRequest.getUri())) {
            ANSITerminal.error(String.format("Failed to match on URL [%s] WITH [%s]", stubbedRequest.getUri(), assertingRequest.getUri()));
            return false;
        }
        ANSITerminal.info(String.format("Matched on URL [%s] WITH [%s]", stubbedRequest.getUri(), assertingRequest.getUri()));

        if (!listsIntersect(stubbedRequest.getMethod(), assertingRequest.getMethod())) {
            ANSITerminal.error(String.format("Failed to match on METHOD [%s] WITH [%s]", stubbedRequest.getMethod(), assertingRequest.getMethod()));
            return false;
        }
        ANSITerminal.info(String.format("Matched on METHOD [%s] WITH [%s]", stubbedRequest.getMethod(), assertingRequest.getMethod()));

        if (!postBodiesMatch(stubbedRequest.isPostStubbed(), stubbedRequest.getPostBody(), assertingRequest)) {
            ANSITerminal.error(String.format("Failed to match on POST BODY [%s] WITH [%s]", stubbedRequest.getPostBody(), assertingRequest.getPostBody()));
            return false;
        }
        ANSITerminal.info(String.format("Matched on POST BODY [%s] WITH [%s]", stubbedRequest.getPostBody(), assertingRequest.getPostBody()));

        if (!headersMatch(stubbedRequest.getHeaders(), assertingRequest.getHeaders())) {
            ANSITerminal.error(String.format("Failed to match on HEADERS [%s] WITH [%s]", stubbedRequest.getHeaders(), assertingRequest.getHeaders()));
            return false;
        }
        ANSITerminal.info(String.format("Matched on HEADERS [%s] WITH [%s]", stubbedRequest.getHeaders(), assertingRequest.getHeaders()));

        if (!queriesMatch(stubbedRequest.getQuery(), assertingRequest.getQuery())) {
            ANSITerminal.error(String.format("Failed to match on QUERY [%s] WITH [%s]", stubbedRequest.getQuery(), assertingRequest.getQuery()));
            return false;
        }
        ANSITerminal.info(String.format("Matched on QUERY [%s] WITH [%s]", stubbedRequest.getQuery(), assertingRequest.getQuery()));

        return true;
    }

    private boolean urlsMatch(final String stubbedUrl, final String assertingUrl) {
        return stringsMatch(stubbedUrl, assertingUrl, URL.toString());
    }

    private boolean postBodiesMatch(final boolean isPostStubbed, final String stubbedPostBody, final StubRequest assertingRequest) {
        final String assertingPostBody = assertingRequest.getPostBody();
        if (isPostStubbed) {
            final String assertingContentType = assertingRequest.getHeaders().get("content-type");
            if (isNotSet(assertingPostBody)) {
                return false;
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_JSON)) {
                return jsonMatch(stubbedPostBody, assertingPostBody);
            } else if (isSet(assertingContentType) && assertingContentType.contains(Common.HEADER_APPLICATION_XML)) {
                return xmlMatch(stubbedPostBody, assertingPostBody);
            } else {
                return stringsMatch(stubbedPostBody, assertingPostBody, POST.toString());
            }
        }

        return true;
    }

    private boolean queriesMatch(final Map<String, String> stubbedQuery, final Map<String, String> assertingQuery) {
        return mapsMatch(stubbedQuery, assertingQuery, QUERY.toString());
    }

    private boolean headersMatch(final Map<String, String> stubbedHeaders, final Map<String, String> assertingHeaders) {
        final Map<String, String> stubbedHeadersCopy = new HashMap<>(stubbedHeaders);
        for (final StubbableAuthorizationType authorizationType : StubbableAuthorizationType.values()) {
            // auth header is dealt with in StubRepository after request is matched
            stubbedHeadersCopy.remove(authorizationType.asYAMLProp());
        }
        return mapsMatch(stubbedHeadersCopy, assertingHeaders, HEADERS.toString());
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
        }
        return regexMatch(stubbedValue, assertingValue, templateTokenName) || stubbedValue.equals(assertingValue);
    }

    private boolean regexMatch(final String stubbedValue, final String assertingValue, final String templateTokenName) {
        return RegexParser.INSTANCE.match(stubbedValue, assertingValue, templateTokenName, regexGroups);
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

    private boolean jsonMatch(final String stubbedJson, final String assertingJson) {
        try {
            boolean passed = JSONCompare.compareJSON(stubbedJson, assertingJson, JSONCompareMode.NON_EXTENSIBLE).passed();
            if (passed) {
                return true;
            } else {
                final String escapedStubbedPostBody = escapeSpecialRegexCharacters(stubbedJson);
                return stringsMatch(escapedStubbedPostBody, assertingJson, POST.toString());
            }
        } catch (final JSONException e) {
            final String escapedStubbedPostBody = escapeSpecialRegexCharacters(stubbedJson);
            return stringsMatch(escapedStubbedPostBody, assertingJson, POST.toString());
        }
    }

    private boolean xmlMatch(final String stubbedXml, final String assertingXml) {
        try {
            final Diff diff = new Diff(stubbedXml, assertingXml);
            diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

            return (diff.similar() || diff.identical());
        } catch (SAXException | IOException e) {
            return stringsMatch(stubbedXml, assertingXml, POST.toString());
        }
    }
}
