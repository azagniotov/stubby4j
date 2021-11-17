package io.github.azagniotov.stubby4j.server.ssl;

import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class CustomHostnameVerifierTest {

    @Test
    public void subjectAltNamesMustContainLocalhost() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final Set<String> subjectAltNames = CustomHostnameVerifier.getSubjectAltNames(x509, 2);

        assertThat(subjectAltNames.contains("localhost")).isTrue();
    }

    @Test
    public void subjectAltNamesMustContain127_0_0_1() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final Set<String> subjectAltNames = CustomHostnameVerifier.getSubjectAltNames(x509, 7);

        assertThat(subjectAltNames.contains("127.0.0.1")).isTrue();
    }

    @Test
    public void localhostMustBeSubjectAltName() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final boolean isSubjectAltName = CustomHostnameVerifier.isSubjectAltName("localhost", x509);

        assertThat(isSubjectAltName).isTrue();
    }

    @Test
    public void ip127_0_0_1MustBeSubjectAltName() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final boolean isSubjectAltName = CustomHostnameVerifier.isSubjectAltName("127.0.0.1", x509);

        assertThat(isSubjectAltName).isTrue();
    }

    @Test
    public void ipv6_colon_colon_one_MustBeSubjectAltName() throws Exception {
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final boolean isSubjectAltName = CustomHostnameVerifier.isSubjectAltName("::1", x509);

        assertThat(isSubjectAltName).isTrue();
    }

    @Test
    public void stubbySelfSignedCertificateShouldNotHaveX500PrincipalNameLocalhost() throws Exception {
        // stubby4j self-signed certificate does not have 'localhost' string in its x500 PrincipalName
        final Set<X509Certificate> selfSignedX509 = SslUtils.keyStoreAsX509Certificates();
        final X509Certificate x509 = new ArrayList<>(selfSignedX509).get(0);

        final boolean isX500PrincipalName = CustomHostnameVerifier.isX500PrincipalNameLocalhost(x509);

        assertThat(isX500PrincipalName).isFalse();
    }
}
