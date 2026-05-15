package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class HandicapApplicationSslCertificateTest {

    @Test
    void configuresDefaultSslSocketFactoryWithGolfCanadaCertificate() {
        SSLSocketFactory before = HttpsURLConnection.getDefaultSSLSocketFactory();

        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();

        SSLSocketFactory after = HttpsURLConnection.getDefaultSSLSocketFactory();
        assertNotNull(after);
        assertNotSame(before, after);
    }

    @Test
    void compositeTrustManagerTrustsBundledGolfCanadaCertificate() throws Exception {
        X509TrustManager trustManager = GolfCanadaSslTrustConfigurator.createCompositeTrustManager();
        X509Certificate certificate = GolfCanadaSslTrustConfigurator.loadGolfCanadaCertificate();
        assertDoesNotThrow(() -> trustManager.checkServerTrusted(new X509Certificate[] {certificate}, "RSA"));
    }
}
