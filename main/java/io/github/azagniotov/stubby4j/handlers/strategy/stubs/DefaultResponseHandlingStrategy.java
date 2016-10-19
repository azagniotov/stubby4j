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

import io.github.azagniotov.stubby4j.utils.FileUtils;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DefaultResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public DefaultResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
      HandlerUtils.setResponseMainHeaders(response);
      setStubResponseHeaders(foundStubResponse, response);

      if (StringUtils.isSet(foundStubResponse.getLatency())) {
         final long latency = Long.parseLong(foundStubResponse.getLatency());
         TimeUnit.MILLISECONDS.sleep(latency);
      }
      response.setStatus(Integer.parseInt(foundStubResponse.getStatus()));

      byte[] responseBody = foundStubResponse.getResponseBodyAsBytes();
      if (foundStubResponse.doesFilePathContainTemplateTokens()) {
         String resolvedPath = StringUtils.replaceTokensInString(
            foundStubResponse.getRawFile().getAbsolutePath(),
            assertionStubRequest.getRegexGroups());
         File resolvedFile = new File(resolvedPath);
         if(resolvedFile.exists()){
            responseBody = FileUtils.fileToBytes(resolvedFile);
         } else {
            response.setStatus(HttpStatus.NOT_FOUND_404);
         }
      } else if (foundStubResponse.isContainsTemplateTokens()) {
         final String replacedTemplate = StringUtils.replaceTokens(responseBody, assertionStubRequest.getRegexGroups());
         responseBody = StringUtils.getBytesUtf8(replacedTemplate);
      }

      final OutputStream streamOut = response.getOutputStream();
      streamOut.write(responseBody);
      streamOut.flush();
      streamOut.close();
   }

   private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
      response.setCharacterEncoding(StringUtils.UTF_8);
      for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
         response.setHeader(entry.getKey(), entry.getValue());
      }
   }
}
