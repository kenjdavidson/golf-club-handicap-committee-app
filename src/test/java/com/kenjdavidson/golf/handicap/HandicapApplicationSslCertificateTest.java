package com.kenjdavidson.golf.handicap;

import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class HandicapApplicationSslCertificateTest {

    @Test
    void configuresDefaultSslSocketFactoryWithGolfCanadaCertificate() {
        SSLSocketFactory before = HttpsURLConnection.getDefaultSSLSocketFactory();

        HandicapApplication.configureGolfCanadaCertificateTrust();

        SSLSocketFactory after = HttpsURLConnection.getDefaultSSLSocketFactory();
        assertNotNull(after);
        assertNotSame(before, after);
    }
}
