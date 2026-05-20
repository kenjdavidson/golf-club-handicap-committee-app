package com.kenjdavidson.golf.handicap.config

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticationProvider
import com.kenjdavidson.golf.handicap.views.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Lazy private val golfCanadaAuthenticationProvider: GolfCanadaAuthenticationProvider
) : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { requests ->
            requests.requestMatchers("/h2-console/**").denyAll()
        }
        http.authenticationProvider(golfCanadaAuthenticationProvider)

        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }
}
