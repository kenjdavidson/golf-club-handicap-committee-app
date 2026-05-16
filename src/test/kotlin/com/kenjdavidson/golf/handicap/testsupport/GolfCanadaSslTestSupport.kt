package com.kenjdavidson.golf.handicap.testsupport

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.util.Locale
import java.util.function.Supplier

object GolfCanadaSslTestSupport {
    private val defaultConnectTimeout = Duration.ofSeconds(10)
    private val defaultReadTimeout = Duration.ofSeconds(10)
    private val defaultRetryDelay = Duration.ofMillis(500)

    init {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust()
    }

    @JvmStatic
    @JvmOverloads
    fun createRestTemplate(
        connectTimeout: Duration = defaultConnectTimeout,
        readTimeout: Duration = defaultReadTimeout
    ): RestTemplate {
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(connectTimeout)
        requestFactory.setReadTimeout(readTimeout)
        return RestTemplate(requestFactory)
    }

    @JvmStatic
    @JvmOverloads
    fun <T> executeWithConnectionResetRetry(
        supplier: Supplier<T>,
        maxAttempts: Int = 3,
        retryDelay: Duration = defaultRetryDelay
    ): T {
        require(maxAttempts > 0) { "maxAttempts must be greater than zero" }

        var currentAttempt = 1
        while (true) {
            try {
                return supplier.get()
            } catch (exception: ResourceAccessException) {
                val shouldRetry = isConnectionReset(exception) && currentAttempt < maxAttempts
                if (!shouldRetry) {
                    throw exception
                }

                if (!retryDelay.isNegative && !retryDelay.isZero) {
                    Thread.sleep(retryDelay.toMillis())
                }
                currentAttempt++
            }
        }
    }

    @JvmStatic
    fun isConnectionReset(throwable: Throwable?): Boolean {
        var current = throwable
        while (current != null) {
            if (current.message?.lowercase(Locale.ROOT)?.contains("connection reset") == true) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
