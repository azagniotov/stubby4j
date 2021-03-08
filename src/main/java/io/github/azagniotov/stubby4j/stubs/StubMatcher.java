package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.annotations.VisibleForTesting;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.azagniotov.stubby4j.utils.StringUtils.escapeSpecialRegexCharacters;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isNotSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.POST;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.QUERY;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.URL;
import static org.xmlunit.builder.Input.fromByteArray;

class StubMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubMatcher.class);
    private static final DefaultNodeMatcher NODE_MATCHER_BY_NAME_AND_ALL_ATTRIBUTES = new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes);
    private static final Pattern SUB_TYPE_PATTERN = Pattern.compile("/(?:.*\\+)?(\\w*);?");
    private final Map<String, String> regexGroups;

    StubMatcher(final Map<String, String> regexGroups) {
        this.regexGroups = regexGroups;
    }

    boolean matches(final StubRequest stubbedRequest, final StubRequest assertingRequest) {
        if (!urlsMatch(stubbedRequest.getUri(), assertingRequest.getUri())) {
            ANSITerminal.error(String.format("Failed to match on URL [%s] WITH [%s]", stubbedRequest.getUri(), assertingRequest.getUri()));
            LOGGER.error("Failed to match on URL [{}] WITH [{}].", stubbedRequest.getUri(), assertingRequest.getUri());
            return false;
        }
        ANSITerminal.info(String.format("Matched on URL [%s] WITH [%s]", stubbedRequest.getUri(), assertingRequest.getUri()));
        LOGGER.info(String.format("Matched on URL [%s] WITH [%s].", stubbedRequest.getUri(), assertingRequest.getUri()));

        if (!listsIntersect(stubbedRequest.getMethod(), assertingRequest.getMethod())) {
            ANSITerminal.error(String.format("Failed to match on METHOD [%s] WITH [%s]", stubbedRequest.getMethod(), assertingRequest.getMethod()));
            LOGGER.error("Failed to match on METHOD [{}] WITH [{}].", stubbedRequest.getMethod(), assertingRequest.getMethod());
            return false;
        }
        ANSITerminal.info(String.format("Matched on METHOD [%s] WITH [%s]", stubbedRequest.getMethod(), assertingRequest.getMethod()));
        LOGGER.info("Matched on METHOD [{}] WITH [{}]", stubbedRequest.getMethod(), assertingRequest.getMethod());

        if (!postBodiesMatch(stubbedRequest.isRequestBodyStubbed(), stubbedRequest.getPostBody(), assertingRequest)) {
            ANSITerminal.error(String.format("Failed to match on POST BODY [%s] WITH [%s]", stubbedRequest.getPostBody(), assertingRequest.getPostBody()));
            LOGGER.error("Failed to match on POST BODY [{}] WITH [{}].", stubbedRequest.getPostBody(), assertingRequest.getPostBody());
            return false;
        }
        ANSITerminal.info(String.format("Matched on POST BODY [%s] WITH [%s]", stubbedRequest.getPostBody(), assertingRequest.getPostBody()));
        LOGGER.info("Matched on POST BODY [{}] WITH [{}].", stubbedRequest.getPostBody(), assertingRequest.getPostBody());

        if (!headersMatch(stubbedRequest.getHeaders(), assertingRequest.getHeaders())) {
            ANSITerminal.error(String.format("Failed to match on HEADERS [%s] WITH [%s]", stubbedRequest.getHeaders(), assertingRequest.getHeaders()));
            LOGGER.error("Failed to match on HEADERS [{}] WITH [{}].", stubbedRequest.getHeaders(), assertingRequest.getHeaders());
            return false;
        }
        ANSITerminal.info(String.format("Matched on HEADERS [%s] WITH [%s]", stubbedRequest.getHeaders(), assertingRequest.getHeaders()));
        LOGGER.info("Matched on HEADERS [{}] WITH [{}].", stubbedRequest.getHeaders(), assertingRequest.getHeaders());

        if (!queriesMatch(stubbedRequest.getQuery(), assertingRequest.getQuery())) {
            ANSITerminal.error(String.format("Failed to match on QUERY [%s] WITH [%s]", stubbedRequest.getQuery(), assertingRequest.getQuery()));
            LOGGER.error("Failed to match on QUERY [{}] WITH [{}].", stubbedRequest.getQuery(), assertingRequest.getQuery());
            return false;
        }
        ANSITerminal.info(String.format("Matched on QUERY [%s] WITH [%s]", stubbedRequest.getQuery(), assertingRequest.getQuery()));
        LOGGER.info("Matched on QUERY [{}] WITH [{}].", stubbedRequest.getQuery(), assertingRequest.getQuery());

        return true;
    }

    private boolean urlsMatch(final String stubbedUrl, final String assertingUrl) {
        return stringsMatch(stubbedUrl, assertingUrl, URL.toString());
    }

    @VisibleForTesting
    boolean postBodiesMatch(final boolean isPostStubbed, final String stubbedPostBody, final StubRequest assertingRequest) {
        if (isPostStubbed) {
            final String assertingPostBody = assertingRequest.getPostBody();
            if (isNotSet(assertingPostBody)) {
                return false;
            }

            final String assertingContentType = assertingRequest.getHeaders().get("content-type");

            if (isSet(assertingContentType)) {
                final Matcher matcher = SUB_TYPE_PATTERN.matcher(assertingContentType);
                if (matcher.find()) {
                    final String subType = matcher.group(1);

                    if ("json".equals(subType)) {
                        return jsonMatch(stubbedPostBody, assertingPostBody);
                    } else if ("xml".equals(subType)) {
                        return xmlMatch(stubbedPostBody, assertingPostBody);
                    }
                }
            }

            return stringsMatch(stubbedPostBody, assertingPostBody, POST.toString());
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
            final boolean passed = JSONCompare.compareJSON(stubbedJson, assertingJson, JSONCompareMode.NON_EXTENSIBLE).passed();
            if (passed) {
                return true;
            } else {
                final String escapedStubbedPostBody = escapeSpecialRegexCharacters(stubbedJson);
                return stringsMatch(escapedStubbedPostBody, assertingJson, POST.toString());
            }
        } catch (final JSONException e) {
            // In a "happy path", this exception happens when stubbed JSON is a RegEx pattern
            return stringsMatch(stubbedJson, assertingJson, POST.toString());
        }
    }

    private boolean xmlMatch(final String stubbedXml, final String assertingXml) {
        try {

            final Input.Builder control = fromByteArray(getBytesUtf8(stubbedXml));
            final Input.Builder assertion = fromByteArray(getBytesUtf8(assertingXml));

            // There is a chance that the stubbed XML contains XMLUnit placeholders,
            // e.g.: ${xmlunit.matchesRegex(..)}, so let's do another comparison pass
            // using PlaceholderDifferenceEvaluator.
            // More info: https://github.com/azagniotov/stubby4j#regex-stubbing-for-xml-content
            final DifferenceEvaluator differenceEvaluatorChain = DifferenceEvaluators.chain(
                    DifferenceEvaluators.Default,
                    new PlaceholderDifferenceEvaluator()
            );

            final DiffBuilder xmlDiffBuilder = DiffBuilder
                    .compare(control)
                    .withTest(assertion)
                    .withDifferenceEvaluator(differenceEvaluatorChain)
                    .checkForSimilar()
                    .normalizeWhitespace()
                    .ignoreComments()
                    .withNodeMatcher(NODE_MATCHER_BY_NAME_AND_ALL_ATTRIBUTES)
                    .withComparisonController(ComparisonControllers.StopWhenDifferent);

            final Diff diff = xmlDiffBuilder.build();

            return !diff.hasDifferences();
        } catch (Exception e) {
            // A common exception that I have seen to happen is:
            // org.xmlunit.XMLUnitException: The markup in the document preceding the root element must be well-formed.

            ANSITerminal.error(String.format("Failed to parse XML markup: %s, cause: %s", e, e.getCause()));
            LOGGER.error("Failed to parse XML markup: {}, cause: {}", e, e.getCause());

            return regexMatch(stubbedXml, assertingXml, POST.toString());
        }
    }
}
