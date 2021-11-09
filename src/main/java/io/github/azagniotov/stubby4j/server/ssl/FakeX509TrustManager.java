package io.github.azagniotov.stubby4j.server.ssl;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@GeneratedCodeCoverageExclusion
public final class FakeX509TrustManager implements X509TrustManager {

    private final Set<X509Certificate> acceptedIssuers = new HashSet<>();

    public FakeX509TrustManager() {

    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        acceptedIssuers.addAll(Arrays.asList(chain));
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        acceptedIssuers.addAll(Arrays.asList(chain));
    }

    public boolean isClientTrusted(final X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(final X509Certificate[] chain) {
        return true;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // it seems to be OK for Java <= 6 to return an empty array but not for Java 7 (at least 1.7.0_04-b20):
        // requesting an URL with a valid certificate throws:
        //  javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
        // when the array returned here is empty
        if (acceptedIssuers.isEmpty()) {
            return new X509Certificate[0];
        }
        return acceptedIssuers.toArray(new X509Certificate[0]);
    }
}