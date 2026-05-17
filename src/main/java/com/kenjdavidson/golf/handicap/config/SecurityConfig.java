package com.kenjdavidson.golf.handicap.config;

import com.kenjdavidson.golf.handicap.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 * Security configuration for the Handicap Committee application.
 *
 * <p>The server is bound exclusively to {@code 127.0.0.1} (see
 * {@code application.properties}), but the UI still requires an authenticated
 * session before any application routes are accessible.  Spring Security is
 * configured to:
 * <ul>
 *   <li>Use Vaadin's recommended Spring Security integration so framework
 *       internal requests remain accessible while views are protected.</li>
 *   <li>Expose the login view as the only anonymous route.</li>
 *   <li>Provide a small in-memory user store for the local application.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Value("${app.auth.username:committee}")
    private String username;

    @Value("${app.auth.password:committee}")
    private String password;

    @Bean
    public UserDetailsManager userDetailsService() {
        UserDetails user = User.withUsername(username)
            .password("{noop}" + password)
            .roles("COMMITTEE")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**").denyAll()
        );

        super.configure(http);
        setLoginView(http, LoginView.class);
    }
}
