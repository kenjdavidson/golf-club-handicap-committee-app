package com.kenjdavidson.golf.handicap;

import com.kenjdavidson.golf.handicap.config.GolfCanadaSslTrustConfigurator;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
public class HandicapApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        GolfCanadaSslTrustConfigurator.configureDefaultSslTrust();
        ConfigurableApplicationContext context = new SpringApplicationBuilder(HandicapApplication.class)
                .headless(false)
                .run(args);
        String protocol = context.getEnvironment().getProperty("server.ssl.key-store") != null ? "https" : "http";
        String port = context.getEnvironment().getProperty("server.port", "8080");
        DesktopAppLauncher.launchApp(args, protocol + "://localhost:" + port);
    }
}
