package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;
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
    private static final ServletOutputStream SERVLET_OUTPUT_STREAM = new ServletOutputStream() {

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
    };
    private static final byte[] EMPTY_BYTES = {};

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

    @Test
    public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithoutLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(EMPTY_BYTES);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(SERVLET_OUTPUT_STREAM);

        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(EMPTY_BYTES);
        when(mockStubResponse.getLatency()).thenReturn("100");
        when(mockHttpServletResponse.getOutputStream()).thenReturn(SERVLET_OUTPUT_STREAM);

        when(mockAssertionRequest.getQuery()).thenReturn(new HashMap<>());
        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldCheckLatencyDelayWhenHandlingDefaultResponseWithLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("200");
        when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(getBytesUtf8(SOME_RESULTS_MESSAGE));
        when(mockStubResponse.getLatency()).thenReturn("100");
        when(mockHttpServletResponse.getOutputStream()).thenReturn(SERVLET_OUTPUT_STREAM);

        long before = System.currentTimeMillis();
        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);
        long after = System.currentTimeMillis();

        assertThat(after - before).isGreaterThanOrEqualTo(100);

        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldReturnReplacedValueInResponseHeaderWhenRequestBodyHasDynamicToken() throws Exception {
        final String nonce = UUID.randomUUID().toString();
        final String headerValuePrefix = "redirect-uri=https://google.com&nonce=";

        when(mockHttpServletResponse.getOutputStream()).thenReturn(SERVLET_OUTPUT_STREAM);
        when(mockAssertionRequest.getRegexGroups()).thenReturn(new TreeMap<String, String>() {{
            put("post.1", nonce);
        }});

        when(mockStubResponse.getStatus()).thenReturn("302");
        when(mockStubResponse.getHeaders()).thenReturn(new HashMap<String, String>() {{
            put("Location", headerValuePrefix + "<%post.1%>");
        }});
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(getBytesUtf8(SOME_RESULTS_MESSAGE));

        defaultResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.LOCATION.asString(), headerValuePrefix + nonce);
        verifyMainHeaders(mockHttpServletResponse);
    }

    private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-stage, must-revalidate");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.PRAGMA.asString(), "no-cache");
        verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeader.EXPIRES.asString(), 0);
    }
}
