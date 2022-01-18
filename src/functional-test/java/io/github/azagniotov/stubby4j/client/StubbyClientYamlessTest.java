package io.github.azagniotov.stubby4j.client;

import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static com.google.common.truth.Truth.assertThat;


public class StubbyClientYamlessTest {

    private static final String ADDRESS_TO_BIND = "127.0.0.1";
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_TLS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();

    @BeforeClass
    public static void beforeClass() throws Exception {

        // For example, passing additional command line args. But, they are not needed for this specific test
        final String[] additionalFlags = new String[]{"--debug"};

        final InputStream resourceAsStream = StubbyClientYamlessTest.class.getResourceAsStream("/yaml/standalone-stub.yaml");
        final String stubsYamlConfigurationData = StringUtils.inputStreamToString(resourceAsStream);

        STUBBY_CLIENT.startJettyYamless(stubsYamlConfigurationData,
                STUBS_PORT,
                STUBS_TLS_PORT,
                ADMIN_PORT,
                ADDRESS_TO_BIND,
                additionalFlags);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        STUBBY_CLIENT.stopJetty();
    }

    @Test
    public void shouldStartStubby4jUsingStubbyClientByCallingYamlessAPI() throws Exception {
        final String uri = "/standalone/stub/uri";
        final StubbyResponse stubbyResponse = STUBBY_CLIENT.doGetOverSsl(ADDRESS_TO_BIND, uri, STUBS_TLS_PORT);

        assertThat(stubbyResponse.body()).isEqualTo("This is working!");
        assertThat(stubbyResponse.statusCode()).isEqualTo(HttpStatus.OK_200);
    }
}
