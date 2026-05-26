package com.kenjdavidson.golf.handicap.verification

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

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), VerificationProperties(20))

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

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), VerificationProperties(20))

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

        val parser = StructuredPdfNoShowRoundParser(FakeExtractor(text), VerificationProperties(20))

        assertThrows(VerificationProcessingException::class.java) {
            parser.parse(ByteArray(0))
        }
    }

    private class FakeExtractor(private val text: String) : PdfTextExtractor {
        override fun extract(pdfBytes: ByteArray): String = text
    }
}
