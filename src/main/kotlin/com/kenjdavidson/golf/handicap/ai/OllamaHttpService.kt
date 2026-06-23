package com.kenjdavidson.golf.handicap.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
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

    private val objectMapper = ObjectMapper()

    override fun generate(prompt: String): String {
        log.debug("Sending prompt to Ollama [{}] model: {}", modelTag, prompt.take(80))
        return try {
            val request = GenerateRequest(model = modelTag, prompt = prompt, stream = false)
            val response = restClient.post()
                .uri("/api/generate")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GenerateResponse::class.java)
                ?: throw OllamaServiceException("Ollama returned an empty response for model '$modelTag'.")
            response.response
        } catch (ex: RestClientException) {
            throw OllamaServiceException("Failed to communicate with Ollama at $baseUrl: ${ex.message}", ex)
        }
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
