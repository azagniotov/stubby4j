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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractHandlerExtensionTest {

    @Mock
    private Request mockBaseRequest;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenBaseRequestHandled() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();

        when(mockBaseRequest.isHandled()).thenReturn(true);

        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled(
                "someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        assertThat(isHandled).isTrue();
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenResponseCommitted() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();

        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(true);

        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled(
                "someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        assertThat(isHandled).isTrue();
    }

    @Test
    public void shouldDetermineRequestAsNotHandled() throws Exception {
        AbstractHandlerExtension abstractHandlerExtension = new DummyHandler();

        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(false);

        final boolean isHandled = abstractHandlerExtension.logAndCheckIsHandled(
                "someName", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        assertThat(isHandled).isFalse();
    }

    private class DummyHandler implements AbstractHandlerExtension {}
}
