package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.file.PdfTextExtractor
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import com.kenjdavidson.golf.handicap.verification.file.StructuredPdfNoShowRoundParser
import com.kenjdavidson.golf.handicap.ai.AiSettingsService
import com.kenjdavidson.golf.handicap.ai.GeminiProperties
import com.kenjdavidson.golf.handicap.ai.OllamaProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StructuredPdfNoShowRoundParserTest {

    @Test
    fun `parses new layout and skips rows flagged as no show`() {
        val text = """
            Date,Time,Holes,Player,Tee,Type,Paid,No Show
            5/10/2026,09:00,18,"Adderley, Jim",Blue Springs,Regular,Yes,No
            5/11/2026,09:10,18,"Adderley, Jim",Blue Springs,Regular,Yes,Yes
            5/09/2026,08:45,9,"Adderley, Jim",Blue Springs,Regular,Yes,No
        """.trimIndent()

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), testSettings())

        val parsed = parser.parse(ByteArray(0))

        assertEquals("Adderley, Jim", parsed.playerName)
        assertNull(parsed.memberId)
        assertNull(parsed.homeCourse)
        assertEquals(listOf(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 9)), parsed.rounds.map { it.playedDate })
        assertEquals("Blue Springs", parsed.rounds.first().playDistance)
    }

    @Test
    fun `parses tab separated layout rows`() {
        val text = """
            Date	Time	Holes	Player	Tee	Type	Paid	No Show
            6/01/2026	10:00	18	Smith, John	White	Regular	Yes	No
        """.trimIndent()

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), testSettings())

        val parsed = parser.parse(ByteArray(0))

        assertEquals(1, parsed.rounds.size)
        assertEquals(LocalDate.of(2026, 6, 1), parsed.rounds.first().playedDate)
    }

    @Test
    fun `throws when all rows are no show entries`() {
        val text = """
            Date,Time,Holes,Player,Tee,Type,Paid,No Show
            5/10/2026,09:00,18,"Adderley, Jim",Blue Springs,Regular,Yes,Yes
        """.trimIndent()

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), testSettings())

        assertThrows(VerificationProcessingException::class.java) {
            parser.parse(ByteArray(0))
        }
    }

    private class FakeExtractor(private val text: String) : PdfTextExtractor {
        override fun extract(pdfBytes: ByteArray): String = text
    }

    private fun testSettings(maxRounds: Int = 20): UserSettingsService = UserSettingsService(
        parsers = listOf(NoopRoundParser()),
        aiSettingsService = AiSettingsService(
            OllamaProperties("http://localhost:11434"),
            GeminiProperties("https://generativelanguage.googleapis.com", "gemini-2.5-flash", 0.1)
        ),
        verificationProperties = VerificationProperties(maxRounds)
    )

    private class NoopRoundParser : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = error("Not used in parser tests.")
    }
}
