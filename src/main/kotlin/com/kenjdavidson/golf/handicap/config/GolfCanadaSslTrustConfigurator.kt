package com.kenjdavidson.golf.handicap.config

import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object GolfCanadaSslTrustConfigurator {
    private val log = LoggerFactory.getLogger(GolfCanadaSslTrustConfigurator::class.java)
    private const val CERTIFICATE_PATH = "certs/golfcanada.pem"

    @JvmStatic
    fun configureDefaultSslTrust() {
        try {
            val trustManager = createCompositeTrustManager()
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)
            SSLContext.setDefault(sslContext)
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            log.info("Installed Golf Canada certificate into application SSL trust configuration")
        } catch (_: IOException) {
            log.warn("Golf Canada certificate resource not found; continuing with default JVM truststore")
        } catch (ex: GeneralSecurityException) {
            log.warn("Unable to install Golf Canada certificate into SSL trust configuration: {}", ex.message)
        }
    }

    @JvmStatic
    fun createCompositeTrustManager(): X509TrustManager {
        val defaultTrustManager = getX509TrustManager(defaultTrustManagers())
        val golfCanadaTrustManager = getX509TrustManager(golfCanadaTrustManagers(loadGolfCanadaCertificate()))
        return CompositeX509TrustManager(defaultTrustManager, golfCanadaTrustManager)
    }

    @JvmStatic
    fun loadGolfCanadaCertificate(): X509Certificate {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        GolfCanadaSslTrustConfigurator::class.java.classLoader.getResourceAsStream(CERTIFICATE_PATH).use { stream ->
            if (stream == null) {
                throw IOException("Missing $CERTIFICATE_PATH")
            }
            return certificateFactory.generateCertificate(stream) as X509Certificate
        }
    }

    private fun defaultTrustManagers() = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
        init(null as KeyStore?)
    }.trustManagers

    private fun golfCanadaTrustManagers(golfCanadaCertificate: Certificate) =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("golfcanada", golfCanadaCertificate)
            init(keyStore)
        }.trustManagers

    private fun getX509TrustManager(trustManagers: Array<javax.net.ssl.TrustManager>): X509TrustManager {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        throw IllegalStateException("No X509TrustManager available")
    }

    private class CompositeX509TrustManager(
        private val defaultTrustManager: X509TrustManager,
        private val golfCanadaTrustManager: X509TrustManager
    ) : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType)
            } catch (_: CertificateException) {
                golfCanadaTrustManager.checkClientTrusted(chain, authType)
            }
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType)
            } catch (_: CertificateException) {
                golfCanadaTrustManager.checkServerTrusted(chain, authType)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> =
            defaultTrustManager.acceptedIssuers + golfCanadaTrustManager.acceptedIssuers
    }
}
