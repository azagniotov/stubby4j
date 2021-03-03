package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.caching.Cache;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.azagniotov.stubby4j.stubs.StubResponse.notFoundResponse;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.redirectResponse;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.unauthorizedResponse;
import static io.github.azagniotov.stubby4j.utils.CollectionUtils.constructParamMap;
import static io.github.azagniotov.stubby4j.utils.ConsoleUtils.logAssertingRequest;
import static io.github.azagniotov.stubby4j.utils.HandlerUtils.extractPostRequestBody;
import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNotNull;
import static io.github.azagniotov.stubby4j.utils.ReflectionUtils.injectObjectFields;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;
import static io.github.azagniotov.stubby4j.utils.StringUtils.toLower;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;
import static java.util.Collections.list;

public class StubRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubRepository.class);

    // 7200 secs => 2 hours
    private static final long CACHE_ENTRY_LIFETIME_SECONDS = 7200L;

    private final File configFile;

    private final List<StubHttpLifecycle> stubs;
    private final Cache<String, StubHttpLifecycle> stubMatchesCache;

    private final ConcurrentHashMap<String, AtomicLong> resourceStats;
    private final ConcurrentHashMap<String, StubHttpLifecycle> uuidToStub;

    private final CompletableFuture<YamlParseResultSet> stubLoadComputation;
    private final StubbyHttpTransport stubbyHttpTransport;

    public StubRepository(final File configFile, final CompletableFuture<YamlParseResultSet> stubLoadComputation) {
        this.stubs = new ArrayList<>();
        this.uuidToStub = new ConcurrentHashMap<>();
        this.configFile = configFile;
        this.stubLoadComputation = stubLoadComputation;
        this.stubbyHttpTransport = new StubbyHttpTransport();
        this.resourceStats = new ConcurrentHashMap<>();
        this.stubMatchesCache = Cache.stubHttpLifecycleCache(CACHE_ENTRY_LIFETIME_SECONDS);
    }


    private static void logMatch(long elapsed, StubHttpLifecycle matched) {
        StringBuilder message = new StringBuilder()
                .append("Found a match after ")
                .append(elapsed)
                .append(" milliseconds URL [")
                .append(matched.getUrl())
                .append("]");
        if (isSet(matched.getDescription())) {
            message.append(" Description [")
                    .append(matched.getDescription())
                    .append("]");
        }

        ANSITerminal.status(message.toString());
        LOGGER.debug("{}", message);
    }

    public StubSearchResult search(final HttpServletRequest incomingRequest) throws IOException {
        final StubRequest assertionStubRequest = this.toStubRequest(incomingRequest);
        logAssertingRequest(assertionStubRequest);

        final StubResponse match = findMatch(new StubHttpLifecycle.Builder().withRequest(assertionStubRequest).build());

        return new StubSearchResult(assertionStubRequest, match);
    }

    /**
     * That's where the raw {@link HttpServletRequest request} is converted to a {@link StubHttpLifecycle},
     * which will be matched to the in-memory stubs
     *
     * @param request raw {@link HttpServletRequest request}
     */
    public StubRequest toStubRequest(final HttpServletRequest request) throws IOException {
        final StubRequest.Builder builder = new StubRequest.Builder();
        builder.withUrl(request.getPathInfo())
                .withPost(extractPostRequestBody(request, "stubs"))
                .withMethod(request.getMethod());

        final Enumeration<String> headerNamesEnumeration = request.getHeaderNames();
        final List<String> headerNames = isNotNull(headerNamesEnumeration) ? list(request.getHeaderNames()) : new LinkedList<>();
        for (final String headerName : headerNames) {
            final String headerValue = request.getHeader(headerName);

            builder.withHeader(toLower(headerName), headerValue);
        }

        return builder.withQuery(constructParamMap(request.getQueryString())).build();
    }

    private StubResponse findMatch(final StubHttpLifecycle incomingRequest) {

        final Optional<StubHttpLifecycle> matchedStubOptional = matchStub(incomingRequest);

        if (!matchedStubOptional.isPresent()) {
            return notFoundResponse();
        }

        final StubHttpLifecycle matchedStub = matchedStubOptional.get();
        final String resourceId = matchedStub.getResourceId();
        resourceStats.putIfAbsent(resourceId, new AtomicLong(0));
        resourceStats.get(resourceId).incrementAndGet();

        final StubResponse matchedStubResponse = matchedStub.getResponse(true);
        if (matchedStub.isAuthorizationRequired() && matchedStub.isIncomingRequestUnauthorized(incomingRequest)) {
            return unauthorizedResponse();
        }

        if (matchedStubResponse.hasHeaderLocation()) {
            return redirectResponse(Optional.of(matchedStubResponse));
        }

        if (matchedStubResponse.isRecordingRequired()) {
            final String recordingSource = String.format("%s%s", matchedStubResponse.getBody(), incomingRequest.getUrl());
            try {
                final StubbyResponse stubbyResponse = stubbyHttpTransport.fetchRecordableHTTPResponse(matchedStub.getRequest(), recordingSource);
                injectObjectFields(matchedStubResponse, BODY.toString(), stubbyResponse.getContent());
            } catch (Exception e) {
                ANSITerminal.error(String.format("Could not record from %s: %s", recordingSource, e.toString()));
                LOGGER.error("Could not record from {}.", e);
            }
        }
        return matchedStubResponse;
    }

    /**
     * That's the point where the incoming {@link StubHttpLifecycle} that was created from the incoming
     * raw {@link HttpServletRequest request} is matched to the in-memory stubs.
     * <p>
     * First, the local cache holding previously matched stubs is checked to see if there is a match for the incoming
     * {@link StubHttpLifecycle request} URI. If the incoming {@link StubHttpLifecycle} URI found in the cache, then the
     * cached match and the incoming {@link StubHttpLifecycle} are compared to each other to determine a complete
     * equality based on the {@link StubRequest#equals(Object)}.
     * <p>
     * If a complete equality with the cached {@link StubHttpLifecycle match} was not achieved, the incoming
     * {@link StubHttpLifecycle request} is compared to every {@link StubHttpLifecycle element} in the list of loaded
     * stubs.
     * <p>
     * The {@link List<StubHttpLifecycle>#indexOf(Object)} implicitly invokes {@link StubHttpLifecycle#equals(Object)},
     * which invokes the {@link StubRequest#equals(Object)}.
     *
     * @param incomingStub {@link StubHttpLifecycle}
     * @return an {@link Optional} describing {@link StubHttpLifecycle} match, or an empty {@link Optional} if there was no match.
     * @see #toStubRequest(HttpServletRequest)
     * @see StubHttpLifecycle#equals(Object)
     * @see StubRequest#equals(Object)
     * @see StubMatcher#matches(StubRequest, StubRequest)
     */
    private synchronized Optional<StubHttpLifecycle> matchStub(final StubHttpLifecycle incomingStub) {

        final long initialStart = System.currentTimeMillis();
        final String incomingRequestUrl = incomingStub.getUrl();

        // TODO Caching related behavior is disabled by https://github.com/azagniotov/stubby4j/pull/176
        //  due to https://github.com/azagniotov/stubby4j/issues/170 until a more viable way to use
        //  the cache for matching optimization is identified
        return matchAll(incomingStub, initialStart, incomingRequestUrl);
    }

    private Optional<StubHttpLifecycle> matchAll(final StubHttpLifecycle incomingStub, final long initialStart, final String incomingRequestUrl) {
        for (final StubHttpLifecycle stubbed : stubs) {
            if (incomingStub.equals(stubbed)) {
                final long elapsed = System.currentTimeMillis() - initialStart;
                logMatch(elapsed, stubbed);

                ANSITerminal.status(String.format("Caching the found match for URL [%s]", incomingRequestUrl));
                LOGGER.debug("Caching the found match for URL [{}].", incomingRequestUrl);

                // TODO Caching related behavior is disabled by https://github.com/azagniotov/stubby4j/pull/176
                //  due to https://github.com/azagniotov/stubby4j/issues/170 until a more viable way to use
                //  the cache for matching optimization is identified
                // stubMatchesCache.putIfAbsent(incomingRequestUrl, stubbed);

                return Optional.of(stubbed);
            }
        }

        return Optional.empty();
    }

    public synchronized Optional<StubHttpLifecycle> matchStubByIndex(final int index) {
        if (!canMatchStubByIndex(index)) {
            return Optional.empty();
        }
        return Optional.of(stubs.get(index));
    }

    synchronized boolean resetStubsCache(final YamlParseResultSet yamlParseResultSet) {
        this.stubMatchesCache.clear();
        this.stubs.clear();
        this.uuidToStub.clear();

        final boolean added = this.stubs.addAll(yamlParseResultSet.getStubs());
        if (added) {
            this.stubMatchesCache.clear();
            updateResourceIDHeaders();
            this.uuidToStub.putAll(yamlParseResultSet.getUuidToStubs());
        }
        return added;
    }

    public synchronized void refreshStubsFromYamlConfig(final YamlParser yamlParser) throws Exception {
        resetStubsCache(yamlParser.parse(this.configFile.getParent(), configFile));
    }

    public synchronized void refreshStubsByPost(final YamlParser yamlParser, final String postPayload) throws Exception {
        resetStubsCache(yamlParser.parse(this.configFile.getParent(), postPayload));
    }

    public synchronized String refreshStubByIndex(final YamlParser yamlParser, final String putPayload, final int index) throws Exception {
        final YamlParseResultSet yamlParseResultSet = yamlParser.parse(this.configFile.getParent(), putPayload);
        final StubHttpLifecycle newStub = yamlParseResultSet.getStubs().get(0);
        updateStubByIndex(index, newStub);

        return newStub.getUrl();
    }

    public synchronized String refreshStubByUuid(final YamlParser yamlParser, final String putPayload, final String uuid) throws Exception {
        final YamlParseResultSet yamlParseResultSet = yamlParser.parse(this.configFile.getParent(), putPayload);
        final StubHttpLifecycle newStub = yamlParseResultSet.getStubs().get(0);
        updateStubByUuid(uuid, newStub);

        return newStub.getUrl();
    }

    // Just a shallow copy that protects collection from modification, the points themselves are not copied
    public List<StubHttpLifecycle> getStubs() {
        return new LinkedList<>(stubs);
    }

    // Just a shallow copy that protects collection from modification, the points themselves are not copied
    public ConcurrentHashMap<String, AtomicLong> getResourceStats() {
        return new ConcurrentHashMap<>(resourceStats);
    }

    public String getResourceStatsAsCsv() {
        final String csvNoHeader = resourceStats.toString().replaceAll("\\{|\\}", "").replaceAll(", ", FileUtils.BR).replaceAll("=", ",");
        return String.format("resourceId,hits%s%s", FileUtils.BR, csvNoHeader);
    }

    public synchronized String getOnlyStubRequestUrl() {
        return stubs.get(0).getUrl();
    }

    public File getYamlConfig() {
        return configFile;
    }

    public synchronized Map<File, Long> getExternalFiles() {
        final Set<String> escrow = new HashSet<>();
        final Map<File, Long> externalFiles = new HashMap<>();
        for (final StubHttpLifecycle stub : stubs) {
            cacheExternalFile(escrow, externalFiles, stub.getRequest().getRawFile());

            final List<StubResponse> responses = stub.getResponses();
            for (final StubResponse stubbedResponse : responses) {
                cacheExternalFile(escrow, externalFiles, stubbedResponse.getRawFile());
            }
        }

        return externalFiles;
    }

    private void cacheExternalFile(final Set<String> escrow, final Map<File, Long> externalFiles, final File file) {
        if (ObjectUtils.isNotNull(file) && !escrow.contains(file.getName())) {
            escrow.add(file.getName());
            externalFiles.put(file, file.lastModified());
        }
    }


    public String getYamlConfigCanonicalPath() {
        try {
            return this.configFile.getCanonicalPath();
        } catch (IOException e) {
            return this.configFile.getAbsolutePath();
        }
    }

    public synchronized String getStubYaml() {
        final StringBuilder builder = new StringBuilder();
        for (final StubHttpLifecycle stub : stubs) {
            builder.append(stub.getCompleteYaml()).append(FileUtils.BR).append(FileUtils.BR);
        }

        return builder.toString();
    }

    public synchronized String getStubYamlByIndex(final int index) {
        return stubs.get(index).getCompleteYaml();
    }

    public synchronized String getStubYamlByUuid(final String uuid) {
        return uuidToStub.get(uuid).getCompleteYaml();
    }

    public synchronized boolean canMatchStubByIndex(final int index) {
        return stubs.size() - 1 >= index;
    }

    public synchronized boolean canMatchStubByUuid(final String uuid) {
        return uuidToStub.containsKey(uuid);
    }

    synchronized void updateStubByIndex(final int index, final StubHttpLifecycle newStub) {
        final StubHttpLifecycle deletedStub = deleteStubByIndex(index);
        stubs.add(index, newStub);
        updateResourceIDHeaders();

        // If deleted stub url is a regex, i.e.: ^/resources/asn/.*$, then we need
        // to clear from the cache all the keys (i.e.: URLs) that match that regex,
        // since we cache stubs by the incoming HTTP request url
        if (!this.stubMatchesCache.clearByKey(deletedStub.getUrl())) {
            this.stubMatchesCache.clearByRegexKey(deletedStub.getUrl());
        }

        if (StringUtils.isSet(deletedStub.getUUID())) {
            uuidToStub.remove(deletedStub.getUUID());
        }

        if (StringUtils.isSet(newStub.getUUID())) {
            uuidToStub.put(newStub.getUUID(), newStub);
        }
    }

    synchronized void updateStubByUuid(final String uuid, final StubHttpLifecycle newStub) {
        final StubHttpLifecycle obsolete = uuidToStub.get(uuid);
        final int resourceId = Integer.parseInt(obsolete.getResourceId());

        updateStubByIndex(resourceId, newStub);
    }

    public synchronized StubHttpLifecycle deleteStubByIndex(final int index) {
        final StubHttpLifecycle removedStub = stubs.remove(index);
        updateResourceIDHeaders();

        if (StringUtils.isSet(removedStub.getUUID())) {
            uuidToStub.remove(removedStub.getUUID());
        }

        return removedStub;
    }

    public synchronized StubHttpLifecycle deleteStubByUuid(final String uuid) {
        final StubHttpLifecycle toBeRemoved = uuidToStub.get(uuid);
        final int resourceId = Integer.parseInt(toBeRemoved.getResourceId());

        return deleteStubByIndex(resourceId);
    }

    public synchronized void deleteAllStubs() {
        this.stubMatchesCache.clear();
        this.stubs.clear();
        this.uuidToStub.clear();
    }

    private void updateResourceIDHeaders() {
        for (int index = 0; index < stubs.size(); index++) {
            stubs.get(index).setResourceId(index);
        }
    }


    public void retrieveLoadedStubs() {
        try {
            final YamlParseResultSet yamlParseResultSet = stubLoadComputation.get();
            stubs.addAll(yamlParseResultSet.getStubs());
            uuidToStub.putAll(yamlParseResultSet.getUuidToStubs());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
