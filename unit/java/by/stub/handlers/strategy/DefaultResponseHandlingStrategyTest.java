package by.stub.handlers.strategy;

import by.stub.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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

public class DefaultResponseHandlingStrategyTest {

   private static final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);
   private static final StubRequest mockAssertionRequest = Mockito.mock(StubRequest.class);

   private final String someResultsMessage = "we have results";

   private static StubResponseHandlingStrategy defaultResponseStubResponseHandlingStrategy;

   @BeforeClass
   public static void beforeClass() throws Exception {
      defaultResponseStubResponseHandlingStrategy = new DefaultResponseHandlingStrategy(mockStubResponse);
   }

   private void verifyMainHeaders(final HttpServletResponse mockHttpServletResponse) throws Exception {
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.SERVER.asString(), HandlerUtils.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CONTENT_TYPE.asString(), "text/html;charset=UTF-8");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.CACHE_CONTROL.asString(), "no-cache, no-store, must-revalidate");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeader.PRAGMA.asString(), "no-cache");
      verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeader.EXPIRES.asString(), 0);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithoutLatency() throws Exception {

      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});
      Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

         @Override
         public void write(final int i) throws IOException {

         }

         @Override public boolean isReady() {
            return false;
         }

         @Override public void setWriteListener(final WriteListener writeListener) {

         }
      });

      defaultResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldVerifyBehaviourWhenHandlingDefaultResponseWithLatency() throws Exception {

      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});
      when(mockStubResponse.getLatency()).thenReturn("100");

      Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

         @Override
         public void write(final int i) throws IOException {

         }

         @Override public boolean isReady() {
            return false;
         }

         @Override public void setWriteListener(final WriteListener writeListener) {

         }
      });

      when(mockAssertionRequest.getQuery()).thenReturn(new HashMap<String, String>());
      defaultResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verifyMainHeaders(mockHttpServletResponse);
   }

   @Test
   public void shouldCheckLatencyDelayWhenHandlingDefaultResponseWithLatency() throws Exception {

      final PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
      final HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(someResultsMessage.getBytes(StringUtils.UTF_8));
      when(mockStubResponse.getLatency()).thenReturn("100");
      Mockito.when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {

         @Override
         public void write(final int i) throws IOException {

         }

         @Override public boolean isReady() {
            return false;
         }

         @Override public void setWriteListener(final WriteListener writeListener) {

         }
      });

      long before = System.currentTimeMillis();
      defaultResponseStubResponseHandlingStrategy.handle(mockHttpServletResponse, mockAssertionRequest);
      long after = System.currentTimeMillis();

      assertThat(after - before).isGreaterThanOrEqualTo(100);

      verifyMainHeaders(mockHttpServletResponse);
   }
}
