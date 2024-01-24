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
import static org.mockito.Mockito.when;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StubsResponseHandlingStrategyFactoryTest {

    private static final byte[] EMPTY_BYTES = {};

    @Mock
    private StubResponse mockStubResponse;

    @Test
    public void shouldReturnNotFoundResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(EMPTY_BYTES);

        StubResponseHandlingStrategy handlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);

        assertThat(handlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldReturnDefaultResponseHandlingStrategyWhen404ResponseHasNoBody() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(HttpStatus.Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn("something".getBytes());

        StubResponseHandlingStrategy handlingStrategy =
                StubsResponseHandlingStrategyFactory.getStrategy(mockStubResponse);

        assertThat(handlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }
}
