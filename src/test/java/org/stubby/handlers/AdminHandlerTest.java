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
import org.stubby.yaml.YamlParser;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
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
   private YamlParser mockYamlParser = Mockito.mock(YamlParser.class);
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
      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, times(1)).println(adminHandlerHtml);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestOnPingPage() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_PING;
      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);

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

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      when(mockJettyOrchestrator.getDataStore()).thenReturn(mockDataStore);
      when(mockDataStore.getStubHttpLifecycles()).thenReturn(new LinkedList<StubHttpLifecycle>());
      when(mockJettyOrchestrator.getYamlParser()).thenReturn(mockYamlParser);
      when(mockYamlParser.getLoadedConfigYamlPath()).thenReturn("/User/filename.yaml");
      doThrow(exceptionClass).when(mockPrintWriter).println(Mockito.anyString());

      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);
      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, never()).println(adminHandlerHtml);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, exceptionClass.getCanonicalName());
   }

   @Test
   public void verifyBehaviourDuringGetRequestOnRegisterNewEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_STUBDATA_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);

      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      final String adminHandlerHtml = HandlerUtils.populateHtmlTemplate("index", mockHttpServletRequest.getContextPath());
      verify(mockPrintWriter, never()).println(adminHandlerHtml);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.METHOD_NOT_ALLOWED_405, String.format("Method GET is not allowed on URI %s", mockHttpServletRequest.getPathInfo()));
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CREATED_201);
   }

   @Test
   public void verifyBehaviourDuringPostRequestOnRegisterNewIncompleteEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_STUBDATA_NEW;
      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);

      when(mockHttpServletRequest.getMethod()).thenReturn("post");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.BAD_REQUEST_400, ClientHandler.BAD_POST_REQUEST_MESSAGE);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CREATED_201);
   }

   @Test
   public void verifyBehaviourDuringPostRequestOnRegisterNewEndpoint() throws Exception {
      final String requestPathInfo = AdminHandler.RESOURCE_STUBDATA_NEW;

      @SuppressWarnings("unchecked")
      final List<StubHttpLifecycle> mockStubHttpLifecycleList = Mockito.mock(List.class);

      when(mockJettyOrchestrator.getDataStore()).thenReturn(mockDataStore);
      when(mockJettyOrchestrator.getYamlParser()).thenReturn(mockYamlParser);
      when(mockYamlParser.getLoadedConfigYamlPath()).thenReturn("/User/filename.yaml");
      when(mockHttpServletRequest.getMethod()).thenReturn("post");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getContextPath()).thenReturn(requestPathInfo);
      when(mockDataStore.getStubHttpLifecycles()).thenReturn(mockStubHttpLifecycleList);
      when(mockStubHttpLifecycleList.size()).thenReturn(1);

      final AdminHandler adminHandler = new AdminHandler(mockJettyOrchestrator);

      final String postData = "" +
            "-  request:\n" +
            "      method: GET\n" +
            "      url: /item/8\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "      status: 200\n" +
            "      body: {\"alex\" : \"zagniotov\"}";
      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      adminHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.CONFLICT_409);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);

      verify(mockStubHttpLifecycleList, times(1)).clear();
      verify(mockDataStore, times(1)).setStubHttpLifecycles(Mockito.anyListOf(StubHttpLifecycle.class));
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.CREATED_201);
   }
}