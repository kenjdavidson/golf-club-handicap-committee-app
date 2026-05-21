package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;

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
    public static void main(String[] args) {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();
        new SpringApplicationBuilder(HandicapApplication.class)
                .headless(false)
                .run(args);
    }
}
