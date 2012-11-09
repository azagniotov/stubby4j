package by.stub.server;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

final class FakeX509TrustManager implements X509TrustManager {

   FakeX509TrustManager() {

   }

   @Override
   public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

   }

   @Override
   public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

   }

   public boolean isClientTrusted(final X509Certificate[] chain) {
      return true;
   }

   public boolean isServerTrusted(final X509Certificate[] chain) {
      return true;
   }

   @Override
   public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[]{};
   }

   public void allowAllSSL() throws KeyManagementException, NoSuchAlgorithmException {
      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
         @Override
         public boolean verify(final String hostname, final SSLSession session) {
            return true;
         }
      });

      final SSLContext defaultSslContext = SSLContext.getInstance("TLS");
      defaultSslContext.init(new KeyManager[0], new TrustManager[]{this}, new SecureRandom());
      SSLContext.setDefault(defaultSslContext);
      HttpsURLConnection.setDefaultSSLSocketFactory(defaultSslContext.getSocketFactory());
   }
}