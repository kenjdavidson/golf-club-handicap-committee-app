package com.kenjdavidson.golf.handicap.security

interface GolfCanadaAuthenticationService {
    fun authenticate(username: String, password: String): GolfCanadaAuthenticatedUser
}
