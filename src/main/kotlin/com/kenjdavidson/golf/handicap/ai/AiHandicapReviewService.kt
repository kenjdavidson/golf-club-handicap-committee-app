package com.kenjdavidson.golf.handicap.ai

import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaHistoryLookupService
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaScoreDetailsLookupService
import org.springframework.stereotype.Service

/**
 * Builds and submits an AI review prompt for a handicap verification result.
 *
 * The prompt includes:
 * - The summary outcome of the current verification checks (match percentage, status, notes)
 * - The round-by-round comparison between scheduled and Golf Canada rounds
 * - Score details for the last [VerificationProperties.maxRounds] Golf Canada rounds
 *
 * Callers are responsible for running this on a background thread and
 * pushing the result back to the Vaadin UI via [com.vaadin.flow.component.UI.access].
 */
@Service
class AiHandicapReviewService(
    private val historyLookupService: GolfCanadaHistoryLookupService,
    private val scoreDetailsLookupService: GolfCanadaScoreDetailsLookupService,
    private val verificationProperties: VerificationProperties
) {

    /**
     * Generates an AI review for the given [result].
     *
     * @param result     The completed verification result.
     * @param aiService  The active [AiIntegrationService] to use for generation.
     * @return The AI-generated review text.
     * @throws AiIntegrationException if the AI call fails.
     */
    fun review(result: FileVerificationResult, aiService: AiIntegrationService): String {
        val maxRounds = verificationProperties.maxRounds
        val scoreDetailsList = fetchScoreDetails(result, maxRounds)
        val prompt = buildPrompt(result, scoreDetailsList, maxRounds)
        return aiService.generate(prompt)
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun fetchScoreDetails(result: FileVerificationResult, maxRounds: Int): List<ScoreDetails> {
        val individualId = result.individualId ?: return emptyList()

        val historyEntries = historyLookupService.getHistory(individualId)
        return historyEntries
            .asSequence()
            .take(maxRounds)
            .mapNotNull { entry ->
                val scoreId = entry.id ?: return@mapNotNull null
                runCatching { scoreDetailsLookupService.getScoreDetails(scoreId) }.getOrNull()
            }
            .filterNotNull()
            .toList()
    }

    private fun buildPrompt(
        result: FileVerificationResult,
        scoreDetailsList: List<ScoreDetails>,
        maxRounds: Int
    ): String = buildString {
        appendLine("You are acting as a golf handicap committee member reviewing a member's scoring history for compliance.")
        appendLine("Please review the following verification results and scoring history for any irregularities.")
        appendLine("Keep your response concise. Start with a single summary line (e.g. 'No irregularities found.' or 'Some irregularities may need further review.'), then list any specific concerns as bullet points if applicable.")
        appendLine()

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
        if (scoreDetailsList.isEmpty()) {
            appendLine("No detailed score records available.")
        } else {
            scoreDetailsList.forEachIndexed { index, details ->
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
}
