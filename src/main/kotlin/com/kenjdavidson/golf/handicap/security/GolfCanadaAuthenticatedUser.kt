package com.kenjdavidson.golf.handicap.security

import com.kenjdavidson.golf.handicap.golfcanada.model.AuthToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class GolfCanadaAuthenticatedUser(
    private val usernameValue: String,
    val displayName: String,
    val email: String?,
    val golfCanadaCardId: String?,
    val accessToken: String,
    private val authoritiesValue: Collection<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authoritiesValue

    override fun getPassword(): String? = null

    override fun getUsername(): String = usernameValue

    companion object {
        @JvmStatic
        fun from(authToken: AuthToken): GolfCanadaAuthenticatedUser {
            val user = authToken.user ?: error("Golf Canada authentication response did not include a user.")
            val username = user.username?.takeIf { it.isNotBlank() }
                ?: user.email?.takeIf { it.isNotBlank() }
                ?: "golf-canada-user"
            val displayName = user.fullName?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(user.firstName, user.lastName)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                ?: username
            val accessToken = authToken.accessToken?.takeIf { it.isNotBlank() }
                ?: error("Golf Canada authentication response did not include an access token.")

            return GolfCanadaAuthenticatedUser(
                usernameValue = username,
                displayName = displayName,
                email = user.email?.takeIf { it.isNotBlank() },
                golfCanadaCardId = user.golfCanadaCardId?.takeIf { it.isNotBlank() },
                accessToken = accessToken,
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
    }
}
