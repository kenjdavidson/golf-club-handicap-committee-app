package com.kenjdavidson.golf.handicap.ai

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Externalized configuration for the Ollama HTTP integration.
 *
 * The base URL defaults to the standard Ollama port on localhost and can be
 * overridden via the `app.ai.ollama.base-url` property (or the matching
 * environment variable `APP_AI_OLLAMA_BASE_URL`).
 */
@Component
class OllamaProperties(
    @Value("\${app.ai.ollama.base-url:http://localhost:11434}")
    val baseUrl: String
)
