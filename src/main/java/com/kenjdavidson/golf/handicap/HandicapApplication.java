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

import java.util.Locale;

/**
 * Entry point for the Golf Club Handicap Committee application.
 *
 * <p>When started via {@link #main} the application auto-detects the host OS:
 * <ul>
 *   <li><b>Windows / macOS</b> – the {@code desktop} Spring profile is activated as
 *       the default, which binds Tomcat to {@code 127.0.0.1} and shuts down the JVM
 *       when the browser session ends.</li>
 *   <li><b>Linux (and all other OS)</b> – no default profile is set, so the base
 *       {@code application.properties} cloud-safe settings apply: the server listens
 *       on all interfaces and the automatic JVM shutdown listener is never registered.</li>
 * </ul>
 *
 * <p>{@code SPRING_PROFILES_ACTIVE} always takes precedence and can override the
 * OS-based default on any platform.
 *
 * <p>This auto-detection works identically in JVM and GraalVM native-image binaries
 * because {@link #main} still executes at runtime; Spring Boot's AOT processor
 * preserves {@code @Profile} condition metadata so that profile-gated beans are
 * evaluated at runtime rather than baked in at build time.
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
        SpringApplicationBuilder builder = new SpringApplicationBuilder(HandicapApplication.class)
                .headless(false);
        if (isDesktopOs()) {
            builder.properties("spring.profiles.default=desktop");
        }
        context = builder.run(args);
    }

    /**
     * Returns {@code true} on Windows and macOS, where the app runs as a local desktop
     * application. Returns {@code false} on Linux and all other platforms, which are
     * treated as cloud/server environments.
     */
    private static boolean isDesktopOs() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win") || os.contains("mac");
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
