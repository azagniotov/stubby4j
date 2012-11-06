package unit.by.stub.handlers;

import by.stub.cli.ANSITerminal;
import by.stub.database.DataStore;
import by.stub.handlers.StubsHandler;
import by.stub.yaml.stubs.NotFoundStubResponse;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import by.stub.yaml.stubs.StubResponseTypes;
import by.stub.yaml.stubs.UnauthorizedStubResponse;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Alexander Zagniotov
 * @since 6/30/12, 8:15 PM
 */
@SuppressWarnings("serial")
public class StubsHandlerTest {

   private DataStore mockDataStore = Mockito.mock(DataStore.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   private PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);

   private final String someResultsMessage = "we have results";

   @BeforeClass
   public static void beforeClass() throws Exception {
      ANSITerminal.muteConsole(true);
   }

   @Before
   public void beforeEach() throws Exception {
      mockDataStore = Mockito.mock(DataStore.class);
      mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   }

   @Test
   public void verifyBehaviourDuringHandleGetRequestWithNoResults() throws Exception {

      final String requestPathInfo = "/path/1";

      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
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

      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
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

      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
   }

   @Test
   public void verifyBehaviourDuringHandlePostRequestWithEmptyPostData() throws Exception {

      final String requestPathInfo = "/path/1";

      final NotFoundStubResponse mockStubResponse = Mockito.mock(NotFoundStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.POST);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
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
      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.OK_200);
   }


   @Test
   public void verifyBehaviourDuringHandleGetRequestWithSomeResults() throws Exception {

      final String requestPathInfo = "/path/1";

      final StubResponse mockStubResponse = Mockito.mock(StubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.DEFAULT);
      when(mockStubResponse.getStatus()).thenReturn("200");
      when(mockStubResponse.getResponseBody()).thenReturn(someResultsMessage);

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
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
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
   @Ignore
   public void verifyBehaviourDuringHandleGetRequestWithWrongCredentialsInAuthorizationHeader() throws Exception {

      final String requestPathInfo = "/path/1";


      final Enumeration<String> headerNames = Collections.enumeration(new ArrayList<String>() {{
         add(StubRequest.AUTH_HEADER);
      }});
      // We already found that it was unauthorized
      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);

      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockHttpServletRequest.getHeaderNames()).thenReturn(headerNames);
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic Ym9iOnNlY3JldA==");

      final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      when(mockDataStore.findStubResponseFor(assertionStubRequest)).thenReturn(mockStubResponse);

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
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);

      final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      when(mockDataStore.findStubResponseFor(assertionStubRequest)).thenReturn(mockStubResponse);

      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);
      when(mockStubResponse.getStatus()).thenReturn("200");

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.UNAUTHORIZED_401);
      verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.UNAUTHORIZED_401, "You are not authorized to view this page without supplied 'Authorization' HTTP header");
      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
   }

   @Test
   @Ignore
   public void verifyBehaviourDuringHandleGetRequestWithIncompleteAuthorizationHeaderSet() throws Exception {

      final String requestPathInfo = "/path/1";

      final Enumeration<String> headerNames = Collections.enumeration(new ArrayList<String>() {{
         add(StubRequest.AUTH_HEADER);
      }});

      // We already found that it was unauthorized
      final UnauthorizedStubResponse mockStubResponse = Mockito.mock(UnauthorizedStubResponse.class);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getHeaderNames()).thenReturn(headerNames);
      when(mockHttpServletRequest.getHeader(StubRequest.AUTH_HEADER)).thenReturn("Basic ");
      when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
      when(mockStubResponse.getStubResponseType()).thenReturn(StubResponseTypes.UNAUTHORIZED);

      final StubRequest assertionStubRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);
      when(mockDataStore.findStubResponseFor(assertionStubRequest)).thenReturn(mockStubResponse);
      //when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);


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
      when(mockStubResponse.getResponseBody()).thenReturn(someResultsMessage);
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);

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
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);
      when(mockStubResponse.getResponseBody()).thenReturn(someResultsMessage);

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
      when(mockDataStore.findStubResponseFor(Mockito.any(StubRequest.class))).thenReturn(mockStubResponse);

      final StubsHandler stubsHandler = new StubsHandler(mockDataStore);
      stubsHandler.handle(requestPathInfo, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);

      verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
      verify(mockPrintWriter, never()).println(someResultsMessage);
   }
}
