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

import javax.servlet.http.HttpServletRequest;

public final class AdminResponseHandlingStrategyFactory {

    private AdminResponseHandlingStrategyFactory() {}

    public static AdminResponseHandlingStrategy getStrategy(final HttpServletRequest request) {

        final String method = request.getMethod();
        final HttpVerbsEnum verbEnum;

        try {
            verbEnum = HttpVerbsEnum.valueOf(method);
        } catch (final IllegalArgumentException ex) {
            return new NullHandlingStrategy();
        }

        switch (verbEnum) {
            case POST:
                return new PostHandlingStrategy();

            case PUT:
                return new PutHandlingStrategy();

            case DELETE:
                return new DeleteHandlingStrategy();

            default:
                return new GetHandlingStrategy();
        }
    }
}
