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

package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

public class NullHandlingStrategy implements AdminResponseHandlingStrategy {

    @Override
    public void handle(
            final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository)
            throws IOException {
        response.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
        response.getWriter()
                .println(String.format(
                        "Method %s is not implemented on URI %s", request.getMethod(), request.getRequestURI()));
    }
}
