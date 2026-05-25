package com.kenjdavidson.golf.handicap

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

/**
 * Entry point for the Golf Club Handicap Committee desktop application.
 *
 * The application starts an embedded Tomcat server bound exclusively to
 * `127.0.0.1` and, once ready, automatically opens the default system
 * browser at the local URL. All data is held in an in-memory H2 database
 * so no PII survives after the process exits.
 */
@SpringBootApplication
class HandicapApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GolfCanadaSslTrustConfigurator.configureDefaultSslTrust()
            SpringApplicationBuilder(HandicapApplication::class.java)
                .headless(false)
                .run(*args)
        }
    }
}
