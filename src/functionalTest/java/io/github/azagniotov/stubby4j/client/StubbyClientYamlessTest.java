/*
 * Copyright (c) 2012-2024 Alexander Zagniotov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.azagniotov.stubby4j.client;

import static com.google.common.truth.Truth.assertThat;

import io.github.azagniotov.stubby4j.utils.NetworkPortUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import java.io.InputStream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StubbyClientYamlessTest {

    private static final String ADDRESS_TO_BIND = "127.0.0.1";
    private static final int STUBS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int STUBS_TLS_PORT = NetworkPortUtils.findAvailableTcpPort();
    private static final int ADMIN_PORT = NetworkPortUtils.findAvailableTcpPort();

    private static final StubbyClient STUBBY_CLIENT = new StubbyClient();

    @BeforeClass
    public static void beforeClass() throws Exception {

        // For example, passing additional command line args. But, they are not needed for this specific test
        final String[] additionalFlags = new String[] {"--debug"};

        final InputStream resourceAsStream =
                StubbyClientYamlessTest.class.getResourceAsStream("/yaml/standalone-stub.yaml");
        final String stubsYamlConfigurationData = StringUtils.inputStreamToString(resourceAsStream);

        STUBBY_CLIENT.startJettyYamless(
                stubsYamlConfigurationData, STUBS_PORT, STUBS_TLS_PORT, ADMIN_PORT, ADDRESS_TO_BIND, additionalFlags);
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
