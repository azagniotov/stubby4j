package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.client.StubbyClient;
import io.github.azagniotov.stubby4j.client.StubbyResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;

public class StubsPortalLoadTest {

    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
    private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);
    private static final String STUBS_SSL_URL = String.format("https://localhost:%s", STUBS_SSL_PORT);
    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
    private static String stubsData;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalLoadTest.class.getResource("/yaml/10k_stubs_load_test.yaml");
        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());

        final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
        assertThat(adminPortalResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    @After
    public void afterEach() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void cacheTesting_CheckResponseTimeOnRepeatedRequests() throws Exception {

        final int idx = 10_000;

        final String content = "{\"request_id\":\"abc_" + idx + "\", \"payload\":\"Yo, this is big!!\"}";
        final String requestUrl = String.format("%s%s", STUBS_URL, "/azagniotov/load/test/uri/" + idx);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(HEADER_APPLICATION_JSON);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#1
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HttpRequest requestOne = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        requestOne.setHeaders(httpHeaders);

        final long firstStart = System.currentTimeMillis();
        final HttpResponse responseOne = requestOne.execute();
        final long firstElapsed = System.currentTimeMillis() - firstStart;

        final String responseOneContentAsString = responseOne.parseAsString().trim();

        assertThat(responseOne.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"status\":\"CREATED RESOURCE#" + idx + "!\"}").isEqualTo(responseOneContentAsString);

        System.out.println("\n\n**************************************************************");
        System.out.println(String.format("It took %s milliseconds to make a 1st request", firstElapsed));
        System.out.println("**************************************************************\n");

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#2
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        requestTwo.setHeaders(httpHeaders);

        final long secondStart = System.currentTimeMillis();
        final HttpResponse responseTwo = requestTwo.execute();
        final long secondElapsed = System.currentTimeMillis() - secondStart;

        final String responseTwoContentAsString = responseTwo.parseAsString().trim();

        assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"status\":\"CREATED RESOURCE#" + idx + "!\"}").isEqualTo(responseTwoContentAsString);

        System.out.println("\n**************************************************************");
        System.out.println(String.format("It took %s milliseconds to make a 2nd request", secondElapsed));
        System.out.println("**************************************************************");

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Making request#3
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HttpRequest requestThree = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
        requestThree.setHeaders(httpHeaders);

        final long thirdStart = System.currentTimeMillis();
        final HttpResponse responseThree = requestThree.execute();
        final long thirdElapsed = System.currentTimeMillis() - thirdStart;

        final String responseThreeContentAsString = responseThree.parseAsString().trim();

        assertThat(responseThree.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
        assertThat("{\"status\":\"CREATED RESOURCE#" + idx + "!\"}").isEqualTo(responseThreeContentAsString);

        System.out.println("**************************************************************");
        System.out.println(String.format("It took %s milliseconds to make a 3rd request", thirdElapsed));
        System.out.println("**************************************************************\n\n");
    }
}
