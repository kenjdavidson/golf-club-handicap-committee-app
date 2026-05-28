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
        assertEquals("Scheduled Course", AppMessages.translate(Locale.ENGLISH, "main.rounds.course"))
        assertEquals("Played Course", AppMessages.translate(Locale.ENGLISH, "main.rounds.gcCourse"))
        assertEquals("No Matching Score", AppMessages.translate(Locale.ENGLISH, "main.rounds.scheduledOnly"))
    }

    @Test
    fun `maps language codes to supported locales`() {
        assertEquals(Locale.ENGLISH, AppMessages.localeForLanguage("en"))
        assertEquals(Locale.FRENCH, AppMessages.localeForLanguage("fr"))
        assertNull(AppMessages.localeForLanguage("es"))
    }
}
