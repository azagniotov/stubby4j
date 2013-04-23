package by.stub.handlers.strategy;

import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
   private static final StubRequest mockAssertionRequest = Mockito.mock(StubRequest.class);

   private static StubResponseHandlingStrategy redirectResponseStubResponseHandlingStrategy;

   @BeforeClass
   public static void beforeClass() throws Exception {
      redirectResponseStubResponseHandlingStrategy = new RedirectResponseHandlingStrategy(mockStubResponse);
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
      final HttpServletResponseWithGetStatus mockHttpServletResponse = Mockito.mock(HttpServletResponseWithGetStatus.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);

      redirectResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.LOCATION, mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONNECTION, "close");
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
      final HttpServletResponseWithGetStatus mockHttpServletResponse = Mockito.mock(HttpServletResponseWithGetStatus.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockStubResponse.getLatency()).thenReturn("100");

      redirectResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.LOCATION, mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONNECTION, "close");
      verifyMainHeaders(mockHttpServletResponse);
   }
}
