package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi
import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import com.kenjdavidson.golf.handicap.golfcanada.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.client.HttpClientErrorException

class GolfCanadaAuthenticationServiceTest {
    private val authenticationApi = mock(AuthenticationApi::class.java)
    private val authenticationService = DefaultGolfCanadaAuthenticationService(authenticationApi)

    @Test
    fun `authenticate uses golf canada client and returns authenticated user`() {
        `when`(
            authenticationApi.authenticate(
                "password",
                "golf.user@example.com",
                "secret-password",
                false,
                "address email offline_access openid phone profile roles",
                null,
                null
            )
        ).thenReturn(
            AuthToken().accessToken("access-token").user(
                User()
                    .username("golf.user@example.com")
                    .fullName("Golf User")
                    .email("golf.user@example.com")
                    .golfCanadaCardId("1234567")
            )
        )

        val authenticatedUser = authenticationService.authenticate("golf.user@example.com", "secret-password")

        assertEquals("golf.user@example.com", authenticatedUser.username)
        assertEquals("Golf User", authenticatedUser.displayName)
        assertEquals("golf.user@example.com", authenticatedUser.email)
        verify(authenticationApi).authenticate(
            "password",
            "golf.user@example.com",
            "secret-password",
            false,
            "address email offline_access openid phone profile roles",
            null,
            null
        )
    }

    @Test
    fun `authenticate translates unauthorized response to bad credentials`() {
        `when`(
            authenticationApi.authenticate(
                "password",
                "golf.user@example.com",
                "wrong-password",
                false,
                "address email offline_access openid phone profile roles",
                null,
                null
            )
        ).thenThrow(HttpClientErrorException(HttpStatus.UNAUTHORIZED))

        assertThrows(BadCredentialsException::class.java) {
            authenticationService.authenticate("golf.user@example.com", "wrong-password")
        }
    }
}
