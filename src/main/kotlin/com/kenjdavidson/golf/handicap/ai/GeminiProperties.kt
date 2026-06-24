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
    val temperature: Double
) {
    init {
        require(temperature in 0.0..2.0) {
            "Gemini temperature must be between 0.0 and 2.0 (configured via app.ai.gemini.temperature)"
        }
    }
}
