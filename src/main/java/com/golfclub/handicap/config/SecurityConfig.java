package com.golfclub.handicap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Handicap Committee application.
 *
 * <p>Because the server is bound exclusively to {@code 127.0.0.1} (see
 * {@code application.properties}), the primary network-layer protection is
 * already in place.  Spring Security is still configured to:
 * <ul>
 *   <li>Disable Spring's built-in CSRF filter.  Vaadin 24 manages its own
 *       CSRF tokens for every UI interaction via its stateful server-side
 *       model; duplicating CSRF handling at the Spring layer breaks Vaadin's
 *       internal request handling.  This is the <em>recommended</em>
 *       configuration for any Vaadin + Spring Security integration.</li>
 *   <li>Permit all requests from {@code localhost} without a login prompt
 *       (the committee members are already on the local machine).</li>
 *   <li>Allow Vaadin's internal endpoints ({@code /VAADIN/**}, etc.).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF is not required for a localhost-only desktop application.
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Vaadin framework paths
                .requestMatchers(
                    "/VAADIN/**",
                    "/vaadinServlet/**",
                    "/frontend/**",
                    "/frontend-es5/**",
                    "/frontend-es6/**",
                    "/icons/**",
                    "/images/**",
                    "/styles/**",
                    "/sw.js",
                    "/offline-page.html",
                    "/manifest.webmanifest"
                ).permitAll()
                // H2 console is disabled in properties; block it here too just in case.
                .requestMatchers("/h2-console/**").denyAll()
                // All other paths are accessible to any local request.
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
