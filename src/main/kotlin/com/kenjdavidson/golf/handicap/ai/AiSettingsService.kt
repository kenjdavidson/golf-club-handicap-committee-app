package com.kenjdavidson.golf.handicap.ai

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope

/**
 * Session-scoped service that holds the AI settings for the currently
 * authenticated user and exposes a ready-to-use [OllamaService] instance.
 *
 * Settings are loaded from and persisted to the user-home JSON store via
 * [com.kenjdavidson.golf.handicap.settings.UserSettingsService] (also
 * session-scoped), so choices survive browser refreshes within the same
 * application run.
 *
 * The set of integration types available to users is governed by
 * [AiProperties.allowedTypes], which can be restricted via the
 * `APP_AI_ALLOWED_TYPES` environment variable (e.g. `NONE,GEMINI` for a
 * cloud deployment that does not have Ollama/Docker available).
 */
@SessionScope
@Component
class AiSettingsService(
    private val ollamaProperties: OllamaProperties,
    private val geminiProperties: GeminiProperties,
    private val aiProperties: AiProperties = AiProperties("NONE,EXTERNAL,LOCAL,GEMINI")
) {
    @Volatile
    private var integrationTypeValue: AiIntegrationType = AiIntegrationType.NONE

    @Volatile
    private var selectedModelTagValue: String? = null

    @Volatile
    private var geminiApiKeyValue: String? = null

    /**
     * The integration types available for selection in the Settings UI.
     * Derived from [AiProperties.allowedTypes].
     */
    val allowedTypes: List<AiIntegrationType>
        get() = aiProperties.allowedTypes

    var integrationType: AiIntegrationType
        get() = integrationTypeValue
        set(value) {
            integrationTypeValue = value
            rebuildService()
        }

    var selectedModelTag: String?
        get() = selectedModelTagValue
        set(value) {
            selectedModelTagValue = value
            rebuildService()
        }

    var geminiApiKey: String?
        get() = geminiApiKeyValue
        set(value) {
            geminiApiKeyValue = value?.trim()?.takeIf { it.isNotBlank() }
            rebuildService()
        }

    /**
     * The active [OllamaService] for the current session.
     *
     * Rebuilt whenever [integrationType] or [selectedModelTag] changes.
     */
    @Volatile
    private var ollamaServiceValue: OllamaService = NoopOllamaService()

    val ollamaService: OllamaService
        get() = ollamaServiceValue

    @PostConstruct
    fun init() {
        rebuildService()
    }

    /**
     * Applies AI settings atomically without triggering extra rebuilds.
     *
     * If [integrationType] is not in [allowedTypes] (e.g. a persisted Ollama setting
     * loaded on a cloud deployment that only allows Gemini) it is silently reset to
     * [AiIntegrationType.NONE].
     */
    fun applySettings(integrationType: AiIntegrationType, selectedModelTag: String?, geminiApiKey: String? = null) {
        this.integrationTypeValue =
            if (integrationType in aiProperties.allowedTypes) integrationType else AiIntegrationType.NONE
        this.selectedModelTagValue = selectedModelTag
        this.geminiApiKeyValue = geminiApiKey?.trim()?.takeIf { it.isNotBlank() }
        rebuildService()
    }

    // ── Internals ──────────────────────────────────────────────────────────────

    private fun rebuildService() {
        ollamaServiceValue = when (integrationTypeValue) {
            AiIntegrationType.NONE -> NoopOllamaService()
            AiIntegrationType.EXTERNAL,
            AiIntegrationType.LOCAL -> {
                val tag = selectedModelTagValue
                if (tag.isNullOrBlank()) NoopOllamaService()
                else OllamaHttpService(ollamaProperties.baseUrl, tag)
            }
            AiIntegrationType.GEMINI -> {
                // Use the user-entered key; fall back to the operator-configured env key.
                val key = geminiApiKeyValue ?: geminiProperties.apiKey?.takeIf { it.isNotBlank() }
                if (key.isNullOrBlank()) NoopOllamaService()
                else GeminiHttpService(
                    baseUrl = geminiProperties.baseUrl,
                    model = geminiProperties.model,
                    apiKey = key,
                    temperature = geminiProperties.temperature
                )
            }
        }
    }
}
