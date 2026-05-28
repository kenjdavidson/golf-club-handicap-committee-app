package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.security.GolfCanadaAuthenticatedUser
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.streams.toList

class NavbarTest {
    @Test
    fun `uses consistent 200px width for user menu and language submenu items`() {
        val navbar = buildNavbar()
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

    @Test
    fun `fires main menu toggle event when app menu button is clicked`() {
        val navbar = buildNavbar()
        val toggled = AtomicBoolean(false)
        navbar.addAppMenuToggleListener {
            toggled.set(true)
        }

        val appMenuButton = readAppMenuButton(navbar)
        appMenuButton.click()

        assertTrue(toggled.get())
    }

    @Test
    fun `uses full size menu button with spacing from avatar`() {
        val navbar = buildNavbar()
        val appMenuButton = readAppMenuButton(navbar)
        val userSection = navbar.children
            .toList()
            .filterIsInstance<HorizontalLayout>()
            .first { it.style.get("margin-left") == "auto" }

        assertEquals("auto", userSection.style.get("margin-left"))
        assertEquals("var(--lumo-size-m)", appMenuButton.style.get("width"))
        assertEquals("var(--lumo-size-m)", appMenuButton.style.get("height"))
        assertEquals("var(--lumo-size-m)", appMenuButton.style.get("min-width"))
        assertTrue(appMenuButton.themeNames.contains("tertiary"))
        assertTrue(appMenuButton.themeNames.contains("icon"))
    }

    private fun buildNavbar(): Navbar {
        val authenticationContext = mock(AuthenticationContext::class.java)
        val userProfileResolver = mock(UserProfileResolver::class.java)
        val authenticatedUser = mock(GolfCanadaAuthenticatedUser::class.java)
        `when`(authenticatedUser.golfCanadaCardId).thenReturn("5200043264")
        `when`(userProfileResolver.resolveAuthenticatedUser(authenticationContext)).thenReturn(authenticatedUser)
        `when`(userProfileResolver.buildUserProfile(authenticatedUser)).thenReturn(
            UserProfile("Ken Davidson", "Committee User", "KD")
        )
        return Navbar(authenticationContext, userProfileResolver)
    }

    private fun readUserMenu(navbar: Navbar): ContextMenu {
        val userMenuField = Navbar::class.java.getDeclaredField("userMenu")
        userMenuField.isAccessible = true
        return userMenuField.get(navbar) as ContextMenu
    }

    private fun readAppMenuButton(navbar: Navbar): Button {
        val appMenuButtonField = Navbar::class.java.getDeclaredField("appMenuButton")
        appMenuButtonField.isAccessible = true
        return appMenuButtonField.get(navbar) as Button
    }
}
