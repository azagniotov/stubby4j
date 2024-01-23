package io.github.azagniotov.stubby4j.server.ssl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

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
