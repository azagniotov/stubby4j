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
import org.eclipse.jetty.http.HttpStatus;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_PROXY_CONFIG;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_PROXY_REQUEST;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_PROXY_RESPONSE;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.notFoundResponse;
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
import static org.eclipse.jetty.http.HttpStatus.getCode;

public class StubRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubRepository.class);

    private final File configFile;

    private final List<StubHttpLifecycle> stubs;
    private final Cache<String, StubHttpLifecycle> stubMatchesCache;

    private final ConcurrentHashMap<String, AtomicLong> resourceStats;
    private final ConcurrentHashMap<String, StubHttpLifecycle> uuidToStub;
    private final ConcurrentHashMap<String, StubProxyConfig> proxyConfigs;

    private final CompletableFuture<YamlParseResultSet> stubLoadComputation;
    private final StubbyHttpTransport stubbyHttpTransport;

    public StubRepository(final File configFile,
                          final Cache<String, StubHttpLifecycle> stubMatchesCache,
                          final CompletableFuture<YamlParseResultSet> stubLoadComputation,
                          final StubbyHttpTransport stubbyHttpTransport) {
        this.stubs = new ArrayList<>();
        this.uuidToStub = new ConcurrentHashMap<>();
        this.proxyConfigs = new ConcurrentHashMap<>();
        this.configFile = configFile;
        this.stubLoadComputation = stubLoadComputation;
        this.stubbyHttpTransport = stubbyHttpTransport;
        this.resourceStats = new ConcurrentHashMap<>();
        this.stubMatchesCache = stubMatchesCache;
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

    private StubResponse findMatch(final StubHttpLifecycle incomingHttpLifecycle) {

        final Optional<StubHttpLifecycle> matchedStubOptional = matchStub(incomingHttpLifecycle);
        if (!matchedStubOptional.isPresent()) {
            if (!proxyConfigs.isEmpty()) {
                return proxyRequest(incomingHttpLifecycle);
            } else {
                return notFoundResponse();
            }
        }

        final StubHttpLifecycle matchedStub = matchedStubOptional.get();
        final String resourceId = matchedStub.getResourceId();
        resourceStats.putIfAbsent(resourceId, new AtomicLong(0));
        resourceStats.get(resourceId).incrementAndGet();

        final StubResponse matchedStubResponse = matchedStub.getResponse(true);
        if (matchedStub.isAuthorizationRequired() && matchedStub.isIncomingRequestUnauthorized(incomingHttpLifecycle)) {
            return unauthorizedResponse();
        }

        if (matchedStubResponse.hasHeaderLocation()) {
            // FYI: for the redirect to work correctly, the stubbed status code must be one fo the HTTP
            // codes that cause the redirect. See StubsResponseHandlingStrategyFactory
            return matchedStubResponse;
        }

        if (matchedStubResponse.isRecordingRequired()) {
            recordResponse(incomingHttpLifecycle, matchedStub, matchedStubResponse);
        }

        return matchedStubResponse;
    }

    /**
     * That's the point where the incoming {@link StubHttpLifecycle} that was created from the incoming
     * raw {@link HttpServletRequest request} is matched to the in-memory stubs.
     * <p>
     * First, the local cache holding previously matched stubs is checked to see if there is a match for the incoming
     * {@link StubHttpLifecycle#hashCode()}. If the incoming {@link StubHttpLifecycle} hashCode found in the cache,
     * then the cached match and the incoming {@link StubHttpLifecycle} are compared to each other to determine a
     * complete equality for sanity check based on the {@link StubRequest#equals(Object)}.
     * <p>
     * If a complete equality with the cached {@link StubHttpLifecycle match} was not achieved, the incoming
     * {@link StubHttpLifecycle request} is compared to every {@link StubHttpLifecycle element} in the list of loaded
     * stubs using their natural order (i.e.: the order in which the stubs were defined in the YAML).
     * <p>
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

        final String incomingRequestHashCode = String.valueOf(incomingStub.hashCode());
        final Optional<StubHttpLifecycle> cachedMatchCandidateOptional = stubMatchesCache.get(incomingRequestHashCode);

        return cachedMatchCandidateOptional.map(cachedMatchCandidate -> {
            ANSITerminal.loaded(String.format("Local cache contains a match for hashCode [%s]", incomingRequestHashCode));
            LOGGER.debug("Local cache contains a match for hashCode [{}].", incomingRequestHashCode);

            final long elapsed = System.currentTimeMillis() - initialStart;
            logMatch(elapsed, cachedMatchCandidate);

            return Optional.of(cachedMatchCandidate);

        }).orElseGet(() -> matchAll(incomingStub, initialStart));
    }

    private StubResponse proxyRequest(final StubHttpLifecycle incomingHttpLifecycle) {

        // The catch-all will always be there if we have proxy configs, otherwise the YAML loading throws
        final StubProxyConfig catchAllProxyConfig = proxyConfigs.get(StubProxyConfig.Builder.DEFAULT_UUID);

        final StubRequest incomingRequest = incomingHttpLifecycle.getRequest();
        final String proxyConfigUuidHeader = incomingRequest
                .getHeaders()
                .getOrDefault(HEADER_X_STUBBY_PROXY_CONFIG, StubProxyConfig.Builder.DEFAULT_UUID);

        if (!proxyConfigs.containsKey(proxyConfigUuidHeader)) {
            final String warning = String.format("Could not find proxy config by UUID using header value '%s', falling back to 'default'", proxyConfigUuidHeader);
            ANSITerminal.warn(warning);
            LOGGER.warn(warning);
        }

        final StubProxyConfig proxyConfig = proxyConfigs.getOrDefault(proxyConfigUuidHeader, catchAllProxyConfig);
        final String proxyEndpoint = String.format("%s%s", proxyConfig.getPropertyEndpoint(), incomingHttpLifecycle.getUrl());

        final String proxyRoundTripUuid = UUID.randomUUID().toString();
        final Map<String, String> proxyResponseFlatHeaders = new HashMap<>();
        proxyResponseFlatHeaders.put(HEADER_X_STUBBY_PROXY_RESPONSE, proxyRoundTripUuid);

        try {
            incomingRequest.getHeaders().put(HEADER_X_STUBBY_PROXY_REQUEST, proxyRoundTripUuid);

            handleIfAdditiveProxyStrategy(incomingRequest, proxyConfig);

            final StubbyResponse stubbyResponse = stubbyHttpTransport.httpRequestFromStub(incomingRequest, proxyEndpoint);
            for (Map.Entry<String, List<String>> entry : stubbyResponse.headers().entrySet()) {
                final String headerName = ObjectUtils.isNull(entry.getKey()) ? "null" : entry.getKey();
                if (entry.getValue().size() == 1) {
                    proxyResponseFlatHeaders.put(headerName, entry.getValue().get(0));
                } else {
                    proxyResponseFlatHeaders.put(headerName, new HashSet<>(entry.getValue()).toString());
                }
            }

            return new StubResponse.Builder()
                    .withHttpStatusCode(getCode(stubbyResponse.statusCode()))
                    .withBody(stubbyResponse.body())
                    .withHeaders(proxyResponseFlatHeaders)
                    .build();

        } catch (Exception e) {
            ANSITerminal.error(String.format("Could not proxy to %s: %s", proxyEndpoint, e.toString()));
            LOGGER.error("Could not proxy to {}.", proxyEndpoint, e);

            return new StubResponse.Builder()
                    .withHttpStatusCode(HttpStatus.Code.INTERNAL_SERVER_ERROR)
                    .withBody(e.getMessage())
                    .withHeaders(proxyResponseFlatHeaders)
                    .build();
        }
    }

    private void handleIfAdditiveProxyStrategy(final StubRequest incomingRequest, final StubProxyConfig proxyConfig) {
        if (proxyConfig.isAdditiveStrategy()) {
            if (proxyConfig.hasHeaders()) {
                for (final Map.Entry<String, String> headerEntry : proxyConfig.getHeaders().entrySet()) {
                    incomingRequest.getHeaders().put(headerEntry.getKey(), headerEntry.getValue());
                }
            }
        }
    }

    private void recordResponse(StubHttpLifecycle incomingRequest, StubHttpLifecycle matchedStub, StubResponse matchedStubResponse) {
        final String recordingSource = String.format("%s%s", matchedStubResponse.getBody(), incomingRequest.getUrl());
        try {
            final StubbyResponse stubbyResponse = stubbyHttpTransport.httpRequestFromStub(matchedStub.getRequest(), recordingSource);
            injectObjectFields(matchedStubResponse, BODY.toString(), stubbyResponse.body());
        } catch (Exception e) {
            ANSITerminal.error(String.format("Could not record from %s: %s", recordingSource, e.toString()));
            LOGGER.error("Could not record from {}.", recordingSource, e);
        }
    }

    private Optional<StubHttpLifecycle> matchAll(final StubHttpLifecycle incomingStub, final long initialStart) {
        for (final StubHttpLifecycle stubbed : stubs) {
            if (incomingStub.equals(stubbed)) {
                final long elapsed = System.currentTimeMillis() - initialStart;
                logMatch(elapsed, stubbed);

                final String incomingRequestHashCode = String.valueOf(incomingStub.hashCode());
                ANSITerminal.status(String.format("Caching the found match for hashCode [%s]", incomingRequestHashCode));
                LOGGER.debug("Caching the found match for hashCode [{}].", incomingRequestHashCode);
                stubMatchesCache.putIfAbsent(incomingRequestHashCode, stubbed);

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

    public synchronized StubProxyConfig matchProxyConfigByName(final String proxyConfigUniqueName) {
        return proxyConfigs.get(proxyConfigUniqueName);
    }

    synchronized boolean resetStubsCache(final YamlParseResultSet yamlParseResultSet) {
        this.stubMatchesCache.clear();
        this.stubs.clear();
        this.uuidToStub.clear();
        this.proxyConfigs.clear();

        final boolean addedStubs = this.stubs.addAll(yamlParseResultSet.getStubs());
        if (addedStubs) {
            this.stubMatchesCache.clear();
            updateResourceIDHeaders();
            this.uuidToStub.putAll(yamlParseResultSet.getUuidToStubs());
        }

        loadProxyConfigsWithOptionalThrow(yamlParseResultSet);

        return addedStubs;
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

    public synchronized String refreshProxyConfigByUuid(final YamlParser yamlParser, final String putPayload, final String uuid) throws Exception {
        final YamlParseResultSet yamlParseResultSet = yamlParser.parse(this.configFile.getParent(), putPayload);
        final StubProxyConfig newStubProxyConfig = yamlParseResultSet.getProxyConfigs().get(uuid);
        updateProxyConfigByUuid(uuid, newStubProxyConfig);

        return newStubProxyConfig.getPropertyEndpoint();
    }

    // Just a shallow copy that protects collection from modification, the points themselves are not copied
    public List<StubHttpLifecycle> getStubs() {
        return new LinkedList<>(stubs);
    }

    public Map<String, StubProxyConfig> getProxyConfigs() {
        return new HashMap<>(proxyConfigs);
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

    public synchronized String dumpCompleteYamlConfig() {
        final StringBuilder builder = new StringBuilder();

        if (!proxyConfigs.isEmpty()) {
            for (final Map.Entry<String, StubProxyConfig> entry : proxyConfigs.entrySet()) {
                builder.append(entry.getValue().getProxyConfigAsYAML()).append(FileUtils.BR).append(FileUtils.BR);
            }
            builder.append(FileUtils.BR);
        }

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

    public synchronized String getProxyConfigYamlByUuid(final String uuid) {
        return proxyConfigs.get(uuid).getProxyConfigAsYAML();
    }

    public synchronized boolean canMatchStubByIndex(final int index) {
        return stubs.size() - 1 >= index;
    }

    public synchronized boolean canMatchStubByUuid(final String uuid) {
        return uuidToStub.containsKey(uuid);
    }

    public synchronized boolean canMatchProxyConfigByUuid(final String uuid) {
        return proxyConfigs.containsKey(uuid);
    }

    synchronized void updateProxyConfigByUuid(final String uuid, final StubProxyConfig newStubProxyConfig) {
        if (!uuid.equals(newStubProxyConfig.getUUID())) {
            final String message = String.format("Provided proxy config UUID '%s' does not match the target UUID '%s'",
                    newStubProxyConfig.getUUID(), uuid);
            throw new IllegalArgumentException(message);
        }

        if (uuid.equals(StubProxyConfig.Builder.DEFAULT_UUID)) {
            proxyConfigs.remove(StubProxyConfig.Builder.DEFAULT_UUID);
            proxyConfigs.put(StubProxyConfig.Builder.DEFAULT_UUID, newStubProxyConfig);
        } else {
            proxyConfigs.remove(uuid);
            proxyConfigs.put(uuid, newStubProxyConfig);
        }
    }

    synchronized void updateStubByIndex(final int index, final StubHttpLifecycle newStub) {
        final StubHttpLifecycle deletedStub = deleteStubByIndex(index);
        stubs.add(index, newStub);
        updateResourceIDHeaders();

        this.stubMatchesCache.clear();

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

    public synchronized StubProxyConfig deleteProxyConfigByUuid(final String uuid) {
        if (uuid.equals(StubProxyConfig.Builder.DEFAULT_UUID)) {
            throw new IllegalArgumentException("You cannot delete 'default' (i.e.: catch-all) proxy config via API");
        }
        return proxyConfigs.remove(uuid);
    }

    public synchronized void clear() {
        this.stubMatchesCache.clear();
        this.stubs.clear();
        this.uuidToStub.clear();
        this.proxyConfigs.clear();
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

            loadProxyConfigsWithOptionalThrow(yamlParseResultSet);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadProxyConfigsWithOptionalThrow(final YamlParseResultSet yamlParseResultSet) {
        final Map<String, StubProxyConfig> loadedProxyConfigs = yamlParseResultSet.getProxyConfigs();

        if (!loadedProxyConfigs.isEmpty() && !loadedProxyConfigs.containsKey(StubProxyConfig.Builder.DEFAULT_UUID)) {
            throw new IllegalStateException("YAML config contains proxy configs, but the 'default' proxy config is not configured, how so?");
        }

        this.proxyConfigs.putAll(loadedProxyConfigs);
    }
}
