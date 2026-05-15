package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();
        SpringApplication.run(HandicapApplication.class, args);
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
