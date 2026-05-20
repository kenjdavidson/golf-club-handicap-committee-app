package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import com.kenjdavidson.golf.handicap.golfcanada.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class CurrentAuthenticatedUserProviderTest {
    private val currentAuthenticatedUserProvider = CurrentAuthenticatedUserProvider()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns current access token from security context`() {
        val authenticatedUser = GolfCanadaAuthenticatedUser(
            authToken = AuthToken().accessToken("access-token").user(
                User()
                    .username("committee.user")
                    .fullName("Committee User")
                    .email("committee.user@example.com")
            ),
            usernameValue = "committee.user",
            displayNameValue = "Committee User",
            emailValue = "committee.user@example.com",
            authoritiesValue = listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            authenticatedUser,
            null,
            authenticatedUser.authorities
        )

        val accessToken = currentAuthenticatedUserProvider.requireAccessToken()

        assertEquals("access-token", accessToken)
    }

    @Test
    fun `throws when no authenticated golf canada user is present`() {
        SecurityContextHolder.clearContext()

        assertThrows(IllegalStateException::class.java) {
            currentAuthenticatedUserProvider.requireAccessToken()
        }
    }
}
