package io.github.azagniotov.stubby4j.server.ssl;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.alpn.openjdk8.server.OpenJDK8ServerALPNProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
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

    public static final KeyStore SELF_SIGNED_CERTIFICATE_TRUST_STORE;
    public static final SSLSocketFactory SSL_SOCKET_FACTORY;

    private static final String SELF_SIGNED_CERTIFICATE_VERSION = "v3";
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
    private static final SSLContext DEFAULT_SSL_CONTEXT;
    private static final SSLEngine DEFAULT_SSL_ENGINE;
    private static final Set<String> SUPPORT_CIPHERS;

    static {

        String overrideDisabledAlgorithms = System.getProperty("overrideDisabledAlgorithms");
        if (overrideDisabledAlgorithms != null && overrideDisabledAlgorithms.equalsIgnoreCase("true")) {
            final String overrideRequest = "Removing SSLv3, TLSv1 and TLSv1.1 from the JDK's 'jdk.tls.disabledAlgorithms' property..";
            ANSITerminal.warn(overrideRequest);
            LOGGER.warn(overrideRequest);

            // https://stackoverflow.com/questions/52115699/relaxing-ssl-algorithm-constrains-programmatically
            // Removed SSLv3, TLSv1 and TLSv1.1
            removeFromSecurityProperty("jdk.tls.disabledAlgorithms", "SSLv3", "TLSv1", "TLSv1.1");

        }
        SELF_SIGNED_CERTIFICATE_TRUST_STORE = loadStubby4jSelfSignedTrustStore();
        DEFAULT_ENABLED_TLS_VERSIONS = new HashSet<>(Arrays.asList(SSLv3, TLS_v1_0, TLS_v1_1, TLS_v1_2));
        TLS_v1_3_JDK_SUPPORTED = isTLSv13SupportedByCurrentJDK();
        ALL_ENABLED_TLS_VERSIONS = narrowDownEnabledProtocols();
        DEFAULT_SSL_CONTEXT = initAndSetDefaultSSLContext();
        DEFAULT_SSL_ENGINE = DEFAULT_SSL_CONTEXT.createSSLEngine();
        SSL_SOCKET_FACTORY = DEFAULT_SSL_CONTEXT.getSocketFactory();
        DEFAULT_SSL_ENGINE.setEnabledProtocols(enabledProtocols());
        SUPPORT_CIPHERS = supportedCiphers();
    }

    private SslUtils() {

    }

    public static void initStatic() {
        // init static { ... }
    }

    public static void sanityCheckOpenJDK8ServerALPNProcessor() {
        final OpenJDK8ServerALPNProcessor openJDK8ServerALPNProcessor = new OpenJDK8ServerALPNProcessor();
        openJDK8ServerALPNProcessor.appliesTo(DEFAULT_SSL_ENGINE);
        openJDK8ServerALPNProcessor.init();
    }

    private static boolean isTLSv13SupportedByCurrentJDK() {
        try {
            final SSLContext tlsContext = SSLContext.getInstance(TLS);
            tlsContext.init(null, new TrustManager[0], null);
            return isContainsTLSv13(tlsContext.getSupportedSSLParameters().getProtocols());
        } catch (Throwable cause) {
            LOGGER.debug("Unable to detect if JDK SSLEngine supports TLSv1.3, assuming no", cause);
            ANSITerminal.warn("Unable to detect if JDK SSLEngine supports TLSv1.3, assuming no");
            return false;
        }
    }

    private static String getTlsVersion() {
        return TLS_v1_3_JDK_SUPPORTED ? TLS_v1_3 : TLS_v1_2;
    }

    private static SSLContext initAndSetDefaultSSLContext() {
        try {
            final SSLContext defaultCandidate = SSLContext.getInstance(getTlsVersion());
            // Setting DefaultExtendedX509TrustManager as we need to ensure that TLS
            // requests made using stubby4j's self-signed certificate are trusted.
            //
            // If the trust manager is not set here, then the self-signed certificate must be added
            // to the client trust store when creating the client's SSL Context during TLS connection config.
            //
            // Basically, some how somewhere someone needs to be able to validate self-signed certificate.
            // https://github.com/azagniotov/stubby4j#client-side-tls-configuration
            defaultCandidate.init(null, new TrustManager[]{new DefaultExtendedX509TrustManager()}, null);
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
        return SUPPORT_CIPHERS.toArray(new String[0]);
    }

    public static String[] enabledProtocols() {
        return ALL_ENABLED_TLS_VERSIONS.toArray(new String[0]);
    }

    private static KeyStore loadStubby4jSelfSignedTrustStore() {
        try {
            //
            // 1. Download and save the remote self-signed certificate from the stubby4j server with TLS at localhost:7443
            //    This opens an SSL connection to the specified hostname and port and prints the SSL certificate.
            // ---------------------------------------------------------------------------------
            // $ echo quit | openssl s_client -showcerts -servername localhost -connect "localhost":7443 > FILE_NAME.pem
            //
            //
            // 2. Optionally, you can perform verification using cURL. Note: the -k (or --insecure) option is NOT used
            // ---------------------------------------------------------------------------------
            // $ curl -X GET --cacert FILE_NAME.pem  --tls-max 1.1  https://localhost:7443/hello -v
            //
            //
            // 3. Finally, load the saved self-signed certificate to a keystore
            // ---------------------------------------------------------------------------------
            // $ keytool -import -trustcacerts -alias stubby4j -file FILE_NAME.pem -keystore FILE_NAME.jks
            //
            //
            // 4. Load the generated FILE_NAME.jks file into the trust store of your client by creating a KeyStore
            // ---------------------------------------------------------------------------------
            final String selfSignedTrustStorePath = getSelfSignedTrustStorePath();
            final InputStream inputStream = SslUtils.class.getResourceAsStream(selfSignedTrustStorePath);
            if (inputStream == null) {
                throw new IllegalStateException(String.format("Could not get resource %s as stream", selfSignedTrustStorePath));
            }
            final KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(inputStream, "stubby4j".toCharArray()); // this is the password entered during the 'keytool -import ... ' command

            return trustStore;
        } catch (Exception e) {
            throw new Error("Could not load stubby4j self-signed certificate", e);
        }
    }

    public static String getSelfSignedTrustStorePath() {
        return String.format("/ssl/openssl.downloaded.stubby4j.self.signed.%s.pkcs12", SELF_SIGNED_CERTIFICATE_VERSION);
    }

    public static String getSelfSignedKeyStorePath() {
        return String.format("/ssl/stubby4j.self.signed.%s.pkcs12", SELF_SIGNED_CERTIFICATE_VERSION);
    }

    /**
     * Return an unmodifiable Set with all trusted X509Certificates contained
     * in the specified KeyStore.
     * <p>
     * From sun.security.validator.TrustStoreUtil
     */
    public static Set<X509Certificate> keyStoreAsX509Certificates() {
        final KeyStore keyStore = SELF_SIGNED_CERTIFICATE_TRUST_STORE;
        Set<X509Certificate> set = new HashSet<>();
        try {
            for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements(); ) {
                String alias = e.nextElement();
                if (keyStore.isCertificateEntry(alias)) {
                    Certificate cert = keyStore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        set.add((X509Certificate) cert);
                    }
                } else if (keyStore.isKeyEntry(alias)) {
                    Certificate[] certs = keyStore.getCertificateChain(alias);
                    if ((certs != null) && (certs.length > 0) && (certs[0] instanceof X509Certificate)) {
                        set.add((X509Certificate) certs[0]);
                    }
                }
            }
        } catch (KeyStoreException e) {
            // ignore
            //
            // This should be rare, but better to log this in the future.
        }

        return Collections.unmodifiableSet(set);
    }

    private static Set<String> supportedCiphers() {
        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = DEFAULT_SSL_ENGINE.getSupportedCipherSuites();
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
                    DEFAULT_SSL_ENGINE.setEnabledCipherSuites(new String[]{tlsPrefixedCipherName});
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

    private static void removeFromSecurityProperty(final String propertyName, final String... protocols) {
        final String propertyValue = Security.getProperty(propertyName);
        final String sanitizedValue = StringUtils.removeValueFromCsv(propertyValue, protocols);
        Security.setProperty(propertyName, sanitizedValue);
    }
}
