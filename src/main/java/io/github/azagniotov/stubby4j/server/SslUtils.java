package io.github.azagniotov.stubby4j.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/*
 * The code in the current file has been adopted from the Netty project:
 *
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/SslUtils.java
 * https://github.com/netty/netty/blob/ede7a604f185cd716032ecbb356b6ea5130f7d0d/handler/src/main/java/io/netty/handler/ssl/JdkSslContext.java
 *
 * I am keeping their original copyright notice here:
 *
 *
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
public class SslUtils {

    public static final String TLS_v1 = "TLSv1";
    public static final String TLS_v1_1 = "TLSv1.1";
    public static final String TLS_v1_2 = "TLSv1.2";
    public static final String TLS_v1_3 = "TLSv1.3";
    public static final String[] ALL_TLS_VERSIONS = new String[]{TLS_v1, TLS_v1_1, TLS_v1_2, TLS_v1_3};

    // See https://tools.ietf.org/html/rfc8446#appendix-B.4
    private static final Set<String> TLSV13_CIPHERS = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList(
                    "TLS_AES_256_GCM_SHA384",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_AES_128_GCM_SHA256",
                    "TLS_AES_128_CCM_8_SHA256",
                    "TLS_AES_128_CCM_SHA256")));

    private static final boolean TLSV1_3_JDK_SUPPORTED;
    private static final boolean TLSV1_3_JDK_DEFAULT_ENABLED;
    private static final SSLContext DEFAULT_SSL_CONTEXT;
    private static final Set<String> SUPPORTED_CIPHERS;

    static {
        try {
            DEFAULT_SSL_CONTEXT = SSLContext.getInstance("TLS");
            DEFAULT_SSL_CONTEXT.init(null, null, null);
        } catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }

        // Choose the sensible default list of protocols that respects JDK flags, eg. jdk.tls.client.protocols
        SSLEngine engine = DEFAULT_SSL_CONTEXT.createSSLEngine();

        final String[] supportedProtocols = DEFAULT_SSL_CONTEXT.getDefaultSSLParameters().getProtocols();
        Set<String> enabledProtocols = new LinkedHashSet<>(Arrays.asList(supportedProtocols));
        enabledProtocols.addAll(Arrays.asList(ALL_TLS_VERSIONS));
        // https://aws.amazon.com/blogs/opensource/tls-1-0-1-1-changes-in-openjdk-and-amazon-corretto/
        // https://support.azul.com/hc/en-us/articles/360061143191-TLSv1-v1-1-No-longer-works-after-upgrade-No-appropriate-protocol-error
        engine.setEnabledProtocols(enabledProtocols.toArray(new String[0]));

        System.out.println("SSLEngine [server] enabled protocols: ");
        System.out.println(new HashSet<>(Arrays.asList(engine.getEnabledProtocols())));

        TLSV1_3_JDK_SUPPORTED = isTLSv13SupportedByJDK0();
        TLSV1_3_JDK_DEFAULT_ENABLED = isTLSv13EnabledByJDK0();

        Set<String> supportedCiphers = supportedCiphers(engine);
        SUPPORTED_CIPHERS = new LinkedHashSet<>(supportedCiphers);

        // GCM (Galois/Counter Mode) requires JDK 8.
        SUPPORTED_CIPHERS.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        SUPPORTED_CIPHERS.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        SUPPORTED_CIPHERS.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        SUPPORTED_CIPHERS.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        SUPPORTED_CIPHERS.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        // AES256 requires JCE unlimited strength jurisdiction policy files.
        SUPPORTED_CIPHERS.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
        // GCM (Galois/Counter Mode) requires JDK 8.
        SUPPORTED_CIPHERS.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
        SUPPORTED_CIPHERS.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        // AES256 requires JCE unlimited strength jurisdiction policy files.
        SUPPORTED_CIPHERS.add("TLS_RSA_WITH_AES_256_CBC_SHA");

        if (TLSV1_3_JDK_SUPPORTED) {
            // To avoid:
            // javax.net.ssl.SSLHandshakeException: The client supported protocol versions [TLSv1.3] are not accepted by server preferences [TLS12, TLS11, TLS10]
            // https://github.com/reactor/reactor-netty/issues/1224#issuecomment-666643495
            SUPPORTED_CIPHERS.addAll(TLSV13_CIPHERS);
        }
    }

    public static String[] includedCipherSuites() {
        return SUPPORTED_CIPHERS.toArray(new String[0]);
    }

    private static Set<String> supportedCiphers(SSLEngine engine) {
        // Choose the sensible default list of cipher suites.
        final String[] supportedCiphers = engine.getSupportedCipherSuites();
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
                    engine.setEnabledCipherSuites(new String[]{tlsPrefixedCipherName});
                    supportedCiphersSet.add(tlsPrefixedCipherName);
                } catch (IllegalArgumentException ignored) {
                    // The cipher is not supported ... move on to the next cipher.
                }
            }
        }
        return supportedCiphersSet;
    }

    private static boolean isTLSv13SupportedByJDK0() {
        try {
            return arrayContainsTls13(DEFAULT_SSL_CONTEXT.getSupportedSSLParameters().getProtocols());
        } catch (Throwable cause) {
            cause.printStackTrace();
            return false;
        }
    }

    private static boolean isTLSv13EnabledByJDK0() {
        try {
            return arrayContainsTls13(DEFAULT_SSL_CONTEXT.getDefaultSSLParameters().getProtocols());
        } catch (Throwable cause) {
            cause.printStackTrace();
            return false;
        }
    }

    private static boolean arrayContainsTls13(String[] array) {
        for (String v : array) {
            if (SslUtils.TLS_v1_3.equals(v)) {
                return true;
            }
        }
        return false;
    }
}
