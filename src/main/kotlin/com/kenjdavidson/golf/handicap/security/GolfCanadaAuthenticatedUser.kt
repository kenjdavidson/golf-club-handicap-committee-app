package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import com.kenjdavidson.golf.handicap.golfcanada.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class GolfCanadaAuthenticatedUser(
    val authToken: AuthToken,
    private val usernameValue: String,
    private val displayNameValue: String,
    private val emailValue: String,
    private val authoritiesValue: Collection<GrantedAuthority>
) : UserDetails {

    val golfCanadaUser: User
        get() = authToken.user ?: throw IncompleteGolfCanadaAuthenticationException(MISSING_USER_MESSAGE)

    val accessToken: String
        get() = requiredText(authToken.accessToken, "access token")

    val displayName: String
        get() = displayNameValue

    val email: String
        get() = emailValue

    val golfCanadaCardId: String?
        get() = golfCanadaUser.golfCanadaCardId

    val handicap: String?
        get() = golfCanadaUser.handicap

    val pcc: String?
        get() = golfCanadaUser.pcc

    val membershipLevel: String?
        get() = golfCanadaUser.membershipLevel

    override fun getAuthorities(): Collection<GrantedAuthority> = authoritiesValue

    override fun getPassword(): String? = null

    override fun getUsername(): String = usernameValue

    companion object {
        @JvmStatic
        fun from(authToken: AuthToken): GolfCanadaAuthenticatedUser {
            val user = authToken.user ?: throw IncompleteGolfCanadaAuthenticationException(MISSING_USER_MESSAGE)
            requiredText(authToken.accessToken, "access token")
            val username = requiredText(user.username, "username")
            val displayName = requiredText(user.fullName, "user full name")
            val email = requiredText(user.email, "user email")

            return GolfCanadaAuthenticatedUser(
                authToken = authToken,
                usernameValue = username,
                displayNameValue = displayName,
                emailValue = email,
                authoritiesValue = buildAuthorities(user)
            )
        }

        private fun buildAuthorities(user: User): Collection<GrantedAuthority> {
            val authorities = linkedSetOf<GrantedAuthority>()
            authorities += SimpleGrantedAuthority("ROLE_USER")

            if (user.isAdmin == true) {
                authorities += SimpleGrantedAuthority("ROLE_ADMIN")
            }
            if (user.isHandicapChair == true) {
                authorities += SimpleGrantedAuthority("ROLE_HANDICAP_CHAIR")
            }
            if (user.isClubManagingUpgrades == true) {
                authorities += SimpleGrantedAuthority("ROLE_CLUB_ADMIN")
            }

            return authorities
        }

        private fun requiredText(value: String?, fieldName: String): String =
            value?.takeIf { it.isNotBlank() }
                ?: throw IncompleteGolfCanadaAuthenticationException(
                    "Golf Canada authentication response did not include $fieldName."
                )

        private const val MISSING_USER_MESSAGE = "Golf Canada authentication response did not include a user."
    }
}

class IncompleteGolfCanadaAuthenticationException(message: String) : IllegalStateException(message)
