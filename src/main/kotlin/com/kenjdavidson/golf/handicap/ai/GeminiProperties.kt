package com.kenjdavidson.golf.handicap.ai

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GeminiProperties(
    @Value("\${app.ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    val baseUrl: String,
    @Value("\${app.ai.gemini.model:gemini-2.5-flash}")
    val model: String,
    @Value("\${app.ai.gemini.temperature:0.1}")
    val temperature: Double,
    /**
     * Optional Gemini API key configured at the application level.
     *
     * **Property:** `app.ai.gemini.api-key`
     * **Env var:** `APP_AI_GEMINI_API_KEY`
     *
     * When set, this key is used as a fallback if the user has not entered
     * their own key in Settings. Useful for cloud deployments where the
     * operator pre-configures a shared key.
     */
    @Value("\${app.ai.gemini.api-key:#{null}}")
    val apiKey: String? = null
) {
    init {
        require(temperature in 0.0..2.0) {
            "Gemini temperature must be in the range [0.0, 2.0]; configured value via app.ai.gemini.temperature is invalid."
        }
    }
}
