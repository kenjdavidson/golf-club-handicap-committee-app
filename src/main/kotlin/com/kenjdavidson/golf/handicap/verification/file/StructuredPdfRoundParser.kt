package com.kenjdavidson.golf.handicap.verification.file

import com.kenjdavidson.golf.handicap.settings.UserSettingsService
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
@Order(1)
@ParserDefinition(
    displayNameKey = "settings.parser.type.pdfType1.name",
    descriptionKey = "settings.parser.type.pdfType1.description"
)
class StructuredPdfRoundParser(
    private val textExtractor: PdfTextExtractor,
    private val appSettings: UserSettingsService
) : RoundParser {

    override fun parse(fileBytes: ByteArray): ParsedPlayerHistory = operation("Parsing PDF rounds") {
        val rawText = textExtractor.extract(fileBytes)
        val normalizedLines = rawText.lines().map(String::trim).filter(String::isNotBlank)
        val ownerName = OWNER_NAME_REGEX.find(rawText)?.value
        val rowChunks = reconstructRows(normalizedLines)
        val inferredHomeCourse = inferHomeCourse(rowChunks, ownerName)

        if (rowChunks.isEmpty()) {
            throw VerificationProcessingException("No round rows were detected in the uploaded PDF.")
        }

        val rounds = rowChunks.mapNotNull { parseRound(it, ownerName, inferredHomeCourse) }
            .sortedByDescending { it.playedDate }
            .take(appSettings.maxRounds)

        if (rounds.isEmpty()) {
            throw VerificationProcessingException("No valid played dates were found in the uploaded PDF.")
        }

        val firstRow = rowChunks.firstOrNull()?.let { parseColumns(it, ownerName, inferredHomeCourse) }.orEmpty()

        ParsedPlayerHistory(
            playerName = firstRow.getOrNull(1)?.takeIf { it.isNotBlank() } ?: ownerName,
            memberId = firstRow.getOrNull(0)?.takeIf { it.isNotBlank() },
            homeCourse = firstRow.getOrNull(10)?.takeIf { it.isNotBlank() } ?: inferredHomeCourse,
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

    private fun parseRound(row: String, ownerName: String?, inferredHomeCourse: String?): ParsedRound? {
        val columns = parseColumns(row, ownerName, inferredHomeCourse)
        if (columns.size < 3) {
            return null
        }

        val playDistance = columns.getOrNull(3)
        val courseGroup = columns.getOrNull(8)
        if (isPracticeRound(playDistance, courseGroup)) {
            return null
        }

        val playedDate = parsePlayedDate(columns[2]) ?: return null
        return ParsedRound(
            playedDate = playedDate,
            playDistance = playDistance,
            courseGroup = courseGroup,
            primaryClub = columns.getOrNull(10)
        )
    }

    private fun parseColumns(row: String, ownerName: String?, inferredHomeCourse: String?): List<String> {
        val csvColumns = splitCsvRespectingQuotes(row)
        if (csvColumns.size >= 3 && parsePlayedDate(csvColumns[2]) != null) {
            return csvColumns
        }

        val rowMatch = FIXED_WIDTH_ROW_REGEX.matchEntire(row) ?: return csvColumns
        val memberId = rowMatch.groupValues[1]
        val nameAndLocations = rowMatch.groupValues[2]
        val playDate = rowMatch.groupValues[3]
        val playerName = OWNER_NAME_REGEX.find(nameAndLocations)?.value ?: ownerName
        val locationPrefix = playerName
            ?.takeIf { nameAndLocations.startsWith(it) }
            ?.let { nameAndLocations.removePrefix(it).trim() }
            ?: nameAndLocations.trim()
        val homeCourse = inferredHomeCourse?.takeIf { locationPrefix.startsWith(it) }
        val playDistance = when {
            homeCourse.isNullOrBlank() -> locationPrefix
            else -> locationPrefix.removePrefix(homeCourse).trim().ifBlank { homeCourse }
        }

        return MutableList(11) { "" }.apply {
            this[0] = memberId
            this[1] = playerName.orEmpty()
            this[2] = playDate
            this[3] = playDistance
            this[10] = homeCourse.orEmpty()
        }
    }

    private fun inferHomeCourse(rows: List<String>, ownerName: String?): String? {
        val prefixCounts = mutableMapOf<String, Int>()

        rows.forEach { row ->
            val rowMatch = FIXED_WIDTH_ROW_REGEX.matchEntire(row) ?: return@forEach
            val nameAndLocations = rowMatch.groupValues[2]
            val playerName = OWNER_NAME_REGEX.find(nameAndLocations)?.value ?: ownerName
            val locationPrefix = playerName
                ?.takeIf { nameAndLocations.startsWith(it) }
                ?.let { nameAndLocations.removePrefix(it).trim() }
                ?: nameAndLocations.trim()

            val words = locationPrefix.split(WHITESPACE_REGEX).filter(String::isNotBlank)
            if (words.size == 1) {
                val prefix = words.first()
                prefixCounts[prefix] = prefixCounts.getOrDefault(prefix, 0) + 1
                return@forEach
            }
            for (index in 1..<words.size) {
                val prefix = words.take(index).joinToString(" ")
                prefixCounts[prefix] = prefixCounts.getOrDefault(prefix, 0) + 1
            }
        }

        return prefixCounts.entries
            .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
            ?.key
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
        val OWNER_NAME_REGEX = Regex("([A-Za-z]+,\\s+[A-Z][a-z]+(?:[-'][A-Z][a-z]+)?)")
        val ROW_START_REGEX = Regex("^\\d{4,}\\b.*")
        val DATE_REGEX = Regex("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\d*\\b")
        val FIXED_WIDTH_ROW_REGEX = Regex(
            "^(\\d{4,})\\s+(.+?)\\s+(\\d{1,2}/\\d{1,2}/\\d{4})\\s*(\\d{1,2}:\\d{2}:\\d{2})\\s+" +
                "(\\d{1,2}/\\d{1,2}/\\d{4})\\s+(\\d{1,2}:\\d{2}:\\d{2})\\s+\\d+\\s+\\w+\\b.*$"
        )
        val PRACTICE_MARKERS = listOf("ACADEMY", "POINT AC", "POINT-AC", "SNAKE POINT AC")
        val WHITESPACE_REGEX = Regex("\\s+")
        val PARSER_FORMATTER = DateTimeFormatterBuilder()
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('/')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter()
    }
}
