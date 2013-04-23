package by.stub.utils;

import by.stub.cli.ANSITerminal;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: Alexander Zagniotov
 * Created: 4/22/13 3:33 PM
 */
public class ConsoleUtilsTest {

   private static final String URI = "/some/uri/to/resource/123";

   private ByteArrayOutputStream consoleCaptor;
   private HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
   private HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);

   @BeforeClass
   public static void beforeClass() throws Exception {
      ANSITerminal.muteConsole(false);
   }

   @Before
   public void beforeEach() throws Exception {
      consoleCaptor = new ByteArrayOutputStream();
      final boolean NO_AUTO_FLUSH = false;
      System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

      when(httpServletRequestMock.getRequestURI()).thenReturn(URI);
   }

   @After
   public void afterEach() throws Exception {
      System.setOut(System.out);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_500() throws Exception {

      final int expectedStatus = 500;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s Server Error\u001B[0m", expectedStatus, expectedHandlerName, URI);
      final String expectedConsoleColor = "[31m";

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
      assertThat(actualConsoleOutput).contains(expectedConsoleColor);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_301() throws Exception {

      final int expectedStatus = 301;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s Moved Permanently\u001B[0m", expectedStatus, expectedHandlerName, URI);
      final String expectedConsoleColor = "[33m";

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
      assertThat(actualConsoleOutput).contains(expectedConsoleColor);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_201() throws Exception {

      final int expectedStatus = 201;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s Created\u001B[0m", expectedStatus, expectedHandlerName, URI);
      final String expectedConsoleColor = "[32m";

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
      assertThat(actualConsoleOutput).contains(expectedConsoleColor);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_200() throws Exception {

      final int expectedStatus = 200;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s OK\u001B[0m", expectedStatus, expectedHandlerName, URI);
      final String expectedConsoleColor = "[32m";

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
      assertThat(actualConsoleOutput).contains(expectedConsoleColor);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_100() throws Exception {

      final int expectedStatus = 100;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s Continue\u001B[0m", expectedStatus, expectedHandlerName, URI);
      final String expectedConsoleColor = "[34m";

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
      assertThat(actualConsoleOutput).contains(expectedConsoleColor);
   }

   @Test
   public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_LessThan100() throws Exception {

      final int expectedStatus = 99;
      final String expectedHandlerName = "stubs";
      final String expectedConsoleOutput = String.format("%s [%s]%s 99\u001B[0m", expectedStatus, expectedHandlerName, URI);

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(httpServletResponseMock);
      wrapper.setStatus(expectedStatus);

      ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), wrapper, "stubs");
      final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

      assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
   }
}
