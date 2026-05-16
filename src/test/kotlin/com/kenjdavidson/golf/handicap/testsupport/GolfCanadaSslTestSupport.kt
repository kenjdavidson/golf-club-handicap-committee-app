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
    private val connectionResetPatterns = listOf("connection reset", "connection reset by peer")

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

        var lastException: ResourceAccessException? = null
        for (currentAttempt in 1..maxAttempts) {
            try {
                return supplier.get()
            } catch (exception: ResourceAccessException) {
                lastException = exception
                if (!isConnectionReset(exception) || currentAttempt == maxAttempts) {
                    break
                }

                if (retryDelay.toMillis() > 0) {
                    try {
                        Thread.sleep(retryDelay.toMillis())
                    } catch (interrupted: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IllegalStateException("Thread interrupted during retry delay for connection reset", interrupted)
                    }
                }
            }
        }

        throw requireNotNull(lastException) { "Failed to execute operation after $maxAttempts attempts" }
    }

    @JvmStatic
    fun isConnectionReset(throwable: Throwable?): Boolean {
        var current = throwable
        while (current != null) {
            val currentMessage = current.message?.lowercase(Locale.ROOT)
            if (isConnectionResetMessage(currentMessage)) {
                return true
            }
            current = current.cause
        }
        return false
    }

    private fun isConnectionResetMessage(message: String?): Boolean =
        message != null && connectionResetPatterns.any(message::contains)
}
