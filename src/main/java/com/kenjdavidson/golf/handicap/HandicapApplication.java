package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionDestroyListener;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Golf Club Handicap Committee application.
 *
 * <p>When started via {@link #main} the application runs in desktop mode
 * (activating the {@code desktop} Spring profile) which binds the embedded
 * Tomcat server exclusively to {@code 127.0.0.1} and automatically opens the
 * default system browser.  For cloud deployments the {@code desktop} profile
 * should not be active; the server will then listen on all interfaces and the
 * desktop-specific shutdown behaviour is disabled.
 *
 * <p>All data is held in an in-memory H2 database so no PII survives after
 * the process exits.
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@Push
@PWA(name = "Golf Handicap Committee App", shortName = "Golf Handicap")
public class HandicapApplication implements AppShellConfigurator {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();
        context = new SpringApplicationBuilder(HandicapApplication.class)
                .headless(false)
                .profiles("desktop")
                .run(args);
    }

    public static void shutdownAndExit() {
        if (context != null && context.isActive()) {
            log.info("🛑 Broadcasting native Spring Boot shutdown event sequence...");

            new Thread(() -> {
                try {
                    int exitCode = SpringApplication.exit(context, () -> 0);
                    System.exit(exitCode);
                } catch (Exception e) {
                    log.error("Error during graceful Spring shutdown", e);
                    System.exit(1);
                }
            }).start();
        } else {
            System.exit(0);
        }
    }

    @Bean
    @Profile("desktop")
    public VaadinServiceInitListener vaadinServiceInitListener() {
        return event -> event.getSource().addSessionDestroyListener((SessionDestroyListener) destroyEvent -> {
            log.info("🛑 Browser window closed and session heartbeats timed out. Shutting down Spring Boot backend...");

            // Spawn a quick background thread to close the context so it doesn't deadlock Vaadin's request thread
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    context.close();
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
}
