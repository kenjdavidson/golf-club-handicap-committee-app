package com.kenjdavidson.golf.handicap.config

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi
import com.kenjdavidson.golf.handicap.golfcanada.invoker.ApiClient
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticationProvider
import com.kenjdavidson.golf.handicap.views.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Lazy private val golfCanadaAuthenticationProvider: GolfCanadaAuthenticationProvider,
    @Value("\${app.golf-canada.base-url:https://scg.golfcanada.ca}") private val golfCanadaBaseUrl: String
) : VaadinWebSecurity() {

    @Bean
    fun golfCanadaApiClient(): ApiClient = ApiClient().setBasePath(golfCanadaBaseUrl.removeSuffix("/"))

    @Bean
    fun authenticationApi(apiClient: ApiClient): AuthenticationApi = AuthenticationApi(apiClient)

    override fun configure(http: HttpSecurity) {
        http.authorizeHttpRequests { requests ->
            requests.requestMatchers("/h2-console/**").denyAll()
        }
        http.authenticationProvider(golfCanadaAuthenticationProvider)

        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }
}
