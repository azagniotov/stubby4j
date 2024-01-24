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

public class CustomHostnameVerifierTest {

    private static CustomHostnameVerifier customHostnameVerifier;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);
        customHostnameVerifier = new CustomHostnameVerifier(x509);
    }

    @Test
    public void subjectAltNamesMustContainLocalhost() throws Exception {
        final Set<String> subjectAltNames = customHostnameVerifier.getSubjectAltNames(2);

        assertThat(subjectAltNames.contains("localhost")).isTrue();
    }

    @Test
    public void subjectAltNamesMustContain127_0_0_1() throws Exception {
        final Set<String> subjectAltNames = customHostnameVerifier.getSubjectAltNames(7);

        assertThat(subjectAltNames.contains("127.0.0.1")).isTrue();
    }

    @Test
    public void expectedSubjectAltNames() throws Exception {
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("localhost")).isTrue();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("0.0.0.0")).isTrue();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("127.0.0.1")).isTrue();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("::1")).isTrue();
    }

    @Test
    public void nonSubjectAltNames() throws Exception {
        assertThat(customHostnameVerifier.isSubjectAltNamesContain(null)).isFalse();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("")).isFalse();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("null")).isFalse();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("203.0.113.42"))
                .isFalse();
        assertThat(customHostnameVerifier.isSubjectAltNamesContain("2001:0002:14:5:1:2:bf35:2610"))
                .isFalse();
    }

    @Test
    public void stubbySelfSignedCertificateShouldNotHaveX500PrincipalNameLocalhost() throws Exception {
        // stubby4j self-signed certificate does not have 'localhost' string in its x500 PrincipalName
        final boolean isX500PrincipalName = customHostnameVerifier.isX500PrincipalNameLocalhost();

        assertThat(isX500PrincipalName).isFalse();
    }

    @Test
    public void stubbySelfSignedCertificateShouldContainPrivateIp() throws Exception {
        // stubby4j self-signed certificate does not have 'localhost' string in its x500 PrincipalName
        final boolean isX500PrincipalName = customHostnameVerifier.isSubjectAltNamesContainPrivateIp();

        assertThat(isX500PrincipalName).isTrue();
    }
}
