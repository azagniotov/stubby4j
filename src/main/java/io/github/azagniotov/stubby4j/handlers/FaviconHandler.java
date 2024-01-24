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

package io.github.azagniotov.stubby4j.handlers;

import static io.github.azagniotov.stubby4j.utils.ObjectUtils.isNotNull;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeClassCoverageExclusion;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.resource.Resource;

@GeneratedCodeClassCoverageExclusion
public class FaviconHandler extends AbstractHandler implements AbstractHandlerExtension {

    private final long faviconModified = (System.currentTimeMillis() / 1000) * 1000L;
    private byte[] faviconBytes;

    public FaviconHandler() {
        try {
            final URL fav = this.getClass().getClassLoader().getResource("ui/images/favicon.ico");
            if (isNotNull(fav)) {
                faviconBytes = IO.readBytes(Resource.newResource(fav).getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(
            final String target,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException, ServletException {
        if (logAndCheckIsHandled("favicon", baseRequest, request, response)) {
            return;
        }
        baseRequest.setHandled(true);

        if (isNotNull(faviconBytes)
                && HttpMethod.GET.is(request.getMethod())
                && request.getRequestURI().equals("/favicon.ico")) {
            if (request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString()) == faviconModified)
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/x-icon");
                response.setContentLength(faviconBytes.length);
                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), faviconModified);
                response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
                response.getOutputStream().write(faviconBytes);
            }
        }
    }
}
