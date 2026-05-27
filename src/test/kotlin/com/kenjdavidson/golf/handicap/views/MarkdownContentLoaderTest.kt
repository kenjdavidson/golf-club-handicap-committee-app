package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.components.MarkdownContentLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Locale

class MarkdownContentLoaderTest {
    private val loader = MarkdownContentLoader()

    @Test
    fun `loads english markdown`() {
        val content = loader.requireLocalizedMarkdown("about/about", Locale.ENGLISH)

        assertTrue(content.contains("About Handicap Committee App"))
    }

    @Test
    fun `loads french markdown`() {
        val content = loader.requireLocalizedMarkdown("about/about", Locale.FRENCH)

        assertTrue(content.contains("À propos de l'application du comité de handicap"))
    }

    @Test
    fun `falls back to english for unsupported locale`() {
        val content = loader.requireLocalizedMarkdown("about/about", Locale.forLanguageTag("es"))

        assertEquals(loader.requireLocalizedMarkdown("about/about", Locale.ENGLISH), content)
    }

    @Test
    fun `throws when markdown resources are missing`() {
        assertThrows(IllegalStateException::class.java) {
            loader.requireLocalizedMarkdown("about/does-not-exist", Locale.ENGLISH)
        }
    }
}
