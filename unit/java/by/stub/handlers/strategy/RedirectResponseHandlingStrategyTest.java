package by.stub.handlers.strategy;

import by.stub.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/18/12, 10:11 AM
 */

public class RedirectResponseHandlingStrategyTest {

   private static final StubResponse mockStubResponse = mock(StubResponse.class);
   private static final StubRequest mockAssertionRequest = mock(StubRequest.class);

   private static StubResponseHandlingStrategy redirectResponseStubResponseHandlingStrategy;
   private HttpServletResponse mockHttpServletResponse;

   @BeforeClass
   public static void beforeClass() throws Exception {
      redirectResponseStubResponseHandlingStrategy = new RedirectResponseHandlingStrategy(mockStubResponse);
   }

   @Before
   public void beforeEach() throws Exception {
      mockHttpServletResponse = mock(HttpServletResponse.class);
   }

   private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.SERVER.name(), HandlerUtils.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONTENT_TYPE.name(), "text/html;charset=UTF-8");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CACHE_CONTROL.name(), "no-cache, no-store, must-revalidate");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.PRAGMA.name(), "no-cache");
      verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeader.EXPIRES.name(), 0);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithoutLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);

      redirectResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.LOCATION.name(), mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONNECTION.name(), "close");
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingRedirectResponseWithLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

      when(mockStubResponse.getStatus()).thenReturn("301");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockStubResponse.getLatency()).thenReturn("100");

      redirectResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.MOVED_PERMANENTLY_301);
      verify(mockHttpServletResponse, times(1)).setStatus(Integer.parseInt(mockStubResponse.getStatus()));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.LOCATION.name(), mockStubResponse.getHeaders().get("location"));
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONNECTION.name(), "close");
      verifyMainHeaders(mockHttpServletResponse);
   }
}
