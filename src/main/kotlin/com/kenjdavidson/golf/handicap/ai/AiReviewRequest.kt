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
