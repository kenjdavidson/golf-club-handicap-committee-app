package com.kenjdavidson.golf.handicap.security

import org.springframework.stereotype.Component
import org.springframework.security.core.context.SecurityContextHolder

@Component
class CurrentAuthenticatedUserProvider {
    fun requireAuthenticatedUser(): GolfCanadaAuthenticatedUser {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication is available in the security context.")
        val principal = authentication.principal
        return principal as? GolfCanadaAuthenticatedUser
            ?: throw IllegalStateException("Authenticated principal is not a GolfCanadaAuthenticatedUser.")
    }

    fun requireAccessToken(): String = requireAuthenticatedUser().accessToken
}
