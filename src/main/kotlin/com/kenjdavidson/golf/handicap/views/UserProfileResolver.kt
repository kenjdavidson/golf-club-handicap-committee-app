package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.vaadin.flow.spring.security.AuthenticationContext
import org.springframework.stereotype.Component

@Component
class UserProfileResolver {
    fun resolveAuthenticatedUser(authenticationContext: AuthenticationContext): GolfCanadaAuthenticatedUser {
        return authenticationContext.getAuthenticatedUser(GolfCanadaAuthenticatedUser::class.java)
            .orElseThrow {
                IllegalStateException(
                    "No authenticated Golf Canada user found in the security context. Please log in again to continue."
                )
            }
    }

    fun buildUserProfile(authenticatedUser: GolfCanadaAuthenticatedUser): UserProfile {
        return UserProfile(
            displayName = authenticatedUser.displayName,
            details = buildUserDetails(authenticatedUser),
            initials = buildInitials(authenticatedUser.displayName)
        )
    }

    private fun buildUserDetails(authenticatedUser: GolfCanadaAuthenticatedUser): String {
        return listOfNotNull(
            authenticatedUser.email,
            authenticatedUser.handicap?.takeIf { it.isNotBlank() }?.let { "HCP $it" },
            authenticatedUser.membershipLevel?.takeIf { it.isNotBlank() }
        ).joinToString(" • ")
    }

    private fun buildInitials(displayName: String?): String {
        if (displayName.isNullOrBlank()) {
            return "CU"
        }

        val parts = displayName.trim().split("\\s+".toRegex())
        val first = parts.firstOrNull()?.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        val second = parts.drop(1).lastOrNull()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return first + second
    }
}
