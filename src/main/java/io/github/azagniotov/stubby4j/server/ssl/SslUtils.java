package io.github.azagniotov.stubby4j.server.ssl;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

/**
 * Some of code in the current file has been adopted from the Netty project:
 * <p>
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/SslUtils.java
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/JdkSslContext.java
 */
@GeneratedCodeCoverageExclusion
public final class SslUtils {

    public static final String SSLv3 = "SSLv3";
    public static final String TLS_v1_0 = "TLSv1";
    public static final String TLS_v1_1 = "TLSv1.1";
    public static final String TLS_v1_2 = "TLSv1.2";
    public static final String TLS_v1_3 = "TLSv1.3";

    private static final String TLS = "TLS";
    private static final Logger LOGGER = LoggerFactory.getLogger(SslUtils.class);

    // See https://tools.ietf.org/html/rfc8446#appendix-B.4
    private static final Set<String> TLS_v13_CIPHERS = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList(
                    "TLS_AES_256_GCM_SHA384",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_AES_128_GCM_SHA256",
                    "TLS_AES_128_CCM_8_SHA256",
                    "TLS_AES_128_CCM_SHA256")));

    private static final Set<String> DEFAULT_ENABLED_TLS_VERSIONS;
    private static final boolean TLS_v1_3_JDK_SUPPORTED;
    private static final Set<String> ALL_ENABLED_TLS_VERSIONS;

    private static Set<String> supportedCiphers;

    static {

        String overrideDisabledAlgorithms = System.getProperty("overrideDisabledAlgorithms");
        if (overrideDisabledAlgorithms != null && overrideDisabledAlgorithms.equalsIgnoreCase("true")) {
            final String overrideRequest = "Removing SSLv3, TLSv1 and TLSv1.1 from the JDK's 'jdk.tls.disabledAlgorithms' property..";
            ANSITerminal.warn(overrideRequest);
            LOGGER.warn(overrideRequest);

            // https://stackoverflow.com/questions/52115699/relaxing-ssl-algorithm-constrains-programmatically
            // Removed SSLv3, TLSv1 and TLSv1.1
            Security.setProperty("jdk.tls.disabledAlgorithms", "RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
            Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, MD5, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224");
        }

        DEFAULT_ENABLED_TLS_VERSIONS = new HashSet<>(Arrays.asList(SSLv3, TLS_v1_0, TLS_v1_1, TLS_v1_2));
        TLS_v1_3_JDK_SUPPORTED = isTLSv13SupportedByCurrentJDK();
        ALL_ENABLED_TLS_VERSIONS = narrowDownEnabledProtocols();
    }

    private SslUtils() {

    }

    private static boolean isTLSv13SupportedByCurrentJDK() {
        try {
            final SSLContext sslContext = initTlsSSLContext();
            return isContainsTLSv13(sslContext.getSupportedSSLParameters().getProtocols());
        } catch (Throwable cause) {
            LOGGER.debug("Unable to detect if JDK SSLEngine supports TLSv1.3, assuming no", cause);
            ANSITerminal.warn("Unable to detect if JDK SSLEngine supports TLSv1.3, assuming no");
            return false;
        }
    }

    private static SSLContext initTlsSSLContext() {
        try {
            final SSLContext defaultCandidate = SSLContext.getInstance(TLS);
            defaultCandidate.init(null, new TrustManager[0], null);
            return defaultCandidate;
        } catch (Exception e) {
            throw new Error("failed to initialize the TLS flavor SSL context", e);
        }
    }

    private static String getTlsVersion() {
        return TLS_v1_3_JDK_SUPPORTED ? TLS_v1_3 : TLS_v1_2;
    }

    public static void configureSslEnvironment() {
        final SSLContext defaultSslContext = initAndSetDefaultSSLContext();
        final SSLEngine defaultSslEngine = defaultSslContext.createSSLEngine();
        defaultSslEngine.setEnabledProtocols(enabledProtocols());

        supportedCiphers = supportedCiphers(defaultSslEngine);
    }

    private static SSLContext initAndSetDefaultSSLContext() {
        try {
            final SSLContext defaultCandidate = SSLContext.getInstance(getTlsVersion());
            defaultCandidate.init(new KeyManager[]{}, new TrustManager[]{new FakeX509TrustManager()}, null);
            SSLContext.setDefault(defaultCandidate);

            return defaultCandidate;
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
    }

    private static Set<String> narrowDownEnabledProtocols() {
        return TLS_v1_3_JDK_SUPPORTED ?
                concat(new HashSet<>(singletonList(TLS_v1_3)).stream(), DEFAULT_ENABLED_TLS_VERSIONS.stream())
                        .collect(toSet()) : DEFAULT_ENABLED_TLS_VERSIONS;
    }

    public static String[] includedCipherSuites() {
        return supportedCiphers.toArray(new String[0]);
    }

    public static String[] enabledProtocols() {
        return ALL_ENABLED_TLS_VERSIONS.toArray(new String[0]);
    }

    private static Set<String> supportedCiphers(final SSLEngine sslEngine) {
        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = sslEngine.getSupportedCipherSuites();
        Set<String> supportedCiphersSet = new LinkedHashSet<>(supportedCiphers.length);
        for (String supportedCipher : supportedCiphers) {
            supportedCiphersSet.add(supportedCipher);
            // IBM's J9 JVM utilizes a custom naming scheme for ciphers and only returns ciphers with the "SSL_"
            // prefix instead of the "TLS_" prefix (as defined in the JSSE cipher suite names [1]). According to IBM's
            // documentation [2] the "SSL_" prefix is "interchangeable" with the "TLS_" prefix.
            // See the IBM forum discussion [3] and issue on IBM's JVM [4] for more details.
            //[1] https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#ciphersuites
            //[2] https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.security.component.80.doc/
            // security-component/jsse2Docs/ciphersuites.html
            //[3] https://www.ibm.com/developerworks/community/forums/html/topic?id=9b5a56a9-fa46-4031-b33b-df91e28d77c2
            //[4] https://www.ibm.com/developerworks/rfe/execute?use_case=viewRfe&CR_ID=71770
            if (supportedCipher.startsWith("SSL_")) {
                final String tlsPrefixedCipherName = "TLS_" + supportedCipher.substring("SSL_".length());
                try {
                    sslEngine.setEnabledCipherSuites(new String[]{tlsPrefixedCipherName});
                    supportedCiphersSet.add(tlsPrefixedCipherName);
                } catch (IllegalArgumentException ignored) {
                    // The cipher is not supported ... move on to the next cipher.
                }
            }
        }

        return TLS_v1_3_JDK_SUPPORTED ?
                concat(TLS_v13_CIPHERS.stream(), supportedCiphersSet.stream()).collect(toSet()) :
                supportedCiphersSet;
    }

    private static boolean isContainsTLSv13(final String[] values) {
        for (String v : values) {
            if (TLS_v1_3.equals(v)) {
                return true;
            }
        }
        return false;
    }
}
