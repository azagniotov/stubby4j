package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.http.StubbyHttpTransport;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.ObjectUtils;
import io.github.azagniotov.stubby4j.utils.ReflectionUtils;
import io.github.azagniotov.stubby4j.yaml.YAMLParser;
import io.github.azagniotov.stubby4j.yaml.YamlProperties;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.azagniotov.stubby4j.stubs.StubResponse.notFoundResponse;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.redirectResponse;
import static io.github.azagniotov.stubby4j.stubs.StubResponse.unauthorizedResponse;

public class StubRepository {

    private final File configFile;
    private final List<StubHttpLifecycle> stubs;
    private final Future<List<StubHttpLifecycle>> stubLoadComputation;
    private final StubbyHttpTransport stubbyHttpTransport;
    private final ConcurrentHashMap<String, AtomicLong> resourceStats;
    private final ConcurrentHashMap<String, StubHttpLifecycle> matchedStubsCache;

    public StubRepository(final File configFile, final Future<List<StubHttpLifecycle>> stubLoadComputation) {
        this.stubs = new ArrayList<>();
        this.configFile = configFile;
        this.stubLoadComputation = stubLoadComputation;
        this.stubbyHttpTransport = new StubbyHttpTransport();
        this.resourceStats = new ConcurrentHashMap<>();
        this.matchedStubsCache = new ConcurrentHashMap<>();
    }

    public StubResponse findStubResponseFor(final StubRequest incomingRequest) {
        final StubHttpLifecycle incomingLifecycle = new StubHttpLifecycle();
        incomingLifecycle.setRequest(incomingRequest);

        return findMatch(incomingLifecycle);
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
                ReflectionUtils.injectObjectFields(matchedStubResponse, YamlProperties.BODY, stubbyResponse.getContent());
            } catch (Exception e) {
                ANSITerminal.error(String.format("Could not record from %s: %s", recordingSource, e.toString()));
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
     * @see StubRequest#createFromHttpServletRequest(HttpServletRequest)
     * @see StubHttpLifecycle#equals(Object)
     * @see StubRequest#equals(Object)
     */
    private synchronized Optional<StubHttpLifecycle> matchStub(final StubHttpLifecycle incomingStub) {

        final String incomingRequestUrl = incomingStub.getUrl();
        if (matchedStubsCache.containsKey(incomingRequestUrl)) {
            ANSITerminal.loaded(String.format("Local cache contains potential match for the URL [%s]", incomingRequestUrl));
            final StubHttpLifecycle cachedPotentialMatch = matchedStubsCache.get(incomingRequestUrl);
            // The order(?) in which equality is determined is important here (what object is "equal to" the other one)
            if (incomingStub.equals(cachedPotentialMatch)) {
                ANSITerminal.loaded(String.format("Potential match for the URL [%s] was deemed as a full match", incomingRequestUrl));

                return Optional.of(cachedPotentialMatch);
            }
            ANSITerminal.warn(String.format("Cached match for the URL [%s] failed to match fully, invalidating match cache..", incomingRequestUrl));
            matchedStubsCache.remove(incomingRequestUrl);
        }

        final long initialStart = System.currentTimeMillis();
        for (final StubHttpLifecycle stubbed : stubs) {
            if (incomingStub.equals(stubbed)) {
                final long elapsed = System.currentTimeMillis() - initialStart;
                ANSITerminal.status(String.format("Found a match after %s milliseconds, caching the found match for URL [%s]", elapsed, incomingRequestUrl));
                matchedStubsCache.put(incomingRequestUrl, stubbed);

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

    synchronized boolean resetStubsCache(final List<StubHttpLifecycle> newStubs) {
        this.matchedStubsCache.clear();
        this.stubs.clear();
        final boolean added = this.stubs.addAll(newStubs);
        if (added) {
            this.matchedStubsCache.clear();
            updateResourceIDHeaders();
        }
        return added;
    }

    public synchronized void refreshStubsFromYAMLConfig(final YAMLParser yamlParser) throws Exception {
        resetStubsCache(yamlParser.parse(this.configFile.getParent(), configFile));
    }

    public synchronized void refreshStubsByPost(final YAMLParser yamlParser, final String postPayload) throws Exception {
        resetStubsCache(yamlParser.parse(this.configFile.getParent(), postPayload));
    }

    public synchronized String refreshStubByIndex(final YAMLParser yamlParser, final String putPayload, final int index) throws Exception {
        final List<StubHttpLifecycle> parsedStubs = yamlParser.parse(this.configFile.getParent(), putPayload);
        final StubHttpLifecycle newStub = parsedStubs.get(0);
        updateStubByIndex(index, newStub);

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

    @CoberturaIgnore
    public String getResourceStatsAsCsv() {
        final String csvNoHeader = resourceStats.toString().replaceAll("\\{|\\}", "").replaceAll(", ", FileUtils.BR).replaceAll("=", ",");
        return String.format("resourceId,hits%s%s", FileUtils.BR, csvNoHeader);
    }

    public synchronized String getOnlyStubRequestUrl() {
        return stubs.get(0).getUrl();
    }

    public File getYAMLConfig() {
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

    @CoberturaIgnore
    public String getYAMLConfigCanonicalPath() {
        try {
            return this.configFile.getCanonicalPath();
        } catch (IOException e) {
            return this.configFile.getAbsolutePath();
        }
    }

    public synchronized String getStubYAML() {
        final StringBuilder builder = new StringBuilder();
        for (final StubHttpLifecycle stub : stubs) {
            builder.append(stub.getCompleteYAML()).append(FileUtils.BR).append(FileUtils.BR);
        }

        return builder.toString();
    }

    public synchronized String getStubYAMLByIndex(final int index) {
        return stubs.get(index).getCompleteYAML();
    }

    synchronized void updateStubByIndex(final int index, final StubHttpLifecycle newStub) {
        deleteStubByIndex(index);
        stubs.add(index, newStub);
        updateResourceIDHeaders();
    }

    public synchronized boolean canMatchStubByIndex(final int index) {
        return stubs.size() - 1 >= index;
    }

    public synchronized StubHttpLifecycle deleteStubByIndex(final int index) {
        final StubHttpLifecycle removedStub = stubs.remove(index);
        updateResourceIDHeaders();

        return removedStub;
    }

    private void updateResourceIDHeaders() {
        for (int index = 0; index < stubs.size(); index++) {
            stubs.get(index).setResourceId(index);
        }
    }

    @CoberturaIgnore
    public void retrieveLoadedStubs() {
        try {
            stubs.addAll(stubLoadComputation.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}