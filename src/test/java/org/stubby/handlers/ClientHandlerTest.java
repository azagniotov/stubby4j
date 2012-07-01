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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/30/12, 8:15 PM
 */
public class ClientHandlerTest {

   private Repository mockRepository = Mockito.mock(Repository.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   @SuppressWarnings("unchecked")
   private Map<String, String> mockResponseValues = Mockito.mock(Map.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   private final String noResultsMessage = "no results";
   private final String someResultsMessage = "we have results";

   @Before
   public void beforeTest() throws Exception {
      when(mockResponseValues.get(Repository.NOCONTENT_MSG_KEY)).thenReturn(noResultsMessage);
      when(mockResponseValues.get(Repository.TBL_COLUMN_BODY)).thenReturn(someResultsMessage);
      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      doNothing().when(mockPrintWriter).println(Mockito.anyString());
   }

   @After
   public void afterTest() throws Exception {
      verify(mockRequest, times(1)).setHandled(true);
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.SERVER, HandlerHelper.constructHeaderServerName());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.DATE, new Date().toString());
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.PRAGMA, "no-cache");
      verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeaders.EXPIRES, 0);

      verify(mockHttpServletResponse, never()).setHeader(Repository.TBL_COLUMN_BODY, "no-cache");
      verify(mockHttpServletResponse, never()).setHeader(Repository.NOCONTENT_MSG_KEY, "no-cache");
      verify(mockHttpServletResponse, never()).setHeader(Repository.TBL_COLUMN_STATUS, "no-cache");
   }

   @Test
   public void verifyHandleGetRequestWithNoResults() throws Exception {
      final String method = "GET";
      final String requestPathInfo = "/path/1";

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockRepository.retrieveResponseFor(requestPathInfo, method, null)).thenReturn(mockResponseValues);
      when(mockResponseValues.size()).thenReturn(1);
      when(mockResponseValues.get(Repository.TBL_COLUMN_STATUS)).thenReturn("200");
      when(mockResponseValues.get(Repository.NOCONTENT_MSG_KEY)).thenReturn(noResultsMessage);

      final ClientHandler clientHandler = new ClientHandler(mockRepository);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.NOT_FOUND_404);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.NOT_FOUND_404, noResultsMessage);
   }


   @Test
   public void verifyHandleGetRequestWithSomeResults() throws Exception {
      final String method = "GET";
      final String requestPathInfo = "/path/1";

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockRepository.retrieveResponseFor(requestPathInfo, method, null)).thenReturn(mockResponseValues);
      when(mockResponseValues.size()).thenReturn(2);
      when(mockResponseValues.get(Repository.TBL_COLUMN_STATUS)).thenReturn("200");

      final ClientHandler clientHandler = new ClientHandler(mockRepository);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }

   @Test
   public void verifyHandlePostRequestWithEmptyPostBody() throws Exception {
      final String method = "POST";
      final String requestPathInfo = "/path/1";

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getInputStream()).thenReturn(null);

      final ClientHandler clientHandler = new ClientHandler(mockRepository);
      clientHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.BAD_REQUEST_400, ClientHandler.BAD_POST_REQUEST_MESSAGE);
   }
}