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
import org.springframework.scheduling.annotation.EnableAsync;

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
@EnableAsync
@Push
@PWA(name = "Golf Handicap Committee App", shortName = "Golf Handicap")
public class HandicapApplication implements AppShellConfigurator {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();
        context = new SpringApplicationBuilder(HandicapApplication.class)
                .headless(false)
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
