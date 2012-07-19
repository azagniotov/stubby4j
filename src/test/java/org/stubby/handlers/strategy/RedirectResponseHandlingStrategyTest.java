package org.stubby.handlers.strategy;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.handlers.HttpRequestInfo;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/18/12, 10:11 AM
 */
public class RedirectResponseHandlingStrategyTest {

   private static final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);
   private static final HttpRequestInfo mockHttpRequestInfo = Mockito.mock(HttpRequestInfo.class);

   private static HandlingStrategy redirectResponseHandlingStrategy;

   @BeforeClass
   public static void beforeClass() throws Exception {
      redirectResponseHandlingStrategy = new RedirectResponseHandlingStrategy(mockStubResponse);
   }

   private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT_HTML_UTF_8);
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.PRAGMA, "no-cache");
      verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeaders.EXPIRES, 0);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithoutLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);

      redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockHttpRequestInfo);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.LOCATION, mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONNECTION, "close");
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockStubResponse.getLatency()).thenReturn("100");

      redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockHttpRequestInfo);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.LOCATION, mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONNECTION, "close");
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldCheckLatencyDelayWhenHandlingRedirectResponseWithLatency() throws Exception {

      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockStubResponse.getLatency()).thenReturn("100");

      long before = System.currentTimeMillis();
      redirectResponseHandlingStrategy.handle(mockHttpServletResponse, mockHttpRequestInfo);
      long after = System.currentTimeMillis();

      Assert.assertTrue((after - before) > 100);

      verifyMainHeaders(mockHttpServletResponse);
   }
}
