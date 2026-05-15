package com.kenjdavidson.golf.handicap.testsupport

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

object GolfCanadaSslTestSupport {
    @JvmStatic
    @JvmOverloads
    fun createRestTemplate(connectTimeoutMillis: Int = 10_000, readTimeoutMillis: Int = 10_000): RestTemplate {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust()

        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(connectTimeoutMillis)
        requestFactory.setReadTimeout(readTimeoutMillis)
        return RestTemplate(requestFactory)
    }
}
