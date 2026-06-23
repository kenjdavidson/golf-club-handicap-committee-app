package com.kenjdavidson.golf.handicap.ai

/**
 * Describes a model available for use with Ollama.
 *
 * @param tag           The Ollama pull tag (e.g. "llama3.2:1b").
 * @param displayNameKey i18n message key for a human-readable name.
 * @param descriptionKey i18n message key for a short description.
 * @param estimatedSizeGb Rough download size in gigabytes shown in the UI.
 */
data class OllamaModelOption(
    val tag: String,
    val displayNameKey: String,
    val descriptionKey: String,
    val estimatedSizeGb: Double
) {
    companion object {
        val ALL: List<OllamaModelOption> = listOf(
            OllamaModelOption(
                tag = "llama3.2:1b",
                displayNameKey = "ai.model.llama32_1b.name",
                descriptionKey = "ai.model.llama32_1b.description",
                estimatedSizeGb = 0.7
            ),
            OllamaModelOption(
                tag = "llama3.2",
                displayNameKey = "ai.model.llama32.name",
                descriptionKey = "ai.model.llama32.description",
                estimatedSizeGb = 2.0
            ),
            OllamaModelOption(
                tag = "mistral",
                displayNameKey = "ai.model.mistral.name",
                descriptionKey = "ai.model.mistral.description",
                estimatedSizeGb = 4.1
            ),
            OllamaModelOption(
                tag = "phi4-mini",
                displayNameKey = "ai.model.phi4mini.name",
                descriptionKey = "ai.model.phi4mini.description",
                estimatedSizeGb = 2.5
            ),
            OllamaModelOption(
                tag = "gemma3:1b",
                displayNameKey = "ai.model.gemma3_1b.name",
                descriptionKey = "ai.model.gemma3_1b.description",
                estimatedSizeGb = 0.8
            )
        )
    }
}
