package com.kenjdavidson.golf.handicap.verification

import com.kenjdavidson.golf.handicap.util.operation
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

@Service
class StructuredPdfRoundParser(
    private val textExtractor: PdfTextExtractor,
    private val verificationProperties: VerificationProperties
) : PdfRoundParser {

    override fun parse(pdfBytes: ByteArray): ParsedPlayerHistory = operation("Parsing PDF rounds") {
        val rawText = textExtractor.extract(pdfBytes)
        val normalizedLines = rawText.lines().map(String::trim).filter(String::isNotBlank)
        val ownerName = OWNER_NAME_REGEX.find(rawText)?.value
        val rowChunks = reconstructRows(normalizedLines)

        if (rowChunks.isEmpty()) {
            throw VerificationProcessingException("No round rows were detected in the uploaded PDF.")
        }

        val rounds = rowChunks.mapNotNull(::parseRound)
            .sortedByDescending { it.playedDate }
            .take(verificationProperties.maxRounds)

        if (rounds.isEmpty()) {
            throw VerificationProcessingException("No valid played dates were found in the uploaded PDF.")
        }

        val firstRow = rowChunks.firstOrNull()?.let(::splitCsvRespectingQuotes).orEmpty()

        ParsedPlayerHistory(
            playerName = firstRow.getOrNull(1)?.takeIf { it.isNotBlank() } ?: ownerName,
            memberId = firstRow.getOrNull(0)?.takeIf { it.isNotBlank() },
            homeCourse = firstRow.getOrNull(10)?.takeIf { it.isNotBlank() },
            rounds = rounds
        )
    }

    private fun reconstructRows(lines: List<String>): List<String> {
        val rows = mutableListOf<String>()
        val current = StringBuilder()
        var started = false

        for (line in lines) {
            if (line.matches(ROW_START_REGEX)) {
                started = true
                if (current.isNotBlank()) {
                    rows += current.toString()
                    current.setLength(0)
                }
            }
            if (!started) {
                continue
            }
            if (current.isNotEmpty()) {
                current.append(' ')
            }
            current.append(line)
        }

        if (current.isNotBlank()) {
            rows += current.toString()
        }
        return rows
    }

    private fun parseRound(row: String): ParsedRound? {
        val columns = splitCsvRespectingQuotes(row)
        if (columns.size < 3) {
            return null
        }

        val playDistance = columns.getOrNull(3)
        val courseGroup = columns.getOrNull(8)
        if (isPracticeRound(playDistance, courseGroup)) {
            return null
        }

        val playedDate = parsePlayedDate(columns[2]) ?: return null
        val partners = columns.drop(11).filter { it.isNotBlank() }
        return ParsedRound(
            playedDate = playedDate,
            playDistance = playDistance,
            courseGroup = courseGroup,
            primaryClub = columns.getOrNull(10),
            playingPartners = partners
        )
    }

    private fun parsePlayedDate(rawPlayedDate: String): LocalDate? {
        val match = DATE_REGEX.find(rawPlayedDate) ?: return null
        val month = match.groupValues[1].toIntOrNull() ?: return null
        val day = match.groupValues[2].toIntOrNull() ?: return null
        val year = match.groupValues[3].toIntOrNull() ?: return null
        return runCatching {
            LocalDate.parse("$month/$day/$year", PARSER_FORMATTER)
        }.getOrNull()
    }

    private fun isPracticeRound(playDistance: String?, courseGroup: String?): Boolean {
        val combined = listOfNotNull(playDistance, courseGroup)
            .joinToString(" ")
            .uppercase(Locale.getDefault())

        return PRACTICE_MARKERS.any { marker -> combined.contains(marker) }
    }

    private fun splitCsvRespectingQuotes(value: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        value.forEach { ch ->
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> if (inQuotes) {
                    current.append(ch)
                } else {
                    fields += current.toString().trim()
                    current.setLength(0)
                }

                else -> current.append(ch)
            }
        }

        fields += current.toString().trim()
        return fields
    }

    private companion object {
        val OWNER_NAME_REGEX = Regex("([A-Za-z]+,\\s+[A-Za-z]+)")
        val ROW_START_REGEX = Regex("^\\d{4,}\\b.*")
        val DATE_REGEX = Regex("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\d*\\b")
        val PRACTICE_MARKERS = listOf("ACADEMY", "POINT AC", "POINT-AC", "SNAKE POINT AC")
        val PARSER_FORMATTER = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('/')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()
    }
}
