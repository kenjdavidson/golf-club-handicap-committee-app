package com.kenjdavidson.golf.handicap.verification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class StructuredPdfRoundParserTest {

    @Test
    fun `parses rows, fixes malformed year digits, filters practice rows, and caps at 20`() {
        val rows = (1..25).joinToString("\n") { day ->
            buildRow(
                memberId = "152314",
                name = "Ellis, Dean",
                played = "1/$day/2024${if (day == 25) "5" else ""}",
                playDistance = if (day == 24) "Snake Point Ac" else "North Course",
                courseGroup = "BClass",
                primaryClub = "Snake Point Golf Club"
            )
        }
        val parser = StructuredPdfRoundParser(FakeExtractor("Ellis, Dean\n$rows"), VerificationProperties(20))

        val parsed = parser.parse(ByteArray(0))

        assertEquals("Ellis, Dean", parsed.playerName)
        assertEquals("152314", parsed.memberId)
        assertEquals("Snake Point Golf Club", parsed.homeCourse)
        assertEquals(20, parsed.rounds.size)
        assertEquals(LocalDate.of(2024, 1, 25), parsed.rounds.first().playedDate)
        assertEquals(LocalDate.of(2024, 1, 5), parsed.rounds.last().playedDate)
    }

    @Test
    fun `reconstructs wrapped lines into a single row`() {
        val text = """
            Ellis, Dean
            152314,"Ellis, Dean","2/10/2024","North
            Course","08:00","created","time","Mon","BClass","A","Snake Point Golf Club"
        """.trimIndent()
        val parser = StructuredPdfRoundParser(FakeExtractor(text), VerificationProperties(20))

        val parsed = parser.parse(ByteArray(0))

        assertEquals(1, parsed.rounds.size)
        assertNotNull(parsed.rounds.first().playDistance)
    }

    @Test
    fun `throws when no round rows can be detected`() {
        val parser = StructuredPdfRoundParser(FakeExtractor("Ellis, Dean"), VerificationProperties(20))

        assertThrows(VerificationProcessingException::class.java) {
            parser.parse("pdf".toByteArray())
        }
    }

    private fun buildRow(
        memberId: String,
        name: String,
        played: String,
        playDistance: String,
        courseGroup: String,
        primaryClub: String
    ): String = listOf(
        memberId,
        "\"$name\"",
        "\"$played\"",
        "\"$playDistance\"",
        "\"08:00\"",
        "\"2024-01-01\"",
        "\"08:00\"",
        "\"Mon\"",
        "\"$courseGroup\"",
        "\"A\"",
        "\"$primaryClub\""
    ).joinToString(",")

    private class FakeExtractor(private val text: String) : PdfTextExtractor {
        override fun extract(pdfBytes: ByteArray): String = text
    }
}
