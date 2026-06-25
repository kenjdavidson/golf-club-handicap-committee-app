package com.kenjdavidson.golf.handicap.ai

/**
 * Abstraction over configured AI integrations.
 *
 * Callers pass a structured [AiReviewRequest] containing all verification data.
 * Each implementation is responsible for converting the request into the format
 * required by its underlying AI provider and returning the model's response as a
 * plain string. Error handling (connection failures, model unavailability) is left
 * to implementing classes which should throw descriptive runtime exceptions.
 */
interface AiIntegrationService {

    /**
     * Send [request] to the configured model and return the full response text.
     *
     * @throws AiIntegrationException when the request cannot be completed.
     */
    fun generate(request: AiReviewRequest): String

    /** Returns `true` when this service can reach the configured AI endpoint. */
    fun isAvailable(): Boolean
}
