package org.stubby.handlers;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.database.DataStore;
import org.stubby.server.JettyOrchestrator;
import org.stubby.utils.HandlerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:46 AM
 */
public class AdminHandlerTest {

   private DataStore mockDataStore = Mockito.mock(DataStore.class);
   private JettyOrchestrator mockJettyOrchestrator = Mockito.mock(JettyOrchestrator.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   @Before
   public void beforeTest() throws Exception {
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      doNothing().when(mockPrintWriter).println(Mockito.anyString());
   }

   @After
   public void afterTest() throws Exception {
      verify(mockRequest, times(1)).setHandled(true);
      verify(mockHttpServletResponse, times(1)).setContentType(MimeTypes.TEXT_HTML_UTF_8);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestOnIndexPage() throws Exception {
      final String requestPathInfo = "/";
      final AdminHandler clientHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, times(1)).println(adminHandlerHtml);
   }
}
