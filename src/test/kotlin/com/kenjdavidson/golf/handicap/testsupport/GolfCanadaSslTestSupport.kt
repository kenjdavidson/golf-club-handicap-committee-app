package com.kenjdavidson.golf.handicap.testsupport

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

object GolfCanadaSslTestSupport {
    private val defaultConnectTimeout = Duration.ofSeconds(10)
    private val defaultReadTimeout = Duration.ofSeconds(10)

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
}
