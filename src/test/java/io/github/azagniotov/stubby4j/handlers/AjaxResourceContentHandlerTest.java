package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubRepository;
import io.github.azagniotov.stubby4j.stubs.StubTypes;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AjaxResourceContentHandlerTest {

    private static final Optional<StubHttpLifecycle> STUB_HTTP_LIFECYCLE_OPTIONAL = Optional.of(new StubHttpLifecycle.Builder().build());
    private static final StubProxyConfig STUB_PROXY_CONFIG = new StubProxyConfig.Builder().withPropertyEndpoint("http://google.com").build();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PrintWriter mockPrintWriter;

    @Mock
    private StubRepository mockStubRepository;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Mock
    private Request mockBaseRequest;

    @Captor
    private ArgumentCaptor<String> fieldCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<Integer> responseSequenceCaptor;

    @Captor
    private ArgumentCaptor<Integer> stubIndexCaptor;

    @Captor
    private ArgumentCaptor<StubTypes> stubTypeCaptor;

    private AjaxResourceContentHandler spyAjaxResourceContentHandler = new AjaxResourceContentHandler(mockStubRepository);

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Before
    public void beforeEach() throws Exception {
        when(mockHttpServletResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethod.GET.asString());

        spyAjaxResourceContentHandler = Mockito.spy(new AjaxResourceContentHandler(mockStubRepository));
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenBaseRequestHandled() throws Exception {
        when(mockBaseRequest.isHandled()).thenReturn(true);

        spyAjaxResourceContentHandler.handle("/some/uri", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockBaseRequest, never()).setHandled(eq(true));
        verify(mockHttpServletResponse, never()).setStatus(anyInt());

        verify(spyAjaxResourceContentHandler, never()).renderProxyConfigAjaxResponse(any(HttpServletResponse.class), anyString(), any(StubProxyConfig.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));
    }

    @Test
    public void shouldDetermineRequestAsHandledWhenResponseCommitted() throws Exception {
        when(mockBaseRequest.isHandled()).thenReturn(false);
        when(mockHttpServletResponse.isCommitted()).thenReturn(true);

        spyAjaxResourceContentHandler.handle("/some/uri", mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockBaseRequest, never()).setHandled(eq(true));
        verify(mockHttpServletResponse, never()).setStatus(anyInt());

        verify(spyAjaxResourceContentHandler, never()).renderProxyConfigAjaxResponse(any(HttpServletResponse.class), anyString(), any(StubProxyConfig.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchStubbedRequestContent() throws Exception {

        final String requestURI = "/ajax/resource/5/request/post";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchStubByIndex(anyInt())).thenReturn(STUB_HTTP_LIFECYCLE_OPTIONAL);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).throwErrorOnNonExistentResourceIndex(any(HttpServletResponse.class), stubIndexCaptor.capture());
        verify(spyAjaxResourceContentHandler).renderAjaxResponseContent(any(HttpServletResponse.class), stubTypeCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

        assertThat(stubIndexCaptor.getValue()).isEqualTo(5);
        assertThat(stubTypeCaptor.getValue()).isEqualTo(StubTypes.REQUEST);
        assertThat(fieldCaptor.getValue()).isEqualTo("post");
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchStubbedResponseContent() throws Exception {

        final String requestURI = "/ajax/resource/15/response/file";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchStubByIndex(anyInt())).thenReturn(STUB_HTTP_LIFECYCLE_OPTIONAL);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).throwErrorOnNonExistentResourceIndex(any(HttpServletResponse.class), stubIndexCaptor.capture());
        verify(spyAjaxResourceContentHandler).renderAjaxResponseContent(any(HttpServletResponse.class), stubTypeCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

        assertThat(stubIndexCaptor.getValue()).isEqualTo(15);
        assertThat(stubTypeCaptor.getValue()).isEqualTo(StubTypes.RESPONSE);
        assertThat(fieldCaptor.getValue()).isEqualTo("file");
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchStubbedSequencedResponseContent() throws Exception {

        final String requestURI = "/ajax/resource/15/response/8/file";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchStubByIndex(anyInt())).thenReturn(STUB_HTTP_LIFECYCLE_OPTIONAL);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).throwErrorOnNonExistentResourceIndex(any(HttpServletResponse.class), stubIndexCaptor.capture());
        verify(spyAjaxResourceContentHandler).renderAjaxResponseContent(any(HttpServletResponse.class), responseSequenceCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));

        assertThat(stubIndexCaptor.getValue()).isEqualTo(15);
        assertThat(responseSequenceCaptor.getValue()).isEqualTo(8);
        assertThat(fieldCaptor.getValue()).isEqualTo("file");
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchNonExistentStubbedSequencedResponseContent() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Resource does not exist for ID: 999");

        final String requestURI = "/ajax/resource/999/response/8/file";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchStubByIndex(999)).thenReturn(Optional.empty());

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).throwErrorOnNonExistentResourceIndex(any(HttpServletResponse.class), stubIndexCaptor.capture());
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchContentForWrongStubType() throws Exception {
        final String requestURI = "/ajax/resource/5/WRONG-STUB-TYPE/post";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchStubByIndex(anyInt())).thenReturn(STUB_HTTP_LIFECYCLE_OPTIONAL);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).throwErrorOnNonExistentResourceIndex(any(HttpServletResponse.class), anyInt());
        verify(mockPrintWriter).println("Could not fetch the content for stub type: WRONG-STUB-TYPE");

        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchStubbedProxyConfigContent() throws Exception {

        final String requestURI = "/ajax/resource/proxy-config/some-unique-name/proxyConfigAsYAML";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);
        when(mockStubRepository.matchProxyConfigByName(anyString())).thenReturn(STUB_PROXY_CONFIG);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(spyAjaxResourceContentHandler).renderProxyConfigAjaxResponse(any(HttpServletResponse.class), fieldCaptor.capture(), any(StubProxyConfig.class));
        verify(mockStubRepository).matchProxyConfigByName(stringCaptor.capture());

        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

        assertThat(fieldCaptor.getValue()).isEqualTo("proxyConfigAsYAML");
        assertThat(stringCaptor.getValue()).isEqualTo("some-unique-name");
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchNonExistentProxyConfigContent() throws Exception {
        final String requestURI = "/ajax/resource/proxy-config/some-unique-name/proxyConfigAsYAML";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);

        // No proxy config in repository for a given name
        when(mockStubRepository.matchProxyConfigByName(anyString())).thenReturn(null);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockStubRepository).matchProxyConfigByName(stringCaptor.capture());

        verify(spyAjaxResourceContentHandler, never()).renderProxyConfigAjaxResponse(any(HttpServletResponse.class), anyString(), any(StubProxyConfig.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

        assertThat(stringCaptor.getValue()).isEqualTo("some-unique-name");
    }

    @Test
    public void verifyBehaviourWhenAjaxSubmittedToFetchWrongProxyConfigContent() throws Exception {

        final String requestURI = "/ajax/resource/WRONG-proxy-config/some-unique-name/proxyConfigAsYAML";

        when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);

        spyAjaxResourceContentHandler.handle(requestURI, mockBaseRequest, mockHttpServletRequest, mockHttpServletResponse);

        verify(mockStubRepository, never()).matchProxyConfigByName(anyString());
        verify(spyAjaxResourceContentHandler, never()).renderProxyConfigAjaxResponse(any(HttpServletResponse.class), anyString(), any(StubProxyConfig.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), any(StubTypes.class), anyString(), any(StubHttpLifecycle.class));
        verify(spyAjaxResourceContentHandler, never()).renderAjaxResponseContent(any(HttpServletResponse.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

        verify(mockPrintWriter).println("Could not fetch the content for proxy config: WRONG-proxy-config");
    }
}
