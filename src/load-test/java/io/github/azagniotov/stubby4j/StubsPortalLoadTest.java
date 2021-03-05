package io.github.azagniotov.stubby4j;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.cli.CommandLineInterpreter;
import io.github.azagniotov.stubby4j.server.JettyFactory;
import io.github.azagniotov.stubby4j.server.StubbyManager;
import io.github.azagniotov.stubby4j.server.StubbyManagerFactory;
import io.github.azagniotov.stubby4j.yaml.YamlBuilder;
import io.github.azagniotov.stubby4j.yaml.YamlParseResultSet;
import io.github.azagniotov.stubby4j.yaml.YamlParser;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.common.Common.HEADER_APPLICATION_JSON;

public class StubsPortalLoadTest {

    private static final int STUBS_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int STUBS_SSL_PORT = PortTestUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = PortTestUtils.findAvailableTcpPort();

    private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);

    private static StubbyManager stubbyManager;

    @BeforeClass
    public static void beforeClass() throws Exception {

        ANSITerminal.muteConsole(true);

        final URL url = StubsPortalLoadTest.class.getResource("/yaml/25k_stubs_load_test.yaml");
        final InputStream stubsDataInputStream = url.openStream();
        stubsDataInputStream.close();

        final String[] args = new String[]{
                "-m",
                "-l", JettyFactory.DEFAULT_HOST,
                "-s", String.valueOf(STUBS_PORT),
                "-a", String.valueOf(ADMIN_PORT),
                "-t", String.valueOf(STUBS_SSL_PORT),
                //"--disable_stub_caching",
        };

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(args);

        final File configFile = new File(url.getFile());
        final CompletableFuture<YamlParseResultSet> stubLoadComputation = CompletableFuture.supplyAsync(() -> {
            try {
                return new YamlParser().parse(configFile.getParent(), configFile);
            } catch (IOException ioEx) {
                throw new UncheckedIOException(ioEx);
            }
        });

        stubbyManager = new StubbyManagerFactory().construct(configFile, commandLineInterpreter.getCommandlineParams(), stubLoadComputation);
        stubbyManager.startJetty();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        stubbyManager.stopJetty();
    }

    @Test
    public void dataGenerator() throws Exception {

        final StringBuilder builder = new StringBuilder();
        for (int idx = 1; idx <= 25000; idx++) {

            final String yamlToUpdate = new YamlBuilder()
                    .newStubbedRequest()
                    .withUrl("/azagniotov/load/test/uri/" + idx)
                    .withMethodPost()
                    .withHeaderContentType("application/json")
                    .withFoldedPost("{\"request_id\":\"abc_" + idx + "\", \"payload\":\"(.*)\"}")
                    .newStubbedResponse()
                    .withHeaderContentType("application/json")
                    .withFoldedBody("{\"status\":\"CREATED RESOURCE#" + idx + "!\"}")
                    .withStatus("201")
                    .build();

            builder.append(yamlToUpdate).append("\n\n\n");

        }

        try (final FileWriter fileWriter = new FileWriter(new File("./load_test_data.yaml"));
             final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(builder.toString());
            bufferedWriter.flush();
        }
    }

    @Test
    public void cacheTesting_CheckResponseTimeOnRepeatedRequests() throws Exception {

        // Set or unset flag: --disable_stub_caching in beforeClass()

        final int idx = 25_000;

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
        // Making repeated requess
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final int numberOfRepeatedRequests = 10;
        for (int index = 2; index <= numberOfRepeatedRequests; index++) {
            final HttpRequest requestTwo = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, content);
            requestTwo.setHeaders(httpHeaders);

            final long secondStart = System.currentTimeMillis();
            final HttpResponse responseTwo = requestTwo.execute();
            final long secondElapsed = System.currentTimeMillis() - secondStart;

            final String responseTwoContentAsString = responseTwo.parseAsString().trim();

            assertThat(responseTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
            assertThat("{\"status\":\"CREATED RESOURCE#" + idx + "!\"}").isEqualTo(responseTwoContentAsString);

            System.out.println("**************************************************************");
            System.out.println(String.format("It took %s milliseconds to make a %s request", secondElapsed, index));
            System.out.println("**************************************************************");
        }

        System.out.println("\n\n");
    }
}
