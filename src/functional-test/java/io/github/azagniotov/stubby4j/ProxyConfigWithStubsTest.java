package io.github.azagniotov.stubby4j;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;

public class ProxyConfigWithStubsTest {

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

        final URL url = ProxyConfigWithStubsTest.class.getResource("/yaml/main-test-stubs-with-proxy-config.yaml");
        final InputStream stubsDataInputStream = url.openStream();
        stubsData = StringUtils.inputStreamToString(stubsDataInputStream);
        stubsDataInputStream.close();

        STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    @Before
    public void beforeEach() throws Exception {
        final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
        assertThat(adminPortalResponse.statusCode()).isEqualTo(HttpStatus.CREATED_201);
    }

    @After
    public void afterEach() throws Exception {
        ANSITerminal.muteConsole(true);
    }

    @Test
    public void should_ReturnCompleteYAMLConfig_WhenSuccessfulGetMade_ToAdminPortalRoot() throws Exception {

        final String requestUrl = String.format("%s%s", ADMIN_URL, "/");
        final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

        final HttpResponse httpResponse = httpGetRequest.execute();
        final String responseContentAsString = httpResponse.parseAsString().trim();

        assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
        assertThat(responseContentAsString).contains(
                "- proxy-config:\n" +
                        "    proxy-config-description: this is a catch-all proxy config\n" +
                        "    proxy-strategy: as-is\n" +
                        "    proxy-properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com"
        );
    }
}
