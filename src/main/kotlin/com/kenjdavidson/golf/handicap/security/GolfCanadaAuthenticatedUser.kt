package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import com.kenjdavidson.golf.handicap.golfcanada.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class GolfCanadaAuthenticatedUser(
    val authToken: AuthToken,
    private val authoritiesValue: Collection<GrantedAuthority>
) : UserDetails {

    val golfCanadaUser: User
        get() = authToken.user ?: throw IncompleteGolfCanadaAuthenticationException(
            "Golf Canada authentication response did not include a user."
        )

    val accessToken: String
        get() = requiredText(authToken.accessToken, "access token")

    val displayName: String
        get() = requiredText(golfCanadaUser.fullName, "user full name")

    val email: String
        get() = requiredText(golfCanadaUser.email, "user email")

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

    override fun getUsername(): String = requiredText(golfCanadaUser.username, "username")

    companion object {
        @JvmStatic
        fun from(authToken: AuthToken): GolfCanadaAuthenticatedUser {
            val user = authToken.user ?: throw IncompleteGolfCanadaAuthenticationException(
                "Golf Canada authentication response did not include a user."
            )
            requiredText(authToken.accessToken, "access token")
            requiredText(user.username, "username")
            requiredText(user.fullName, "user full name")
            requiredText(user.email, "user email")

            return GolfCanadaAuthenticatedUser(
                authToken = authToken,
                authoritiesValue = buildAuthorities(user)
            )
        }

        private fun buildAuthorities(user: com.kenjdavidson.golf.handicap.golfcanada.model.User): Collection<GrantedAuthority> {
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
    }
}

class IncompleteGolfCanadaAuthenticationException(message: String) : IllegalStateException(message)
