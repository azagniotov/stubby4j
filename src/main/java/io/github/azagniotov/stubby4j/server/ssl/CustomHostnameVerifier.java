package io.github.azagniotov.stubby4j.server.ssl;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * getSubjectAltNames() this code has been inspired by:
 * https://github.com/apache/httpcomponents-client/blob/master/httpclient5/src/main/java/org/apache/hc/client5/http/ssl/DefaultHostnameVerifier.java
 */
final class CustomHostnameVerifier {

    private CustomHostnameVerifier() {

    }

    /**
     * Types of subject names: DNS = 2, IP = 7
     */
    static Set<String> getSubjectAltNames(final X509Certificate cert, final int subjectType) {
        try {
            final Collection<List<?>> entries = cert.getSubjectAlternativeNames();
            if (entries == null) {
                return Collections.emptySet();
            }
            final Set<String> result = new HashSet<>();
            for (final List<?> entry : entries) {
                // java.util.Collections$UnmodifiableRandomAccessList with multiple values
                final Integer type = entry.size() >= 2 ? (Integer) entry.get(0) : null;
                if (type != null) {
                    if (type == subjectType || -1 == subjectType) {
                        final Object o = entry.get(1);
                        if (o instanceof String) {
                            result.add((String) o);
                        }
                    }
                }
            }
            return result;
        } catch (final CertificateParsingException ignore) {
            return Collections.emptySet();
        }
    }

    static boolean isSubjectAltName(final String host, final X509Certificate cert) {
        // Types of subject names: DNS = 2, IP = 7
        final Set<String> subjectAltIps = getSubjectAltNames(cert, 7);
        final Set<String> subjectAltNames = getSubjectAltNames(cert, 2);

        return subjectAltIps.contains(host) || subjectAltNames.contains(host);
    }

    static boolean isX500PrincipalNameLocalhost(final X509Certificate cert) {
        return cert.getIssuerX500Principal().getName("canonical").contains("localhost");
    }
}
