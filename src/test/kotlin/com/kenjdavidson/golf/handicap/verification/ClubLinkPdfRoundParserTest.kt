package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.settings.UserSettingsService
import com.kenjdavidson.golf.handicap.verification.file.ClubLinkPdfRoundParser
import com.kenjdavidson.golf.handicap.verification.file.PdfTextExtractor
import com.kenjdavidson.golf.handicap.verification.file.RoundParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ClubLinkPdfRoundParserTest {

    /**
     * The full ClubLink report layout reproduced from the issue description.
     * Covers two pages, mixed AM/PM times, and the no-space artefact before two-digit hours.
     * All names, addresses, and identifiers are fictional and used for testing only.
     */
    private val fullReportText = """
        ClubLink Corporation ULC
        100 Example Boulevard
        (555) 000-1234
        clublink.ca
        Testville, ON A1B 2C3
        Canada
        Rounds History Page 1 of 2
        05/24/2026August 01, 2025 - May 24, 2026
        JTESTERMr.  John Tester
        999 Fake Street
        Sampletown, ON X9X 9X9
        Date Holes PlayerTime
        Price Level: PL
        Paid No ShowTee Type
        Sat, Aug 2, 25  18 Player One 7:30AM Yes No 1 No Cart
        Tue, Aug 5, 25  18 Player One 9:10AM Yes No 1 Power Cart
        Sun, Aug 10, 25  18 Player One10:50AM Yes No 1 Power Cart
        Thu, Aug 14, 25  18 Player One11:01AM Yes No 1 No Cart
        Rounds History Page 2 of 2
        05/24/2026August 01, 2025 - May 24, 2026
        JTESTERMr.  John Tester
        999 Fake Street
        Sampletown, ON X9X 9X9
        Date Holes PlayerTime
        Price Level: PL
        Paid No ShowTee Type
        Fri, May 22, 26  18 Player One 9:50AM Yes No 1 No Cart
        41
        41
        41
        -
        -
        Total Golf Bookings
        Total Rounds Played
        Total Rounds Paid
        Total Rounds Cancelled
        Total Rounds Unpaid
    """.trimIndent()

    @Test
    fun `parses player name from header by inverting first and last name`() {
        val parser = ClubLinkPdfRoundParser(FakeExtractor(fullReportText), testSettings())
        val parsed = parser.parse(ByteArray(0))
        assertEquals("Tester, John", parsed.playerName)
    }

    @Test
    fun `memberId and homeCourse are null for ClubLink format`() {
        val parser = ClubLinkPdfRoundParser(FakeExtractor(fullReportText), testSettings())
        val parsed = parser.parse(ByteArray(0))
        assertNull(parsed.memberId)
        assertNull(parsed.homeCourse)
    }

    @Test
    fun `parses round dates with two-digit year`() {
        val parser = ClubLinkPdfRoundParser(FakeExtractor(fullReportText), testSettings())
        val parsed = parser.parse(ByteArray(0))

        val dates = parsed.rounds.map { it.playedDate }
        assert(LocalDate.of(2026, 5, 22) in dates)
        assert(LocalDate.of(2025, 8, 2) in dates)
        assert(LocalDate.of(2025, 8, 5) in dates)
        assert(LocalDate.of(2025, 8, 10) in dates)
        assert(LocalDate.of(2025, 8, 14) in dates)
    }

    @Test
    fun `rounds are sorted most-recent first`() {
        val parser = ClubLinkPdfRoundParser(FakeExtractor(fullReportText), testSettings())
        val parsed = parser.parse(ByteArray(0))

        val dates = parsed.rounds.map { it.playedDate }
        assertEquals(dates.sortedDescending(), dates)
    }

    @Test
    fun `handles rows where time is concatenated directly after Player One`() {
        // "Player One10:50AM" – no space before two-digit hour
        val text = """
            JTESTERMr.  John Tester
            Sun, Aug 10, 25  18 Player One10:50AM Yes No 1 Power Cart
        """.trimIndent()
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings())
        val parsed = parser.parse(ByteArray(0))

        assertEquals(1, parsed.rounds.size)
        assertEquals(LocalDate.of(2025, 8, 10), parsed.rounds.first().playedDate)
    }

    @Test
    fun `filters out no-show rows`() {
        val text = """
            JTESTERMr.  John Tester
            Sat, Aug 2, 25  18 Player One 7:30AM Yes No 1 No Cart
            Tue, Aug 5, 25  18 Player One 9:10AM Yes Yes 1 No Cart
        """.trimIndent()
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings())
        val parsed = parser.parse(ByteArray(0))

        assertEquals(1, parsed.rounds.size)
        assertEquals(LocalDate.of(2025, 8, 2), parsed.rounds.first().playedDate)
    }

    @Test
    fun `caps rounds at maxRounds setting`() {
        val rows = (1..25).joinToString("\n") { day ->
            "Sat, Aug ${day.coerceAtMost(31)}, 25  18 Player One 9:00AM Yes No 1 No Cart"
        }
        val text = "JTESTERMr.  John Tester\n$rows"
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings(maxRounds = 20))
        val parsed = parser.parse(ByteArray(0))

        assertEquals(20, parsed.rounds.size)
    }

    @Test
    fun `throws when no valid round rows are found`() {
        val text = """
            ClubLink Corporation ULC
            100 Example Boulevard
            JTESTERMr.  John Tester
            Date Holes PlayerTime
        """.trimIndent()
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings())

        assertThrows(VerificationProcessingException::class.java) {
            parser.parse(ByteArray(0))
        }
    }

    @Test
    fun `throws when all rows are no-show`() {
        val text = """
            JTESTERMr.  John Tester
            Sat, Aug 2, 25  18 Player One 7:30AM Yes Yes 1 No Cart
        """.trimIndent()
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings())

        assertThrows(VerificationProcessingException::class.java) {
            parser.parse(ByteArray(0))
        }
    }

    @Test
    fun `skips repeated page headers and summary lines`() {
        // The full report text has two page headers and summary totals; only 5 real round rows.
        val parser = ClubLinkPdfRoundParser(FakeExtractor(fullReportText), testSettings())
        val parsed = parser.parse(ByteArray(0))

        assertEquals(5, parsed.rounds.size)
    }

    @Test
    fun `supports Ms title in owner name line`() {
        val text = """
            JDOEMs.  Jane Doe
            Sat, Aug 2, 25  18 Player One 7:30AM Yes No 1 No Cart
        """.trimIndent()
        val parser = ClubLinkPdfRoundParser(FakeExtractor(text), testSettings())
        val parsed = parser.parse(ByteArray(0))

        assertEquals("Doe, Jane", parsed.playerName)
    }

    private class FakeExtractor(private val text: String) : PdfTextExtractor {
        override fun extract(pdfBytes: ByteArray): String = text
    }

    private fun testSettings(maxRounds: Int = 20): UserSettingsService = UserSettingsService(
        parsers = listOf(NoopRoundParser()),
        verificationProperties = VerificationProperties(maxRounds)
    )

    private class NoopRoundParser : RoundParser {
        override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = error("Not used in parser tests.")
    }
}
