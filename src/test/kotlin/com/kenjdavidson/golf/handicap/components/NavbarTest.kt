package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.kenjdavidson.golf.handicap.views.UserProfile
import com.kenjdavidson.golf.handicap.views.UserProfileResolver
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.spring.security.AuthenticationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.Locale

class NavbarTest {
    @Test
    fun `uses consistent 200px width for user menu and language submenu items`() {
        val authenticationContext = mock(AuthenticationContext::class.java)
        val userProfileResolver = mock(UserProfileResolver::class.java)
        val authenticatedUser = mock(GolfCanadaAuthenticatedUser::class.java)
        `when`(authenticatedUser.golfCanadaCardId).thenReturn("5200043264")
        `when`(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(authenticatedUser)
        `when`(userProfileResolver.buildUserProfile(authenticatedUser)).thenReturn(
            UserProfile("Ken Davidson", "Committee User", "KD")
        )

        val navbar = Navbar(authenticationContext, userProfileResolver)
        val menu = readUserMenu(navbar)

        menu.items.forEach { menuItem ->
            assertEquals("200px", menuItem.style.get("min-width"))
            assertEquals("200px", menuItem.style.get("width"))
        }

        val languageLabel = AppMessages.translate(Locale.ENGLISH, "menu.language")
        val languageItem = menu.items.first { it.text == languageLabel }
        languageItem.subMenu.items.forEach { menuItem ->
            assertEquals("200px", menuItem.style.get("min-width"))
            assertEquals("200px", menuItem.style.get("width"))
        }
    }

    private fun readUserMenu(navbar: Navbar): ContextMenu {
        val userMenuField = Navbar::class.java.getDeclaredField("userMenu")
        userMenuField.isAccessible = true
        return userMenuField.get(navbar) as ContextMenu
    }
}
