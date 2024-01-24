/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import static io.github.azagniotov.stubby4j.utils.FileUtils.fileToBytes;
import static io.github.azagniotov.stubby4j.utils.HandlerUtils.setResponseMainHeaders;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isTokenized;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokens;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokensInString;

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

public final class DefaultResponseHandlingStrategy implements StubResponseHandlingStrategy {

    private final StubResponse stubbedResponse;

    DefaultResponseHandlingStrategy(final StubResponse stubbedResponse) {
        this.stubbedResponse = stubbedResponse;
    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        final Map<String, String> regexGroups = assertionStubRequest.getRegexGroups();

        setResponseMainHeaders(response);
        setResponseStubbedHeaders(response, stubbedResponse, regexGroups);

        if (StringUtils.isSet(stubbedResponse.getLatency())) {
            final long latency = Long.parseLong(stubbedResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
        }
        response.setStatus(stubbedResponse.getHttpStatusCode().getCode());

        final byte[] responseBody = stubbedResponse.getResponseBodyAsBytes();
        if (stubbedResponse.isFilePathContainsTemplateTokens()) {
            final String resolvedPath = replaceTokensInString(stubbedResponse.getRawFileAbsolutePath(), regexGroups);
            final File resolvedFile = new File(resolvedPath);
            if (resolvedFile.exists()) {
                writeOutputStream(response, getBytesUtf8(replaceTokens(fileToBytes(resolvedFile), regexGroups)));
            } else {
                response.setStatus(HttpStatus.NOT_FOUND_404);
            }
        } else if (stubbedResponse.isBodyContainsTemplateTokens()) {
            writeOutputStream(response, getBytesUtf8(replaceTokens(responseBody, regexGroups)));
        } else {
            writeOutputStream(response, responseBody);
        }
    }

    private void setResponseStubbedHeaders(
            final HttpServletResponse response,
            final StubResponse stubResponse,
            final Map<String, String> regexGroups) {
        for (final Map.Entry<String, String> headerPair :
                stubResponse.getHeaders().entrySet()) {
            String responseHeaderValue = headerPair.getValue();
            if (isTokenized(responseHeaderValue)) {
                responseHeaderValue = replaceTokensInString(headerPair.getValue(), regexGroups);
            }
            response.setHeader(headerPair.getKey(), responseHeaderValue);
        }
    }

    private void writeOutputStream(final HttpServletResponse response, final byte[] responseBody) throws IOException {
        try (final OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(responseBody);
            outputStream.flush();
        }
    }
}
