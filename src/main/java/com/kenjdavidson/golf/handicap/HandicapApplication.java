package com.kenjdavidson.golf.handicap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Entry point for the Golf Club Handicap Committee desktop application.
 *
 * <p>The application starts an embedded Tomcat server bound exclusively to
 * {@code 127.0.0.1} and, once ready, automatically opens the default system
 * browser at the local URL.  All data is held in an in-memory H2 database
 * so no PII survives after the process exits.
 */
@Slf4j
@SpringBootApplication
public class HandicapApplication {

    private final Environment environment;

    public HandicapApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        configureGolfCanadaCertificateTrust();
        SpringApplication.run(HandicapApplication.class, args);
    }

    static void configureGolfCanadaCertificateTrust() {
        try {
            X509TrustManager trustManager = createCompositeTrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                null,
                new TrustManager[] {trustManager},
                null
            );
            SSLContext.setDefault(sslContext);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            log.info("Installed Golf Canada certificate into application SSL trust configuration");
        } catch (IOException ex) {
            log.warn("Golf Canada certificate resource not found; continuing with default JVM truststore");
        } catch (GeneralSecurityException ex) {
            log.warn("Unable to install Golf Canada certificate into SSL trust configuration: {}", ex.getMessage());
        }
    }

    static X509TrustManager createCompositeTrustManager() throws IOException, GeneralSecurityException {
        X509TrustManager defaultTrustManager = getX509TrustManager(defaultTrustManagers());
        X509TrustManager golfCanadaTrustManager = getX509TrustManager(golfCanadaTrustManagers(loadGolfCanadaCertificate()));
        return new CompositeX509TrustManager(defaultTrustManager, golfCanadaTrustManager);
    }

    static X509Certificate loadGolfCanadaCertificate() throws IOException, GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        try (InputStream certificateStream = HandicapApplication.class.getClassLoader()
            .getResourceAsStream("certs/golfcanada.pem")) {
            if (certificateStream == null) {
                throw new IOException("Missing certs/golfcanada.pem");
            }
            return (X509Certificate) certificateFactory.generateCertificate(certificateStream);
        }
    }

    private static TrustManager[] defaultTrustManagers() throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        return trustManagerFactory.getTrustManagers();
    }

    private static TrustManager[] golfCanadaTrustManagers(Certificate golfCanadaCertificate) throws IOException, GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("golfcanada", golfCanadaCertificate);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static X509TrustManager getX509TrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager x509TrustManager) {
                return x509TrustManager;
            }
        }
        throw new IllegalStateException("No X509TrustManager available");
    }

    private record CompositeX509TrustManager(
        X509TrustManager defaultTrustManager,
        X509TrustManager golfCanadaTrustManager
    ) implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType);
            } catch (java.security.cert.CertificateException ex) {
                golfCanadaTrustManager.checkClientTrusted(chain, authType);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (java.security.cert.CertificateException ex) {
                golfCanadaTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] primaryIssuers = defaultTrustManager.getAcceptedIssuers();
            X509Certificate[] secondaryIssuers = golfCanadaTrustManager.getAcceptedIssuers();
            X509Certificate[] merged = Arrays.copyOf(primaryIssuers, primaryIssuers.length + secondaryIssuers.length);
            System.arraycopy(secondaryIssuers, 0, merged, primaryIssuers.length, secondaryIssuers.length);
            return merged;
        }
    }

    /**
     * Opens the default system browser at the application URL once Spring Boot
     * has finished starting.  This gives the application a native-desktop feel
     * without requiring any additional launcher code.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserOnStartup() {
        String port = environment.getProperty("server.port", "8080");
        String url = "http://localhost:" + port;

        log.info("Application ready – opening browser at {}", url);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                log.warn("Could not open browser automatically: {}", e.getMessage());
            }
        } else {
            log.info("Desktop integration not available. Navigate to {} manually.", url);
        }
    }
}
