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

import io.github.azagniotov.stubby4j.utils.ConsoleUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

public interface AbstractHandlerExtension {

    default boolean logAndCheckIsHandled(
            final String handlerName,
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        ConsoleUtils.logIncomingRequest(request);
        if (baseRequest.isHandled() || response.isCommitted()) {
            ConsoleUtils.logIncomingRequestError(
                    request, handlerName, "HTTP response was committed or base request was handled, aborting..");
            return true;
        }

        return false;
    }
}
