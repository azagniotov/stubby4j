package io.github.azagniotov.stubby4j.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Some of code in the current file has been adopted from the Netty project:
 * <p>
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/SslUtils.java
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/JdkSslContext.java
 */
public class SslUtils {

    public static final String TLS_v1 = "TLSv1";
    public static final String TLS_v1_1 = "TLSv1.1";
    public static final String TLS_v1_2 = "TLSv1.2";
    public static final String TLS_v1_3 = "TLSv1.3";
    static final SSLEngine SSL_ENGINE;
    private static final String[] ALL_TLS_VERSIONS = new String[]{TLS_v1, TLS_v1_1, TLS_v1_2, TLS_v1_3};

    // See https://tools.ietf.org/html/rfc8446#appendix-B.4
    private static final Set<String> TLSV13_CIPHERS = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList(
                    "TLS_AES_256_GCM_SHA384",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_AES_128_GCM_SHA256",
                    "TLS_AES_128_CCM_8_SHA256",
                    "TLS_AES_128_CCM_SHA256")));
    private static final SSLContext DEFAULT_SSL_CONTEXT;
    private static final Set<String> SUPPORTED_CIPHERS;

    static {

        // https://stackoverflow.com/questions/52115699/relaxing-ssl-algorithm-constrains-programmatically
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, RC4, DH keySize < 1024, EC keySize < 224, DES40_CBC, RC4_40, 3DES_EDE_CBC");
        Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224");

        try {
            DEFAULT_SSL_CONTEXT = SSLContext.getInstance(TLS_v1_3);
            DEFAULT_SSL_CONTEXT.init(null, null, null);
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }

        // Choose the sensible default list of protocols that respects JDK flags, eg. jdk.tls.client.protocols
        SSL_ENGINE = DEFAULT_SSL_CONTEXT.createSSLEngine();
        SSL_ENGINE.setEnabledProtocols(ALL_TLS_VERSIONS);

        System.out.println("SSLEngine [server] enabled protocols: ");
        System.out.println(new HashSet<>(Arrays.asList(SSL_ENGINE.getEnabledProtocols())));

        Set<String> supportedCiphers = supportedCiphers();
        SUPPORTED_CIPHERS = new LinkedHashSet<>(supportedCiphers);
        SUPPORTED_CIPHERS.addAll(TLSV13_CIPHERS);
    }

    public static String[] includedCipherSuites() {
        return SUPPORTED_CIPHERS.toArray(new String[]{});
    }

    private static Set<String> supportedCiphers() {
        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = SSL_ENGINE.getSupportedCipherSuites();
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
                    SSL_ENGINE.setEnabledCipherSuites(new String[]{tlsPrefixedCipherName});
                    supportedCiphersSet.add(tlsPrefixedCipherName);
                } catch (IllegalArgumentException ignored) {
                    // The cipher is not supported ... move on to the next cipher.
                }
            }
        }
        return supportedCiphersSet;
    }
}
