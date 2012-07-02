package org.stubby.handlers;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.database.DataStore;
import org.stubby.utils.HandlerUtils;
import org.stubby.yaml.stubs.NullStubResponse;
import org.stubby.yaml.stubs.StubResponse;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/30/12, 8:15 PM
 */
public class ClientHandlerTest {

   private DataStore mockDataStore = Mockito.mock(DataStore.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   private final String someResultsMessage = "we have results";

   @Before
   public void beforeTest() throws Exception {
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      doNothing().when(mockPrintWriter).println(Mockito.anyString());
   }

   @After
   public void afterTest() throws Exception {
      verify(mockRequest, times(1)).setHandled(true);
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.DATE, new Date().toString());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.PRAGMA, "no-cache");
      verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeaders.EXPIRES, 0);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestWithNoResults() throws Exception {
      final String method = "GET";
      final String requestPathInfo = "/path/1";

      final NullStubResponse mockStubResponse = Mockito.mock(NullStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findResponseFor(requestPathInfo, method, null)).thenReturn(mockStubResponse);

      final ClientHandler clientHandler = new ClientHandler(mockDataStore);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.NOT_FOUND_404);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.NOT_FOUND_404, "No data found for GET request at URI /path/1");
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithSomeResults() throws Exception {
      final String method = "GET";
      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findResponseFor(requestPathInfo, method, null)).thenReturn(mockStubResponse);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);
      when(mockStubResponse.getHeaders()).thenReturn(new HashMap<String, String>());

      final ClientHandler clientHandler = new ClientHandler(mockDataStore);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithEmptyPostBody() throws Exception {
      final String method = "POST";
      final String requestPathInfo = "/path/1";

      final NullStubResponse mockStubResponse = Mockito.mock(NullStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findResponseFor(requestPathInfo, method, null)).thenReturn(mockStubResponse);

      final ClientHandler clientHandler = new ClientHandler(mockDataStore);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.BAD_REQUEST_400, ClientHandler.BAD_POST_REQUEST_MESSAGE);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithNoMatch() throws Exception {
      final String postData = "postData";
      final String method = "POST";
      final String requestPathInfo = "/path/1";

      final NullStubResponse mockStubResponse = Mockito.mock(NullStubResponse.class);
      final ServletInputStream mockServletInputStream = Mockito.mock(ServletInputStream.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);

      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      when(mockDataStore.findResponseFor(requestPathInfo, method, postData)).thenReturn(mockStubResponse);

      final ClientHandler clientHandler = new ClientHandler(mockDataStore);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.NOT_FOUND_404);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.NOT_FOUND_404, "No data found for POST request at URI /path/1 for post data: " + postData);
   }


   @Test
   public void verifyBehaviourDuringHandlePostRequestWithMatch() throws Exception {
      final String postData = "postData";
      final String method = "POST";
      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);
      final ServletInputStream mockServletInputStream = Mockito.mock(ServletInputStream.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);
      when(mockStubResponse.getHeaders()).thenReturn(new HashMap<String, String>());

      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      when(mockDataStore.findResponseFor(requestPathInfo, method, postData)).thenReturn(mockStubResponse);

      final ClientHandler clientHandler = new ClientHandler(mockDataStore);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }

}