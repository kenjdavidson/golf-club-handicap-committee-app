package com.kenjdavidson.golf.handicap.ai

import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult

/**
 * Structured request passed to [AiIntegrationService.generate].
 *
 * Each integration service is responsible for converting this into the
 * format required by its underlying AI provider (e.g. plain text for Ollama,
 * structured JSON content for Gemini).
 *
 * @param verificationResult  The completed verification result including member profile,
 *                            status, and round comparisons.
 * @param scoreDetails        Hole-by-hole score records for the most-recent rounds.
 * @param maxRounds           The configured maximum number of rounds to include.
 */
data class AiReviewRequest(
    val verificationResult: FileVerificationResult,
    val scoreDetails: List<ScoreDetails> = emptyList(),
    val maxRounds: Int = 20
)

/**
 * Renders the verification data as a plain-text block suitable for use as the
 * user-facing content in any AI provider's prompt.
 *
 * System-level instructions (persona, tone, output format) are intentionally
 * **not** included here — each [AiIntegrationService] implementation supplies
 * those through its own mechanism (Ollama Modelfile, Gemini system instruction,
 * etc.).
 */
fun AiReviewRequest.toPromptText(): String = buildString {
    val result = verificationResult
    appendLine("=== VERIFICATION SUMMARY ===")
    appendLine("Member: ${result.memberProfile.fullName ?: "Unknown"}")
    appendLine("Status: ${result.status}")
    appendLine("Date Match: ${result.matchPercentage}% (${result.matchedCount} of ${result.comparedCount} rounds matched)")
    if (result.mismatchedDates.isNotEmpty()) {
        appendLine("Mismatched Dates: ${result.mismatchedDates.joinToString(", ")}")
    }
    if (result.notes.isNotEmpty()) {
        appendLine("Notes: ${result.notes.joinToString("; ")}")
    }
    appendLine()

    appendLine("=== ROUND COMPARISON (Scheduled vs Golf Canada) ===")
    if (result.roundComparisons.isEmpty()) {
        appendLine("No round comparisons available.")
    } else {
        result.roundComparisons.forEach { comparison ->
            val scheduled = comparison.scheduledRound?.let {
                "Scheduled: ${it.playedDate} at ${it.playDistance ?: "unknown course"}"
            } ?: "Scheduled: (none)"
            val gcEntry = comparison.golfCanadaEntry?.let {
                "GolfCanada: ${it.date?.toLocalDate()} at ${it.course ?: "unknown"} (score=${it.score}, differential=${it.differential})"
            } ?: "GolfCanada: (none)"
            val matchStatus = if (comparison.isMatched) "MATCHED" else "UNMATCHED"
            appendLine("  [$matchStatus] $scheduled | $gcEntry")
        }
    }
    appendLine()

    appendLine("=== RECENT GOLF CANADA SCORE DETAILS (last $maxRounds rounds) ===")
    if (scoreDetails.isEmpty()) {
        appendLine("No detailed score records available.")
    } else {
        scoreDetails.forEachIndexed { index, details ->
            appendLine("Round ${index + 1}: ${details.playDate?.toLocalDate()} at ${details.course ?: "unknown"}")
            appendLine("  Score: gross=${details.gross}, net=${details.net}, par=${details.par}, differential=${details.differential}")
            val holeScores = details.holeScores
            if (!holeScores.isNullOrEmpty()) {
                val front = holeScores.take(9).mapNotNull { it.gross }.joinToString(", ")
                val back = holeScores.drop(9).mapNotNull { it.gross }.joinToString(", ")
                if (front.isNotBlank()) appendLine("  Front 9 strokes: $front")
                if (back.isNotBlank()) appendLine("  Back 9 strokes: $back")
            }
        }
    }
    appendLine()
    appendLine("Please provide your review.")
}
