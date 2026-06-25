package com.kenjdavidson.golf.handicap.ai

import com.kenjdavidson.golf.handicap.golfcanada.model.ScoreDetails
import com.kenjdavidson.golf.handicap.verification.FileVerificationResult
import com.kenjdavidson.golf.handicap.verification.VerificationProperties
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaHistoryLookupService
import com.kenjdavidson.golf.handicap.verification.api.GolfCanadaScoreDetailsLookupService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Assembles an [AiReviewRequest] from a handicap verification result and
 * submits it to the active [AiIntegrationService].
 *
 * The request includes:
 * - The verification summary (status, match %, mismatched dates, notes)
 * - The round-by-round comparison between scheduled and Golf Canada rounds
 * - Score details for the last [VerificationProperties.maxRounds] Golf Canada rounds
 *
 * Each [AiIntegrationService] implementation is responsible for converting
 * the structured request into its own provider-specific format.
 */
@Service
class AiHandicapReviewService(
    private val historyLookupService: GolfCanadaHistoryLookupService,
    private val scoreDetailsLookupService: GolfCanadaScoreDetailsLookupService,
    private val verificationProperties: VerificationProperties
) {

    /**
     * Asynchronously generates an AI review for the given [result].
     *
     * Runs on Spring's task executor thread pool. The returned [CompletableFuture]
     * resolves to the AI-generated review text, or completes exceptionally with
     * an [AiIntegrationException] if the AI call fails.
     *
     * @param result     The completed verification result.
     * @param aiService  The active [AiIntegrationService] to use for generation.
     */
    @Async
    fun reviewAsync(result: FileVerificationResult, aiService: AiIntegrationService): CompletableFuture<String> {
        val maxRounds = verificationProperties.maxRounds
        val scoreDetailsList = fetchScoreDetails(result, maxRounds)
        val request = AiReviewRequest(
            verificationResult = result,
            scoreDetails = scoreDetailsList,
            maxRounds = maxRounds
        )
        return CompletableFuture.completedFuture(aiService.generate(request))
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun fetchScoreDetails(result: FileVerificationResult, maxRounds: Int): List<ScoreDetails> {
        val individualId = result.memberProfile.profile?.individualId ?: return emptyList()

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
}
