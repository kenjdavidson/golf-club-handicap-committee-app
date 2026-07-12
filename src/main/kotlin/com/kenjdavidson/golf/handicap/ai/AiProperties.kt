package com.kenjdavidson.golf.handicap.ai

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Global AI configuration that controls which integration types are made available
 * to users in the Settings UI.
 *
 * This is intended to be configured via environment variables or command-line
 * properties when deploying to cloud environments where certain integrations
 * (e.g. Ollama / Docker) are not available.
 *
 * **Property:** `app.ai.allowed-types`
 * **Env var:** `APP_AI_ALLOWED_TYPES`
 * **Default:** all types (`NONE,EXTERNAL,LOCAL,GEMINI`)
 *
 * Example – cloud deployment that only allows Gemini:
 * ```
 * APP_AI_ALLOWED_TYPES=NONE,GEMINI
 * ```
 *
 * [AiIntegrationType.NONE] is always included so users can disable AI features,
 * even if it is omitted from the configured list.
 */
@Component
class AiProperties(
    @Value("\${app.ai.allowed-types:NONE,EXTERNAL,LOCAL,GEMINI}")
    private val allowedTypesRaw: String
) {
    /**
     * The ordered list of [AiIntegrationType] values that are available to users.
     * Unknown or mis-spelled tokens are silently ignored.
     */
    val allowedTypes: List<AiIntegrationType> = allowedTypesRaw
        .split(",")
        .mapNotNull { token ->
            runCatching { AiIntegrationType.valueOf(token.trim().uppercase()) }.getOrNull()
        }
        .distinct()
        .let { types ->
            // NONE must always be present so users can turn off AI features.
            if (AiIntegrationType.NONE !in types) listOf(AiIntegrationType.NONE) + types else types
        }
}
