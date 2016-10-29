package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
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

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/18/12, 10:11 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class RedirectResponseHandlingStrategyTest {

    @Mock
    private StubResponse mockStubResponse;

    @Mock
    private StubRequest mockAssertionRequest;

    @Mock
    private PrintWriter mockPrintWriter;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @InjectMocks
    private RedirectResponseHandlingStrategy redirectResponseHandlingStrategy;


    private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-stage, must-revalidate");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.PRAGMA.asString(), "no-cache");
        verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeader.EXPIRES.asString(), 0);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithoutLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("301");
        when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
        verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.LOCATION.asString(), mockStubResponse.getHeaders().get("location"));
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithLatency() throws Exception {
        when(mockStubResponse.getStatus()).thenReturn("301");
        when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockStubResponse.getLatency()).thenReturn("100");

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
        verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.LOCATION.asString(), mockStubResponse.getHeaders().get("location"));
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);
    }
}
