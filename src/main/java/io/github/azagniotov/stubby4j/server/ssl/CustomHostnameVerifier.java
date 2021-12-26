package io.github.azagniotov.stubby4j.server.ssl;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeMethodCoverageExclusion;

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

    private final X509Certificate x509;
    private final Set<String> subjectAltIps;
    private final Set<String> subjectAltNames;

    CustomHostnameVerifier(final X509Certificate x509) {
        this.x509 = x509;
        this.subjectAltIps = getSubjectAltNames(7);
        this.subjectAltNames = getSubjectAltNames(2);
    }

    @GeneratedCodeMethodCoverageExclusion
    public boolean isSubjectAltNamesContainPrivateIp() {
        for (final String altNameAsIp : this.subjectAltIps) {
            if (LanIPv4Validator.isPrivateIp(altNameAsIp)) {
                return true;
            }
        }

        return false;
    }

    boolean isX500PrincipalNameLocalhost() {
        return x509.getIssuerX500Principal().getName("canonical").contains("localhost");
    }

    /**
     * Types of subject names: DNS = 2, IP = 7
     */
    Set<String> getSubjectAltNames(final int subjectType) {
        try {
            final Collection<List<?>> entries = this.x509.getSubjectAlternativeNames();
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

    boolean isSubjectAltNamesContain(final String host) {
        // Types of subject names: DNS = 2, IP = 7
        return this.subjectAltIps.contains(host) || this.subjectAltNames.contains(host);
    }
}
