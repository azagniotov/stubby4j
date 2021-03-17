package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.stubs.StubSearchResult;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.eclipse.jetty.server.Request;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("serial")
@RunWith(MockitoJUnitRunner.class)
public class StubsPortalHandlerTest {

    private static final String SOME_RESULTS_MESSAGE = "we have results";
    private static final ServletOutputStream SERVLET_OUTPUT_STREAM = new ServletOutputStream() {

        @Override
        public void write(final int i) throws IOException {

        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(final WriteListener writeListener) {

        }
    };

    @Mock
    private StubResponse mockStubResponse;

    @Mock
    private StubSearchResult mockStubSearchResult;

    @Mock
    private PrintWriter mockPrintWriter;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Mock
    private StubRepository mockStubRepository;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private Request mockBaseRequest;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenBaseRequestHandled() throws Exception {
        when(mockBaseRequest.isHandled()).thenReturn(true);

        final StubsPortalHandler stubsPortalHandler = new StubsPortalHandler(mockStubRepository);
        stubsPortalHandler.handle("/path/1", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockBaseRequest, never()).setHandled(eq(true));
        verify(mockStubRepository, never()).search(any(HttpServletRequest.class));
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenResponseCommitted() throws Exception {
        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(true);

        final StubsPortalHandler stubsPortalHandler = new StubsPortalHandler(mockStubRepository);
        stubsPortalHandler.handle("/path/1", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockBaseRequest, never()).setHandled(eq(true));
        verify(mockStubRepository, never()).search(any(HttpServletRequest.class));
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithNoResults() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.NOT_FOUND_404);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandlePostRequestWithNoResults() throws Exception {

        final String postData = "postData";
        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.POST.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.NOT_FOUND);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});

        final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
        when(mockHttpServletRequest.getInputStream()).thenReturn(getServletInputStream(inputStream));

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.NOT_FOUND_404);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandlePostRequestWithMissingPostData() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.POST.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
        verify(mockHttpServletResponse).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandlePostRequestWithEmptyPostData() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.POST.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);

        final InputStream inputStream = new ByteArrayInputStream("".getBytes());
        when(mockHttpServletRequest.getInputStream()).thenReturn(getServletInputStream(inputStream));

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.BAD_REQUEST_400);
        verify(mockHttpServletResponse).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithSomeResults() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(null);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithNoAuthorizationHeaderSet() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.UNAUTHORIZED);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse).sendError(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithEmptyBasicAuthorizationHeaderSet() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.UNAUTHORIZED);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse).sendError(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithEmptyBearerAuthorizationHeaderSet() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.UNAUTHORIZED);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse).sendError(HttpStatus.UNAUTHORIZED_401);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandlePostRequestWithMatch() throws Exception {
        final String postData = "postData";
        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.POST.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(null);
        final InputStream inputStream = new ByteArrayInputStream(postData.getBytes());
        when(mockHttpServletRequest.getInputStream()).thenReturn(getServletInputStream(inputStream));

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithLatency() throws Exception {

        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());
        when(mockHttpServletRequest.getPathInfo()).thenReturn(requestPathInfo);
        when(mockStubResponse.getLatency()).thenReturn("50");
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);
        when(mockStubResponse.getResponseBodyAsBytes()).thenReturn(new byte[]{});
        when(mockHttpServletResponse.getOutputStream()).thenReturn(SERVLET_OUTPUT_STREAM);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(mockHttpServletResponse).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void verifyBehaviourDuringHandleGetRequestWithInvalidLatency() throws Exception {
        final String method = HttpMethod.GET.asString();
        final String requestPathInfo = "/path/1";

        when(mockHttpServletRequest.getMethod()).thenReturn(method);
        when(mockStubResponse.getLatency()).thenReturn("43rl4knt3l");
        when(mockStubResponse.getHttpStatusCode()).thenReturn(Code.OK);

        setUpStubSearchMockExpectations(requestPathInfo);

        verify(mockHttpServletResponse).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        verify(mockHttpServletResponse, never()).setStatus(HttpStatus.OK_200);
        verify(mockPrintWriter, never()).println(SOME_RESULTS_MESSAGE);
    }

    private void setUpStubSearchMockExpectations(final String requestPathInfo) throws Exception {
        when(mockStubRepository.toStubRequest(mockHttpServletRequest)).thenCallRealMethod();
        final StubRequest assertionStubRequest = mockStubRepository.toStubRequest(mockHttpServletRequest);

        when(mockStubRepository.search(mockHttpServletRequest)).thenReturn(mockStubSearchResult);
        when(mockStubSearchResult.getInvariant()).thenReturn(assertionStubRequest);
        when(mockStubSearchResult.getMatch()).thenReturn(mockStubResponse);

        final StubsPortalHandler stubsPortalHandler = new StubsPortalHandler(mockStubRepository);
        stubsPortalHandler.handle(requestPathInfo, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);
    }

    private ServletInputStream getServletInputStream(final InputStream inputStream) {
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(final ReadListener readListener) {

            }
        };
    }
}
