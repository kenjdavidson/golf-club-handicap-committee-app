package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.util.operation
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

@Service
@Order(3)
@ParserDefinition(
    displayNameKey = "settings.parser.type.pdfType3.name",
    descriptionKey = "settings.parser.type.pdfType3.description"
)
class ClubLinkPdfRoundParser(
    private val textExtractor: PdfTextExtractor,
    private val verificationSettings: VerificationSettings
) : RoundParser {

    override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = operation("Parsing ClubLink PDF rounds") {
        val rawText = textExtractor.extract(fileBytes)
        val lines = rawText.lines().map(String::trim).filter(String::isNotBlank)

        val playerName = extractPlayerName(lines)
        val rounds = lines.mapNotNull { parseRound(it) }
            .sortedByDescending { it.playedDate }
            .take(verificationSettings.maxRounds)

        if (rounds.isEmpty()) {
            throw VerificationProcessingException("No valid played dates were found in the uploaded PDF.")
        }

        ParsedPlayerHistory(
            playerName = playerName,
            memberId = null,
            homeCourse = null,
            rounds = rounds
        )
    }

    private fun extractPlayerName(lines: List<String>): String? {
        for (line in lines) {
            val match = OWNER_NAME_LINE_REGEX.find(line) ?: continue
            val fullName = match.groupValues[1].trim()
            val parts = WHITESPACE_REGEX.split(fullName).filter(String::isNotBlank)
            if (parts.size < 2) continue
            val lastName = parts.last()
            val firstName = parts.dropLast(1).joinToString(" ")
            return "$lastName, $firstName"
        }
        return null
    }

    private fun parseRound(line: String): ParsedRound? {
        val match = ROW_REGEX.matchEntire(line) ?: return null
        val dateStr = match.groupValues[1]
        val isNoShow = match.groupValues[2].equals("Yes", ignoreCase = true)
        if (isNoShow) return null
        val playedDate = parsePlayedDate(dateStr) ?: return null
        return ParsedRound(
            playedDate = playedDate,
            playDistance = null,
            courseGroup = null,
            primaryClub = null
        )
    }

    private fun parsePlayedDate(dateStr: String): LocalDate? {
        // Drop the leading "DDD, " day-of-week token; only month/day/year are needed.
        val monthDayYear = dateStr.substringAfter(", ")
        return runCatching {
            LocalDate.parse(monthDayYear, DATE_FORMATTER)
        }.getOrNull()
    }

    private companion object {
        // Matches "TSHELTONMr.  Tom Winski" – uppercase username prefix + title + name
        val OWNER_NAME_LINE_REGEX = Regex("""^[A-Z]{2,}(?:Mr\.|Ms\.|Mrs\.|Dr\.)\s{1,}(.+)$""")
        val WHITESPACE_REGEX = Regex("""\s+""")

        // Matches round rows: "Sat, Aug 2, 25  18 Player One 7:30AM Yes No 1 No Cart"
        // Group 1: full date token ("Sat, Aug 2, 25"), Group 2: no-show flag ("Yes" or "No")
        // "Player One" may appear without a space before the time when hours are two digits.
        val ROW_REGEX = Regex(
            """^((?:Mon|Tue|Wed|Thu|Fri|Sat|Sun),\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+\d{1,2},\s+\d{2})""" +
                """\s+\d+\s+Player\s+One\s*\d{1,2}:\d{2}(?:AM|PM)\s+(?:Yes|No)\s+(Yes|No)\s+\d+\s+.+$"""
        )

        // "Aug 2, 25" (day-of-week stripped) – 2-digit year anchored to the 2000s (25 → 2025)
        val DATE_FORMATTER = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d, ")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .toFormatter(Locale.ENGLISH)
    }
}
