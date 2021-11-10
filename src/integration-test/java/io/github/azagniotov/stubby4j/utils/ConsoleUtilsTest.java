package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import org.eclipse.jetty.http.HttpScheme;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsoleUtilsTest {

    private static final String URI = "/some/uri/to/resource/123";

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    private ByteArrayOutputStream consoleCaptor;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ANSITerminal.muteConsole(false);
    }

    @Before
    public void beforeEach() throws Exception {
        consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        when(mockHttpServletRequest.getRequestURI()).thenReturn(URI);
    }

    @After
    public void afterEach() throws Exception {
        System.setOut(System.out);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_500() throws Exception {

        final int expectedStatus = 500;
        final String expectedConsoleOutput = String.format("<= %s Server Error\n\u001B[0m", expectedStatus);
        final String expectedConsoleColor = "[31m";

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_301() throws Exception {

        final int expectedStatus = 301;
        final String expectedConsoleOutput = String.format("<= %s Moved Permanently\n\u001B[0m", expectedStatus);
        final String expectedConsoleColor = "[33m";

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_201() throws Exception {

        final int expectedStatus = 201;
        final String expectedConsoleOutput = String.format("<= %s Created\n\u001B[0m", expectedStatus);
        final String expectedConsoleColor = "[32m";

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_200() throws Exception {

        final int expectedStatus = 200;
        final String expectedConsoleOutput = String.format("<= %s OK\n\u001B[0m", expectedStatus);
        final String expectedConsoleColor = "[32m";

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_100() throws Exception {

        final int expectedStatus = 100;
        final String expectedConsoleOutput = String.format("<= %s Continue\n\u001B[0m", expectedStatus);
        final String expectedConsoleColor = "[34;1m";

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
        assertThat(actualConsoleOutput).contains(expectedConsoleColor);
    }

    @Test
    public void shouldPrintToConsoleExpectedErrorWithColor_WhenStatus_LessThan100() throws Exception {

        final int expectedStatus = 99;
        final String expectedConsoleOutput = String.format("<= %s 99\n\u001B[0m", expectedStatus);

        when(mockHttpServletResponse.getStatus()).thenReturn(expectedStatus);

        ConsoleUtils.logOutgoingResponse(mockHttpServletRequest.getRequestURI(), mockHttpServletResponse);
        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains(expectedConsoleOutput);
    }
}
