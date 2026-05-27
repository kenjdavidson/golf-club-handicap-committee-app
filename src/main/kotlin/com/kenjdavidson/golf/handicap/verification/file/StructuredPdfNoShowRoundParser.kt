package com.kenjdavidson.golf.handicap.verification.file

import com.kenjdavidson.golf.handicap.settings.AppSettings
import com.kenjdavidson.golf.handicap.util.operation
import com.kenjdavidson.golf.handicap.verification.ParsedPlayerHistory
import com.kenjdavidson.golf.handicap.verification.ParsedRound
import com.kenjdavidson.golf.handicap.verification.VerificationProcessingException
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

@Service
@Order(2)
@ParserDefinition(
    displayNameKey = "settings.parser.type.pdfType2.name",
    descriptionKey = "settings.parser.type.pdfType2.description"
)
class StructuredPdfNoShowRoundParser(
    private val textExtractor: PdfTextExtractor,
    private val appSettings: AppSettings
) : RoundParser {

    override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = operation("Parsing PDF rounds with no-show support") {
        val rawText = textExtractor.extract(fileBytes)
        val normalizedLines = rawText.lines().map(String::trim).filter(String::isNotBlank)
        val rowChunks = reconstructRows(normalizedLines)

        if (rowChunks.isEmpty()) {
            throw VerificationProcessingException("No round rows were detected in the uploaded PDF.")
        }

        var playerName: String? = null
        val rounds = rowChunks.mapNotNull { row ->
            val columns = parseColumns(row)
            val playedDate = parsePlayedDate(columns.getOrNull(0) ?: return@mapNotNull null) ?: return@mapNotNull null
            val isNoShow = (columns.getOrNull(7) ?: "").trim().equals("yes", ignoreCase = true)
            if (isNoShow) {
                return@mapNotNull null
            }
            if (playerName.isNullOrBlank()) {
                playerName = columns.getOrNull(3)?.takeIf { it.isNotBlank() }
            }
            ParsedRound(
                playedDate = playedDate,
                playDistance = columns.getOrNull(4)?.takeIf { it.isNotBlank() },
                courseGroup = columns.getOrNull(5)?.takeIf { it.isNotBlank() },
                primaryClub = null
            )
        }.sortedByDescending { it.playedDate }
            .take(appSettings.maxRounds)

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

    private fun parseColumns(row: String): List<String> {
        val csvColumns = splitCsvRespectingQuotes(row)
        if (csvColumns.size > 1) {
            return csvColumns
        }
        if (!row.contains('\t')) {
            return csvColumns
        }
        return row.split('\t').map(String::trim)
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
        val ROW_START_REGEX = Regex("""^"?\d{1,2}/\d{1,2}/\d{4}\b.*""")
        val DATE_REGEX = Regex("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b")
        val PARSER_FORMATTER = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('/')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter(Locale.getDefault())
    }
}
