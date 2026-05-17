package com.kenjdavidson.golf.handicap.security

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class GolfCanadaAuthenticationProvider(
    private val golfCanadaAuthenticationService: GolfCanadaAuthenticationService
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name ?: ""
        val password = authentication.credentials?.toString().orEmpty()
        val authenticatedUser = golfCanadaAuthenticationService.authenticate(username, password)

        return UsernamePasswordAuthenticationToken.authenticated(
            authenticatedUser,
            null,
            authenticatedUser.authorities
        )
    }

    override fun supports(authentication: Class<*>): Boolean =
        UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
}
