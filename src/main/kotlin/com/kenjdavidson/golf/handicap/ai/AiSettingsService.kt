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
 */
@SessionScope
@Component
class AiSettingsService(
    private val ollamaProperties: OllamaProperties,
    private val geminiProperties: GeminiProperties = GeminiProperties(
        baseUrl = "https://generativelanguage.googleapis.com",
        model = "gemini-2.5-flash"
    )
) {
    @Volatile
    private var integrationTypeValue: AiIntegrationType = AiIntegrationType.NONE

    @Volatile
    private var selectedModelTagValue: String? = null

    @Volatile
    private var geminiApiKeyValue: String? = null

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

    /** Applies AI settings atomically without triggering extra rebuilds. */
    fun applySettings(integrationType: AiIntegrationType, selectedModelTag: String?, geminiApiKey: String? = null) {
        this.integrationTypeValue = integrationType
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
                val key = geminiApiKeyValue
                if (key.isNullOrBlank()) NoopOllamaService()
                else GeminiHttpService(geminiProperties.baseUrl, geminiProperties.model, key)
            }
        }
    }
}
