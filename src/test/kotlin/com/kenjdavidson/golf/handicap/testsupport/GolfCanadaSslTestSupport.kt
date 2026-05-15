package com.kenjdavidson.golf.handicap.testsupport

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

object GolfCanadaSslTestSupport {
    init {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust()
    }

    @JvmStatic
    @JvmOverloads
    fun createRestTemplate(connectTimeoutMillis: Int = 10_000, readTimeoutMillis: Int = 10_000): RestTemplate {
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMillis.toLong()))
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis.toLong()))
        return RestTemplate(requestFactory)
    }
}
