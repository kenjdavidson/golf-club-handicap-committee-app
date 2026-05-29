package com.kenjdavidson.golf.handicap.config

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticationProvider
import com.kenjdavidson.golf.handicap.views.LoginView
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Lazy private val golfCanadaAuthenticationProvider: GolfCanadaAuthenticationProvider
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { requests ->
            requests.requestMatchers("/styles/**").permitAll()
            requests.requestMatchers("/h2-console/**").denyAll()
        }
        http.authenticationProvider(golfCanadaAuthenticationProvider)
        http.with(VaadinSecurityConfigurer.vaadin()) { vaadin ->
            vaadin.loginView(LoginView::class.java)
        }
        return http.build()
    }
}
