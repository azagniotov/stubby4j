/*
HTTP stub server written in Java with embedded Jetty

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

package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.azagniotov.stubby4j.utils.FileUtils.fileToBytes;
import static io.github.azagniotov.stubby4j.utils.HandlerUtils.setResponseMainHeaders;
import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isTokenized;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokens;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokensInString;

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

   private void setResponseStubbedHeaders(final HttpServletResponse response, final StubResponse stubResponse, final Map<String, String> regexGroups) {
      for (final Map.Entry<String, String> headerPair : stubResponse.getHeaders().entrySet()) {
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
