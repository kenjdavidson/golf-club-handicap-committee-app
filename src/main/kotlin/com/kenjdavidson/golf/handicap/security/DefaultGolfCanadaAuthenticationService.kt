package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.api.AuthenticationApi
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException

@Service
class DefaultGolfCanadaAuthenticationService(
    private val authenticationApi: AuthenticationApi
) : GolfCanadaAuthenticationService {

    override fun authenticate(username: String, password: String): GolfCanadaAuthenticatedUser {
        try {
            val authToken = authenticationApi.authenticate(
                GRANT_TYPE,
                username,
                password,
                false,
                SCOPE,
                null,
                null
            )

            return GolfCanadaAuthenticatedUser.from(authToken)
        } catch (exception: HttpClientErrorException) {
            if (exception.statusCode == HttpStatus.BAD_REQUEST || exception.statusCode == HttpStatus.UNAUTHORIZED) {
                throw BadCredentialsException("Invalid Golf Canada credentials.", exception)
            }

            throw AuthenticationServiceException("Golf Canada authentication failed.", exception)
        } catch (exception: RestClientException) {
            throw AuthenticationServiceException("Golf Canada authentication failed.", exception)
        } catch (exception: IllegalStateException) {
            throw AuthenticationServiceException("Golf Canada authentication returned an incomplete response.", exception)
        }
    }

    private companion object {
        const val GRANT_TYPE = "password"
        const val SCOPE = "address email offline_access openid phone profile roles"
    }
}
