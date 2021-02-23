package io.github.azagniotov.stubby4j.handlers.strategy;

import io.github.azagniotov.stubby4j.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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

    @Before
    public void setUp() {
        when(mockStubResponse.getHeaders()).thenReturn(new HashMap<String, String>() {{
            put("location", "http://location.com");
        }});
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingTemporaryRedirectResponseWithoutLatency() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.MOVED_TEMPORARILY);

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse).setStatus(HttpStatus.MOVED_TEMPORARILY_302);
        verify(mockHttpServletResponse).setHeader(HttpHeader.LOCATION.asString(), mockStubResponse.getHeaders().get("location"));
        verify(mockHttpServletResponse).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingPermanentRedirectResponseWithoutLatency() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.MOVED_PERMANENTLY);

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
        verify(mockHttpServletResponse).setHeader(HttpHeader.LOCATION.asString(), mockStubResponse.getHeaders().get("location"));
        verify(mockHttpServletResponse).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithLatency() throws Exception {
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.MOVED_PERMANENTLY);
        when(mockStubResponse.getLatency()).thenReturn("100");

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
        verify(mockHttpServletResponse).setHeader(HttpHeader.LOCATION.asString(), mockStubResponse.getHeaders().get("location"));
        verify(mockHttpServletResponse).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);
    }

    @Test
    public void shouldReturnReplacedValueInLocationHeaderWhenQueryParamHasDynamicToken() throws Exception {
        String redirectUrlDomain = "test.com";
        String tokenizedLocationHeaderValue = "https://<% query.redirect_uri.1 %>/auth";

        when(mockAssertionRequest.getRegexGroups()).thenReturn(new TreeMap<String, String>() {{
            put("query.redirect_uri.1", redirectUrlDomain);
        }});
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.MOVED_TEMPORARILY);
        when(mockStubResponse.getHeaders()).thenReturn(new HashMap<String, String>() {{
            put("location", tokenizedLocationHeaderValue);
        }});

        redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

        verify(mockHttpServletResponse).setStatus(HttpStatus.MOVED_TEMPORARILY_302);
        verify(mockHttpServletResponse).setHeader(HttpHeader.LOCATION.asString(), "https://test.com/auth");
        verify(mockHttpServletResponse).setHeader(HttpHeader.CONNECTION.asString(), "close");
        verifyMainHeaders(mockHttpServletResponse);

    }

    private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
        verify(mockHttpServletResponse).setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
        verify(mockHttpServletResponse).setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
        verify(mockHttpServletResponse).setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-stage, must-revalidate");
        verify(mockHttpServletResponse).setHeader(HttpHeader.PRAGMA.asString(), "no-cache");
        verify(mockHttpServletResponse).setDateHeader(HttpHeader.EXPIRES.asString(), 0);
    }
}
