package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
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
    private HttpServletRequest httpServletRequestMock;
    private HttpServletResponse httpServletResponseMock;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(false);
    }

    @Before
    public void beforeEach() throws Exception {
        consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        httpServletRequestMock = mock(HttpServletRequest.class);
        httpServletResponseMock = mock(HttpServletResponse.class);

        when(httpServletRequestMock.getRequestURI()).thenReturn(URI);
    }

    @After
    public void afterEach() throws Exception {
        System.setOut(System.out);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_500() throws Exception {

        final int expectedStatus = 500;
        final String expectedConsoleOutput = String.format("%s [%s] Server Error\u001B[0m", expectedStatus, URI);
        final String expectedConsoleColor = "[31m";

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_301() throws Exception {

        final int expectedStatus = 301;
        final String expectedConsoleOutput = String.format("%s [%s] Moved Permanently\u001B[0m", expectedStatus, URI);
        final String expectedConsoleColor = "[33m";

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_201() throws Exception {

        final int expectedStatus = 201;
        final String expectedConsoleOutput = String.format("%s [%s] Created\u001B[0m", expectedStatus, URI);
        final String expectedConsoleColor = "[32m";

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_200() throws Exception {

        final int expectedStatus = 200;
        final String expectedConsoleOutput = String.format("%s [%s] OK\u001B[0m", expectedStatus, URI);
        final String expectedConsoleColor = "[32m";

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_100() throws Exception {

        final int expectedStatus = 100;
        final String expectedConsoleOutput = String.format("%s [%s] Continue\u001B[0m", expectedStatus, URI);
        final String expectedConsoleColor = "[34m";

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_LessThan100() throws Exception {

        final int expectedStatus = 99;
        final String expectedConsoleOutput = String.format("%s [%s] 99\u001B[0m", expectedStatus, URI);

        when(httpServletResponseMock.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(httpServletRequestMock.getRequestURI(), httpServletResponseMock);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
    }
}
