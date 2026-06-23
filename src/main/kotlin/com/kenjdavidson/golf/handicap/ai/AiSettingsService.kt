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
    private val ollamaProperties: OllamaProperties
) {
    @Volatile
    private var integrationTypeValue: AiIntegrationType = AiIntegrationType.NONE

    @Volatile
    private var selectedModelTagValue: String? = null

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

    /** Applies [integrationType] and [selectedModelTag] atomically without triggering extra rebuilds. */
    fun applySettings(integrationType: AiIntegrationType, selectedModelTag: String?) {
        this.integrationTypeValue = integrationType
        this.selectedModelTagValue = selectedModelTag
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
        }
    }
}
