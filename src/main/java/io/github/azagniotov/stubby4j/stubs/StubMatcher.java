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

    private static final String FAILED_TO_MATCH_ON_STUBBED = "Failed to match on stubbed";
    private static final String MATCHED_ON_STUBBED = "Matched on stubbed";

    private static final String MSG_FIELDS_TEMPLATE = "[%s] WITH incoming [%s]";
    private static final String MSG_FIELD_URL = " URL ";
    private static final String MSG_FIELD_METHOD = " METHOD ";
    private static final String MSG_FIELD_POST_BODY = " POST BODY ";
    private static final String MSG_FIELD_HEADERS = " HEADERS ";
    private static final String MSG_FIELD_QUERY = " QUERY ";

    private final Map<String, String> regexGroups;

    StubMatcher(final Map<String, String> regexGroups) {
        this.regexGroups = regexGroups;
    }

    boolean matches(final StubRequest stubbedRequest, final StubRequest assertingRequest) {
        // Match stubbed request URI path
        if (!urlsMatch(stubbedRequest.getUri(), assertingRequest.getUri())) {
            final String urlMatchFailed = String.format(FAILED_TO_MATCH_ON_STUBBED + MSG_FIELD_URL + MSG_FIELDS_TEMPLATE, stubbedRequest.getUri(), assertingRequest.getUri());
            ANSITerminal.error(urlMatchFailed);
            LOGGER.error(urlMatchFailed);
            return false;
        }
        final String urlMatchSuccess = String.format(MATCHED_ON_STUBBED + MSG_FIELD_URL + MSG_FIELDS_TEMPLATE, stubbedRequest.getUri(), assertingRequest.getUri());
        ANSITerminal.info(urlMatchSuccess);
        LOGGER.info(urlMatchSuccess);

        // Match stubbed request HTTP method(s)
        if (!stubbedRequest.getMethod().isEmpty()) {
            if (!listsIntersect(stubbedRequest.getMethod(), assertingRequest.getMethod())) {
                final String methodMatchFailed = String.format(FAILED_TO_MATCH_ON_STUBBED + MSG_FIELD_METHOD + MSG_FIELDS_TEMPLATE, stubbedRequest.getMethod(), assertingRequest.getMethod());
                ANSITerminal.error(methodMatchFailed);
                LOGGER.error(methodMatchFailed);
                return false;
            }
            final String methodMatchSuccess = String.format(MATCHED_ON_STUBBED + MSG_FIELD_METHOD + MSG_FIELDS_TEMPLATE, stubbedRequest.getMethod(), assertingRequest.getMethod());
            ANSITerminal.info(methodMatchSuccess);
            LOGGER.info(methodMatchSuccess);
        }

        // Match stubbed request body payload (POST, PUT & PATCH)
        if (stubbedRequest.isRequestBodyStubbed()) {
            if (!postBodiesMatch(stubbedRequest, assertingRequest)) {
                final String bodyMatchFailed = String.format(FAILED_TO_MATCH_ON_STUBBED + MSG_FIELD_POST_BODY + MSG_FIELDS_TEMPLATE, stubbedRequest.getPostBody(), assertingRequest.getPostBody());
                ANSITerminal.error(bodyMatchFailed);
                LOGGER.error(bodyMatchFailed);
                return false;
            }
            final String bodyMatchSuccess = String.format(MATCHED_ON_STUBBED + MSG_FIELD_POST_BODY + MSG_FIELDS_TEMPLATE, stubbedRequest.getPostBody(), assertingRequest.getPostBody());
            ANSITerminal.info(bodyMatchSuccess);
            LOGGER.info(bodyMatchSuccess);
        }

        // Match stubbed request headers
        if (!stubbedRequest.getHeaders().isEmpty()) {
            if (!headersMatch(stubbedRequest.getHeaders(), assertingRequest.getHeaders())) {
                final String headersMatchFailed = String.format(FAILED_TO_MATCH_ON_STUBBED + MSG_FIELD_HEADERS + MSG_FIELDS_TEMPLATE, stubbedRequest.getHeaders(), assertingRequest.getHeaders());
                ANSITerminal.error(headersMatchFailed);
                LOGGER.error(headersMatchFailed);
                return false;
            }
            final String headersMatchSuccess = String.format(MATCHED_ON_STUBBED + MSG_FIELD_HEADERS + MSG_FIELDS_TEMPLATE, stubbedRequest.getHeaders(), assertingRequest.getHeaders());
            ANSITerminal.info(headersMatchSuccess);
            LOGGER.info(headersMatchSuccess);
        }

        // Match stubbed request query params
        if (!stubbedRequest.getQuery().isEmpty()) {
            if (!queriesMatch(stubbedRequest.getQuery(), assertingRequest.getQuery())) {
                final String uriQueryMatchFailed = String.format(FAILED_TO_MATCH_ON_STUBBED + MSG_FIELD_QUERY + MSG_FIELDS_TEMPLATE, stubbedRequest.getQuery(), assertingRequest.getQuery());
                ANSITerminal.error(uriQueryMatchFailed);
                LOGGER.error(uriQueryMatchFailed);
                return false;
            }
            final String uriQueryMatchSuccess = String.format(MATCHED_ON_STUBBED + MSG_FIELD_QUERY + MSG_FIELDS_TEMPLATE, stubbedRequest.getQuery(), assertingRequest.getQuery());
            ANSITerminal.info(uriQueryMatchSuccess);
            LOGGER.info(uriQueryMatchSuccess);
        }

        return true;
    }

    private boolean urlsMatch(final String stubbedUrl, final String assertingUrl) {
        return stringsMatch(stubbedUrl, assertingUrl, URL.toString());
    }

    @VisibleForTesting
    boolean postBodiesMatch(final StubRequest stubbedRequest, final StubRequest assertingRequest) {
        final boolean isPostStubbed = stubbedRequest.isRequestBodyStubbed();
        final String stubbedPostBody = stubbedRequest.getPostBody();

        //TODO Get templateTokenName based on whether YAML 'file' or 'post' were stubbed
        // e.g.: final String templateTokenName = stubbedRequest.getStubbedRequestBodyTokenName()

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
