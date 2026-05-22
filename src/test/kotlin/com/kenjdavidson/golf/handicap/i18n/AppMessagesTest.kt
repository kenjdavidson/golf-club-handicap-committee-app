package com.kenjdavidson.golf.handicap.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Locale

class AppMessagesTest {
    @Test
    fun `translates english and french bundles`() {
        assertEquals("Logout", AppMessages.translate(Locale.ENGLISH, "menu.logout"))
        assertEquals("Se déconnecter", AppMessages.translate(Locale.FRENCH, "menu.logout"))
    }

    @Test
    fun `maps language codes to supported locales`() {
        assertEquals(Locale.ENGLISH, AppMessages.localeForLanguage("en"))
        assertEquals(Locale.FRENCH, AppMessages.localeForLanguage("fr"))
        assertNull(AppMessages.localeForLanguage("es"))
    }
}
