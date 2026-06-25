package com.kenjdavidson.golf.handicap.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

class GeminiHttpService(
    private val baseUrl: String,
    private val model: String,
    private val apiKey: String,
    private val temperature: Double
) : OllamaService {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()

    private val requestConfig = RequestConfig(
        systemInstruction = Content(
            parts = listOf(
                TextPart(
                    text = DEFAULT_SYSTEM_INSTRUCTION
                )
            )
        ),
        temperature = temperature
    )

    override fun generate(request: AiReviewRequest): String {
        val response = try {
            restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    GenerateContentRequest(
                        contents = listOf(
                            Content(parts = listOf(TextPart(text = buildUserContent(request))))
                        ),
                        config = requestConfig
                    )
                )
                .retrieve()
                .body(GenerateContentResponse::class.java)
        } catch (ex: RestClientException) {
            throw AiIntegrationException("Failed to communicate with Gemini at $baseUrl: ${ex.message}", ex)
        }

        return response?.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.takeIf { it.isNotBlank() }
            ?: throw AiIntegrationException("Gemini returned an empty response for model '$model'.")
    }

    private fun buildUserContent(request: AiReviewRequest): String = buildString {
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
            restClient.get()
                .uri("/v1beta/models?key={apiKey}", apiKey)
                .retrieve()
                .toBodilessEntity()
                .statusCode
                .is2xxSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private data class GenerateContentRequest(
        val contents: List<Content>,
        val config: RequestConfig
    )

    private data class RequestConfig(
        val systemInstruction: Content,
        val temperature: Double
    )

    private data class Content(
        val parts: List<TextPart>
    )

    private data class TextPart(
        val text: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class GenerateContentResponse(
        val candidates: List<Candidate> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Candidate(
        val content: ResponseContent = ResponseContent()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ResponseContent(
        val parts: List<ResponsePart> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ResponsePart(
        val text: String = ""
    )

    companion object {
        private const val DEFAULT_SYSTEM_INSTRUCTION =
            "You are an expert Golf Club Manager and head of the Handicap Committee. Your core responsibility is protecting the integrity of the club's handicap system. Analyze user scoring data for sandbagging, vanity handicapping, and anomalies."
    }
}
