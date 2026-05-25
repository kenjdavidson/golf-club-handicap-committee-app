package com.kenjdavidson.golf.handicap.views

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class AboutContentLoaderTest {
    private val loader = AboutContentLoader()

    @Test
    fun `loads english markdown`() {
        val content = loader.requireMarkdown(Locale.ENGLISH)

        assertTrue(content.contains("About Handicap Committee App"))
    }

    @Test
    fun `loads french markdown`() {
        val content = loader.requireMarkdown(Locale.FRENCH)

        assertTrue(content.contains("À propos de l'application du comité de handicap"))
    }

    @Test
    fun `falls back to english for unsupported locale`() {
        val content = loader.requireMarkdown(Locale.forLanguageTag("es"))

        assertEquals(loader.requireMarkdown(Locale.ENGLISH), content)
    }
}
