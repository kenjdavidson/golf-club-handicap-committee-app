package com.kenjdavidson.golf.handicap;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;
import java.util.Objects;

/**
 * Attempts to open the local browser pointing to the application.
 */
@Component
public class OpenBrowserApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenBrowserApplicationRunner.class);

    private final Environment environment;
    private final WebServerApplicationContext webServerContext;

    public OpenBrowserApplicationRunner(
            Environment environment,
            WebServerApplicationContext webServerContext
    ) {
        this.environment = environment;
        this.webServerContext = webServerContext;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        // 1. Defensively double-check headless mode is off before checking Desktop support
        if (System.getProperty("java.awt.headless", "true").equals("true")) {
            log.info("System is running in headless mode. Automatic browser opening skipped.");
            return;
        }

        // 2. Dynamically resolve actual port & protocol (handles random ports and SSL perfectly)
        int port = Objects.requireNonNull(webServerContext.getWebServer()).getPort();
        String keyStore = environment.getProperty("server.ssl.key-store");
        String protocol = (keyStore != null) ? "https" : "http";
        String url = protocol + "://localhost:" + port;

        log.info("Application ready – attempting to open default browser at {}", url);

        // 3. Trigger native OS Desktop browser invocation
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                log.warn("Desktop supported, but browser launch failed: {}", e.getMessage());
            }
        } else {
            log.info("Native desktop integration not supported on this OS. Navigate to {} manually.", url);
        }
    }
}