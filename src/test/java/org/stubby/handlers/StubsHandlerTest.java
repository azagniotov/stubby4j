package org.stubby.handlers;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.database.DataStore;
import org.stubby.yaml.stubs.NotFoundStubResponse;
import org.stubby.yaml.stubs.StubResponse;
import org.stubby.yaml.stubs.StubResponseTypes;
import org.stubby.yaml.stubs.UnauthorizedStubResponse;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 6/30/12, 8:15 PM
 */

public class StubsHandlerTest {

   private DataStore mockDataStore = Mockito.mock(DataStore.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   private final String someResultsMessage = "we have results";

   @Before
   public void beforeTest() throws Exception {

   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestWithNoResults() throws Exception {

      final String requestPathInfo = "/path/1";

      final HttpRequestInfo mockHttpRequestInfo = Mockito.mock(HttpRequestInfo.class);
      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.NOTFOUND);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.NOT_FOUND_404);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.NOT_FOUND_404, "No data found for GET request at URI /path/1");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithNoResults() throws Exception {

      final String postData = "postData";
      final String requestPathInfo = "/path/1";

      final HttpRequestInfo mockHttpRequestInfo = Mockito.mock(HttpRequestInfo.class);
      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.NOTFOUND);
      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.NOT_FOUND_404);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.NOT_FOUND_404, "No data found for POST request at URI /path/1 for post data: " + postData);
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithMissingPostData() throws Exception {

      final String requestPathInfo = "/path/1";

      final HttpRequestInfo mockHttpRequestInfo = Mockito.mock(HttpRequestInfo.class);
      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, never()).sendError(HttpStatus.BAD_REQUEST_400, StubsHandler.BAD_POST_REQUEST_MESSAGE);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithEmptyPostData() throws Exception {

      final String requestPathInfo = "/path/1";

      final HttpRequestInfo mockHttpRequestInfo = Mockito.mock(HttpRequestInfo.class);
      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final InputStream inputStream = new ByteArrayInputStream("".getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, never()).sendError(HttpStatus.BAD_REQUEST_400, StubsHandler.BAD_POST_REQUEST_MESSAGE);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithSomeResults() throws Exception {

      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithNoAuthorizationHeaderSet() throws Exception {

      final String requestPathInfo = "/path/1";

      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.UNAUTHORIZED_401);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.UNAUTHORIZED_401, "You are not authorized to view this page without supplied 'Authorization' HTTP header");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestWithWrongCredentialsInAuthorizationHeader() throws Exception {

      final String requestPathInfo = "/path/1";

      // We already found that it was unauthorized
      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getHeader(HttpRequestInfo.AUTH_HEADER)).thenReturn("Basic Ym9iOnNlY3JldA==");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final HttpRequestInfo httpRequestInfo = new HttpRequestInfo(mockHttpServletRequest, null);
      when(mockDataStore.findStubResponseFor(httpRequestInfo)).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.UNAUTHORIZED_401);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.UNAUTHORIZED_401, "Unauthorized with supplied encoded credentials: 'Ym9iOnNlY3JldA==' which decodes to 'bob:secret'");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithEmptyAuthorizationHeaderSet() throws Exception {

      final String requestPathInfo = "/path/1";

      // We already found that it was unauthorized
      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getHeader(HttpRequestInfo.AUTH_HEADER)).thenReturn("");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final HttpRequestInfo httpRequestInfo = new HttpRequestInfo(mockHttpServletRequest, null);
      when(mockDataStore.findStubResponseFor(httpRequestInfo)).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.INTERNAL_SERVER_ERROR_500, "java.lang.StringIndexOutOfBoundsException: String index out of range: -6");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestWithIncompleteAuthorizationHeaderSet() throws Exception {

      final String requestPathInfo = "/path/1";

      // We already found that it was unauthorized
      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getHeader(HttpRequestInfo.AUTH_HEADER)).thenReturn("Basic ");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getQueryString()).thenReturn("");

      final HttpRequestInfo httpRequestInfo = new HttpRequestInfo(mockHttpServletRequest, null);
      when(mockDataStore.findStubResponseFor(httpRequestInfo)).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.UNAUTHORIZED_401);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.UNAUTHORIZED_401, "Unauthorized with supplied encoded credentials: '' which decodes to ''");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithMatch() throws Exception {
      final String postData = "postData";
      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);

      final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
      Mockito.when(mockHttpServletRequest.getInputStream()).thenReturn(new ServletInputStream() {
         @Override
         public int read() throws IOException {
            return inputStream.read();
         }
      });

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithLatency() throws Exception {

      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockStubResponse.getLatency()).thenReturn("50");
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getBody()).thenReturn(someResultsMessage);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, times(1)).println(someResultsMessage);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithInvalidLatency() throws Exception {
      final String method = "GET";
      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(method);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockStubResponse.getLatency()).thenReturn("43rl4knt3l");
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockDataStore.findStubResponseFor(Mockito.any(HttpRequestInfo.class))).thenReturn(mockStubResponse);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, never()).println(someResultsMessage);
   }
}