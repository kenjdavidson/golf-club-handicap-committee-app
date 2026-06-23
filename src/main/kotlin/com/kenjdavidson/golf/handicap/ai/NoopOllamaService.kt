package com.kenjdavidson.golf.handicap.ai

/**
 * No-op [OllamaService] used when the AI integration type is [AiIntegrationType.NONE].
 *
 * All calls to [generate] throw an [OllamaServiceException] to make accidental invocations
 * visible rather than silently swallowing them.
 */
class NoopOllamaService : OllamaService {

    override fun generate(prompt: String): String =
        throw OllamaServiceException("AI integration is disabled. Select an integration type in Settings → AI Features.")

    override fun isAvailable(): Boolean = false
}
