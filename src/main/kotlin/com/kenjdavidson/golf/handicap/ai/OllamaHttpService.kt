package com.kenjdavidson.golf.handicap.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

/**
 * [OllamaService] implementation that communicates with an Ollama instance over HTTP.
 *
 * Used for both [AiIntegrationType.EXTERNAL] (user-managed Docker container) and
 * [AiIntegrationType.LOCAL] (locally installed Ollama managed by this application).
 *
 * @param baseUrl       Base URL of the Ollama HTTP API (e.g. `http://localhost:11434`).
 * @param modelTag      The Ollama model tag to use for generation (e.g. `llama3.2:1b`).
 */
class OllamaHttpService(
    private val baseUrl: String,
    private val modelTag: String
) : OllamaService {

    private val log = LoggerFactory.getLogger(OllamaHttpService::class.java)

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()

    override fun generate(request: AiReviewRequest): String {
        val prompt = buildPrompt(request)
        log.debug("Sending prompt to Ollama [{}] model: {}", modelTag, prompt.take(80))
        return try {
            val generateRequest = GenerateRequest(model = modelTag, prompt = prompt, stream = false)
            val response = restClient.post()
                .uri("/api/generate")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(generateRequest)
                .retrieve()
                .body(GenerateResponse::class.java)
                ?: throw AiIntegrationException("Ollama returned an empty response for model '$modelTag'.")
            response.response
        } catch (ex: RestClientException) {
            throw AiIntegrationException("Failed to communicate with Ollama at $baseUrl: ${ex.message}", ex)
        }
    }

    private fun buildPrompt(request: AiReviewRequest): String = buildString {
        val result = request.verificationResult
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

        appendLine("=== RECENT GOLF CANADA SCORE DETAILS (last ${request.maxRounds} rounds) ===")
        if (request.scoreDetails.isEmpty()) {
            appendLine("No detailed score records available.")
        } else {
            request.scoreDetails.forEachIndexed { index, details ->
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

    override fun isAvailable(): Boolean {
        return try {
            val status = restClient.get()
                .uri("/")
                .retrieve()
                .toBodilessEntity()
                .statusCode
            status.is2xxSuccessful
        } catch (ex: Exception) {
            log.debug("Ollama availability check failed: {}", ex.message)
            false
        }
    }

    // ── Internal request/response models ──────────────────────────────────────

    private data class GenerateRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class GenerateResponse(
        val response: String = ""
    )
}
