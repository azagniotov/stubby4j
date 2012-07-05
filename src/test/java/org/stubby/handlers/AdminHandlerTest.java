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
import org.stubby.yaml.stubs.StubHttpLifecycle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, times(1)).println(adminHandlerHtml);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestOnPingPage() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_PING;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, never()).println(adminHandlerHtml);

   }

   @Test
   public void verifyBehaviourDuringExceptionWhenSubmittingGetRequestOnPingPage() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_PING;
      final Class<IllegalAccessException> exceptionClass = IllegalAccessException.class;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      doThrow(exceptionClass).when(mockPrintWriter).println(Mockito.anyString());

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, never()).println(adminHandlerHtml);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, exceptionClass.getCanonicalName());
   }

   @Test
   public void verifyBehaviourDuringGetRequestOnRegisterNewEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_ENDPOINT_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, never()).println(adminHandlerHtml);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.METHOD_NOT_ALLOWED_405, String.format("Method GET is not allowed on URI %s", mockHttpServletRequest.getPathInfo()));
   }

   @Test
   public void verifyBehaviourDuringPostRequestOnRegisterNewIncompleteEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_ENDPOINT_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      when(mockHttpServletRequest.getMethod()).thenReturn("post");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getParameter("url")).thenReturn("/some/endpoint");
      when(mockHttpServletRequest.getParameter("responseHeaders")).thenReturn("");

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.BAD_REQUEST_400,
            "Endpoint content provided is not complete, was given StubHttpLifecycle{request=StubRequest{url='/some/endpoint', method='null', postBody='null', headers={}}, response=StubResponse{status='null', body='null', headers={}}}");

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CREATED_201);
   }

   @Test
   public void verifyBehaviourDuringPostRequestOnRegisterNewEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_ENDPOINT_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      @SuppressWarnings("unchecked")
      final List<StubHttpLifecycle> mockStubHttpLifecycleList = Mockito.mock(List.class);

      when(mockHttpServletRequest.getMethod()).thenReturn("post");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getParameter("method")).thenReturn("get");
      when(mockHttpServletRequest.getParameter("url")).thenReturn("/some/endpoint");
      when(mockHttpServletRequest.getParameter("status")).thenReturn("200");
      when(mockHttpServletRequest.getParameter("body")).thenReturn("{\"alex\" : \"zagniotov\"}");
      when(mockHttpServletRequest.getParameter("responseHeaders")).thenReturn("content-type=application/json");

      when(mockDataStore.getStubHttpLifecycles()).thenReturn(mockStubHttpLifecycleList);
      when(mockStubHttpLifecycleList.contains(Mockito.any(StubHttpLifecycle.class))).thenReturn(false);
      when(mockStubHttpLifecycleList.add(Mockito.any(StubHttpLifecycle.class))).thenReturn(true);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CONFLICT_409);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.CREATED_201);
   }

   @Test
   public void verifyBehaviourDuringPostRequestOnRegisterNewDuplicateEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_ENDPOINT_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockDataStore, mockJettyOrchestrator);

      @SuppressWarnings("unchecked")
      final List<StubHttpLifecycle> mockStubHttpLifecycleList = Mockito.mock(List.class);

      when(mockHttpServletRequest.getMethod()).thenReturn("post");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getParameter("method")).thenReturn("get");
      when(mockHttpServletRequest.getParameter("url")).thenReturn("/some/endpoint");
      when(mockHttpServletRequest.getParameter("status")).thenReturn("200");
      when(mockHttpServletRequest.getParameter("body")).thenReturn("{\"alex\" : \"zagniotov\"}");
      when(mockHttpServletRequest.getParameter("responseHeaders")).thenReturn("content-type=application/json");

      when(mockDataStore.getStubHttpLifecycles()).thenReturn(mockStubHttpLifecycleList);
      when(mockStubHttpLifecycleList.contains(Mockito.any(StubHttpLifecycle.class))).thenReturn(true);
      when(mockStubHttpLifecycleList.add(Mockito.any(StubHttpLifecycle.class))).thenReturn(true);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.CONFLICT_409);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CREATED_201);
   }
}
