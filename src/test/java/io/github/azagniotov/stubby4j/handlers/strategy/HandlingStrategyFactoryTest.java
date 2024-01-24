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

package io.github.azagniotov.stubby4j.handlers.strategy;

import static com.google.common.truth.Truth.assertThat;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Test;

public class HandlingStrategyFactoryTest {

    @Test
    public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.okResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.notFoundResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.unauthorizedResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(UnauthorizedResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder()
                .withHttpStatusCode(Code.MOVED_PERMANENTLY)
                .build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder()
                .withHttpStatusCode(Code.MOVED_TEMPORARILY)
                .build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found() throws Exception {
        final StubResponse stubResponse =
                new StubResponse.Builder().withHttpStatusCode(Code.FOUND).build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303() throws Exception {
        final StubResponse stubResponse =
                new StubResponse.Builder().withHttpStatusCode(Code.SEE_OTHER).build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder()
                .withHttpStatusCode(Code.TEMPORARY_REDIRECT)
                .build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308() throws Exception {
        final StubResponse stubResponse = new StubResponse.Builder()
                .withHttpStatusCode(Code.PERMANENT_REDIRECT)
                .build();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }
}
