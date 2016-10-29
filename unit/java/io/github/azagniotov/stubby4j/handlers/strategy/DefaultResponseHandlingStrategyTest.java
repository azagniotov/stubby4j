package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/18/12, 10:11 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultResponseHandlingStrategyTest {

    private static final String SOME_RESULTS_MESSAGE = "we have results";

    @Mock
    private StubResponse mockStubResponse;

    @Mock
    private StubRequest mockAssertionRequest;

    @Mock
    private PrintWriter mockPrintWriter;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @InjectMocks
    private DefaultResponseHandlingStrategy defaultResponseHandlingStrategy;

    private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-stage, must-revalidate");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.PRAGMA.asString(), "no-cache");
        verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeader.EXPIRES.asString(), 0);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithoutLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});
        Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

            @Override
            public void write(final int i) throws IOException {

            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener) {

            }
        });

        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});
        when(mockStubResponse.getLatency()).thenReturn("100");

        Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

            @Override
            public void write(final int i) throws IOException {

            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener) {

            }
        });

        when(mockAssertionRequest.getQuery()).thenReturn(new HashMap<String, String>());
        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldCheckLatencyDelayWhenHandlingDefaultResponseWithLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(SOME_RESULTS_MESSAGE.getBytes(StringUtils.UTF_8));
        when(mockStubResponse.getLatency()).thenReturn("100");
        Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

            @Override
            public void write(final int i) throws IOException {

            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(final WriteListener writeListener) {

            }
        });

        long before = System.currentTimeMillis();
        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);
        long after = System.currentTimeMillis();

        assertThat(after - before).isGreaterThanOrEqualTo(100);

        verifyMainHeaders(mockHttpServletResponse);
    }
}
