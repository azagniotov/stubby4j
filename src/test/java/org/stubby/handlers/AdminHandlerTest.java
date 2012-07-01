package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.database.Repository;
import org.stubby.server.JettyOrchestrator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 7/1/12, 10:17 AM
 */
public class AdminHandlerTest {

   private Repository mockRepository = Mockito.mock(Repository.class);
   private JettyOrchestrator mockJettyOrchestrator = Mockito.mock(JettyOrchestrator.class);

   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   @Before
   public void beforeTest() throws Exception {
      when(mockJettyOrchestrator.getCurrentHost()).thenReturn("localhost");
      when(mockJettyOrchestrator.getCurrentClientPort()).thenReturn(8885);
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      doNothing().when(mockPrintWriter).println(Mockito.anyString());
   }

   @After
   public void afterTest() throws Exception {
      verify(mockRequest, times(1)).setHandled(true);
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.SERVER, HandlerHelper.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockHttpServletResponse, times(1)).setContentType(MimeTypes.TEXT_HTML_UTF_8);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestOnIndexPage() throws Exception {

      final String requestPathInfo = "/";

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      final AdminHandler adminHandler = new AdminHandler(mockRepository, mockJettyOrchestrator);
      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletRequest, times(1)).getContextPath();
      verify(mockPrintWriter, times(1)).println(Mockito.any(String.class));
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestOnPingPage() throws Exception {

      final String requestPathInfo = "/ping";
      final List<Map<String, Object>> dummyData = new LinkedList<Map<String, Object>>();
      dummyData.add(new HashMap<String, Object>());

      @SuppressWarnings("unchecked")
      List<List<Map<String, Object>>> mockHttpConfigData = Mockito.mock(List.class);

      when(mockHttpConfigData.get(Mockito.anyInt())).thenReturn(dummyData);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockRepository.getHttpConfigData()).thenReturn(mockHttpConfigData);

      final AdminHandler adminHandler = new AdminHandler(mockRepository, mockJettyOrchestrator);
      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setContentType(MimeTypes.TEXT_HTML_UTF_8);
      verify(mockPrintWriter, times(1)).println(Mockito.any(String.class));
   }
}