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

package io.github.azagniotov.stubby4j.server.ssl;

import static com.google.common.truth.Truth.assertThat;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class SslUtilsTest {

    private static X509Certificate stubbySelfSignedCert;
    private static CustomHostnameVerifier customHostnameVerifier;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        stubbySelfSignedCert = new ArrayList<>(selfSignedX509).get(0);
        customHostnameVerifier = new CustomHostnameVerifier(stubbySelfSignedCert);
    }

    @Test
    public void certificateShouldBeVersion3() throws Exception {
        assertThat(stubbySelfSignedCert.getVersion()).isEqualTo(3);
    }

    @Test
    public void certificateSigAlgNameShouldBeSHA256withRSA() throws Exception {
        assertThat(stubbySelfSignedCert.getSigAlgName()).isEqualTo("SHA256withRSA");
    }

    @Test
    public void certificateShouldContainExpectedSubjectAltNames() throws Exception {
        final Set<String> subjectAltNames = customHostnameVerifier.getSubjectAltNames(2);

        assertThat(subjectAltNames.size()).isEqualTo(2);
        assertThat(subjectAltNames.contains("localhost")).isTrue();
        assertThat(subjectAltNames.contains("::1")).isTrue();
    }

    @Test
    public void certificateShouldContainExpectedSubjectAltIps() throws Exception {
        final Set<String> subjectAltNameIps = customHostnameVerifier.getSubjectAltNames(7);

        assertThat(subjectAltNameIps.size()).isEqualTo(23);
        assertThat(subjectAltNameIps.contains("0.0.0.0")).isTrue();
        assertThat(subjectAltNameIps.contains("127.0.0.1")).isTrue();

        final int totalIpsPerClass = 7;
        for (int idx = 1; idx <= totalIpsPerClass; idx++) {
            assertThat(subjectAltNameIps.contains("10.0.0." + idx)).isTrue();
            assertThat(subjectAltNameIps.contains("192.168.0." + idx)).isTrue();
            assertThat(subjectAltNameIps.contains("172.16.0." + idx)).isTrue();
        }
    }
}
