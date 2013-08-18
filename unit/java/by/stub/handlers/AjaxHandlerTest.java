package by.stub.handlers;

import by.stub.cli.ANSITerminal;
import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author: Alexander Zagniotov
 * Created: 8/18/13 1:48 PM
 */
public class AjaxHandlerTest {

   private StubbedDataManager mockStubbedDataManager = Mockito.mock(StubbedDataManager.class);
   private Request mockRequest = Mockito.mock(Request.class);
   private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
   private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

   @BeforeClass
   public static void beforeClass() throws Exception {
      ANSITerminal.muteConsole(true);
   }

   @Before
   public void beforeEach() throws Exception {
      mockRequest = Mockito.mock(Request.class);
      mockStubbedDataManager = Mockito.mock(StubbedDataManager.class);
      mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
      mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
   }

   @Test
   public void verifyBehaviourWhenAjaxSubmittedForStubbedRequestContent() throws Exception {

      ArgumentCaptor<String> fieldCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> stubTypeCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Integer> httpCycleIndexCaptor = ArgumentCaptor.forClass(Integer.class);

      final String requestURI = "/ajax/resource/5/request/post";
      final AjaxHandler ajaxHandler = new AjaxHandler(mockStubbedDataManager);
      final AjaxHandler spyAjaxHandler = Mockito.spy(ajaxHandler);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);

      spyAjaxHandler.handle(requestURI, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(spyAjaxHandler).throwErrorOnNonexistentResourceIndex(any(HttpServletResponseWithGetStatus.class), httpCycleIndexCaptor.capture());
      verify(spyAjaxHandler, times(1)).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), stubTypeCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
      verify(spyAjaxHandler, never()).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

      assertThat(httpCycleIndexCaptor.getValue()).isEqualTo(5);
      assertThat(stubTypeCaptor.getValue()).isEqualTo("request");
      assertThat(fieldCaptor.getValue()).isEqualTo("post");
   }

   @Test
   public void verifyBehaviourWhenAjaxSubmittedForStubbedResponseContent() throws Exception {

      ArgumentCaptor<String> fieldCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> stubTypeCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Integer> httpCycleIndexCaptor = ArgumentCaptor.forClass(Integer.class);

      final String requestURI = "/ajax/resource/15/response/file";
      final AjaxHandler ajaxHandler = new AjaxHandler(mockStubbedDataManager);
      final AjaxHandler spyAjaxHandler = Mockito.spy(ajaxHandler);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);

      spyAjaxHandler.handle(requestURI, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(spyAjaxHandler).throwErrorOnNonexistentResourceIndex(any(HttpServletResponseWithGetStatus.class), httpCycleIndexCaptor.capture());
      verify(spyAjaxHandler, times(1)).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), stubTypeCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
      verify(spyAjaxHandler, never()).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), anyInt(), anyString(), any(StubHttpLifecycle.class));

      assertThat(httpCycleIndexCaptor.getValue()).isEqualTo(15);
      assertThat(stubTypeCaptor.getValue()).isEqualTo("response");
      assertThat(fieldCaptor.getValue()).isEqualTo("file");
   }

   @Test
   public void verifyBehaviourWhenAjaxSubmittedForStubbedSequencedResponseContent() throws Exception {

      ArgumentCaptor<String> fieldCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Integer> responseSequenceCaptor = ArgumentCaptor.forClass(Integer.class);
      ArgumentCaptor<Integer> httpCycleIndexCaptor = ArgumentCaptor.forClass(Integer.class);

      final String requestURI = "/ajax/resource/15/response/8/file";
      final AjaxHandler ajaxHandler = new AjaxHandler(mockStubbedDataManager);
      final AjaxHandler spyAjaxHandler = Mockito.spy(ajaxHandler);

      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getRequestURI()).thenReturn(requestURI);

      spyAjaxHandler.handle(requestURI, mockRequest, mockHttpServletRequest, mockHttpServletResponse);

      verify(spyAjaxHandler).throwErrorOnNonexistentResourceIndex(any(HttpServletResponseWithGetStatus.class), httpCycleIndexCaptor.capture());
      verify(spyAjaxHandler, times(1)).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), responseSequenceCaptor.capture(), fieldCaptor.capture(), any(StubHttpLifecycle.class));
      verify(spyAjaxHandler, never()).renderAjaxResponseContent(any(HttpServletResponseWithGetStatus.class), anyString(), anyString(), any(StubHttpLifecycle.class));

      assertThat(httpCycleIndexCaptor.getValue()).isEqualTo(15);
      assertThat(responseSequenceCaptor.getValue()).isEqualTo(8);
      assertThat(fieldCaptor.getValue()).isEqualTo("file");
   }
}
