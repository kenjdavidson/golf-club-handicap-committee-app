package com.kenjdavidson.golf.handicap.ai

/**
 * Abstraction over different Ollama integration modes.
 *
 * Callers pass a plain-text [prompt] and receive the model's response as a
 * plain string.  Error handling (connection failures, model unavailability)
 * is left to implementing classes which should throw descriptive runtime
 * exceptions.
 */
interface OllamaService {

    /**
     * Send [prompt] to the configured model and return the full response text.
     *
     * @throws OllamaServiceException when the request cannot be completed.
     */
    fun generate(prompt: String): String

    /** Returns `true` when this service can actually reach an Ollama endpoint. */
    fun isAvailable(): Boolean
}

class OllamaServiceException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
