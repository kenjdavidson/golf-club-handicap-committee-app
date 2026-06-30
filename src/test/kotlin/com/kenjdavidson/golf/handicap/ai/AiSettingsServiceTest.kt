package com.kenjdavidson.golf.handicap.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AiSettingsServiceTest {

    private val properties = OllamaProperties("http://localhost:11434")
    private val geminiProperties = GeminiProperties(
        baseUrl = "https://generativelanguage.googleapis.com",
        model = "gemini-2.5-flash",
        temperature = 0.1
    )
    private val aiProperties = AiProperties("NONE,EXTERNAL,LOCAL,GEMINI")
    private val service = AiSettingsService(properties, geminiProperties, aiProperties)

    @Test
    fun `defaults to NONE integration type`() {
        assertEquals(AiIntegrationType.NONE, service.integrationType)
    }

    @Test
    fun `defaults to null selected model`() {
        assertEquals(null, service.selectedModelTag)
    }

    @Test
    fun `defaults to null Gemini API key`() {
        assertEquals(null, service.geminiApiKey)
    }

    @Test
    fun `defaults to NoopOllamaService when NONE`() {
        assertTrue(service.ollamaService is NoopOllamaService)
    }

    @Test
    fun `builds OllamaHttpService when EXTERNAL with model selected`() {
        service.applySettings(AiIntegrationType.EXTERNAL, "llama3.2:1b")

        assertTrue(service.ollamaService is OllamaHttpService)
    }

    @Test
    fun `builds OllamaHttpService when LOCAL with model selected`() {
        service.applySettings(AiIntegrationType.LOCAL, "mistral")

        assertTrue(service.ollamaService is OllamaHttpService)
    }

    @Test
    fun `falls back to NoopOllamaService when EXTERNAL but no model`() {
        service.applySettings(AiIntegrationType.EXTERNAL, null)

        assertTrue(service.ollamaService is NoopOllamaService)
    }

    @Test
    fun `falls back to NoopOllamaService when LOCAL but blank model`() {
        service.applySettings(AiIntegrationType.LOCAL, "  ")

        assertTrue(service.ollamaService is NoopOllamaService)
    }

    @Test
    fun `rebuilds service when integration type changes back to NONE`() {
        service.applySettings(AiIntegrationType.LOCAL, "llama3.2")
        assertTrue(service.ollamaService is OllamaHttpService)

        service.integrationType = AiIntegrationType.NONE
        assertTrue(service.ollamaService is NoopOllamaService)
    }

    @Test
    fun `rebuilds service when model tag changes`() {
        service.applySettings(AiIntegrationType.LOCAL, "llama3.2:1b")
        val first = service.ollamaService

        service.selectedModelTag = "mistral"
        val second = service.ollamaService

        assertTrue(first is OllamaHttpService)
        assertTrue(second is OllamaHttpService)
        assertNotNull(second)
    }

    @Test
    fun `builds GeminiHttpService when GEMINI with API key`() {
        service.applySettings(
            integrationType = AiIntegrationType.GEMINI,
            selectedModelTag = null,
            geminiApiKey = "gemini-api-key"
        )

        assertTrue(service.ollamaService is GeminiHttpService)
    }

    @Test
    fun `falls back to NoopOllamaService when GEMINI without API key`() {
        service.applySettings(
            integrationType = AiIntegrationType.GEMINI,
            selectedModelTag = null,
            geminiApiKey = " "
        )

        assertTrue(service.ollamaService is NoopOllamaService)
    }

    @Test
    fun `uses operator-configured Gemini API key when no user key is set`() {
        val propsWithKey = GeminiProperties(
            baseUrl = "https://generativelanguage.googleapis.com",
            model = "gemini-2.5-flash",
            temperature = 0.1,
            apiKey = "operator-key"
        )
        val svc = AiSettingsService(properties, propsWithKey, aiProperties)

        svc.applySettings(integrationType = AiIntegrationType.GEMINI, selectedModelTag = null, geminiApiKey = null)

        assertTrue(svc.ollamaService is GeminiHttpService)
    }

    @Test
    fun `user-entered Gemini API key overrides operator key`() {
        val propsWithKey = GeminiProperties(
            baseUrl = "https://generativelanguage.googleapis.com",
            model = "gemini-2.5-flash",
            temperature = 0.1,
            apiKey = "operator-key"
        )
        val svc = AiSettingsService(properties, propsWithKey, aiProperties)

        svc.applySettings(integrationType = AiIntegrationType.GEMINI, selectedModelTag = null, geminiApiKey = "user-key")

        assertTrue(svc.ollamaService is GeminiHttpService)
        assertEquals("user-key", svc.geminiApiKey)
    }

    @Test
    fun `exposes allowed types from AiProperties`() {
        assertEquals(listOf(AiIntegrationType.NONE, AiIntegrationType.EXTERNAL, AiIntegrationType.LOCAL, AiIntegrationType.GEMINI), service.allowedTypes)
    }

    @Test
    fun `resets disallowed integration type to NONE on applySettings`() {
        val restrictedProps = AiProperties("NONE,GEMINI")
        val svc = AiSettingsService(properties, geminiProperties, restrictedProps)

        svc.applySettings(integrationType = AiIntegrationType.LOCAL, selectedModelTag = "llama3.2")

        assertEquals(AiIntegrationType.NONE, svc.integrationType)
        assertTrue(svc.ollamaService is NoopOllamaService)
    }

    @Test
    fun `NONE is always included in allowedTypes even if omitted from config`() {
        val props = AiProperties("GEMINI")

        assertTrue(AiIntegrationType.NONE in props.allowedTypes)
        assertTrue(AiIntegrationType.GEMINI in props.allowedTypes)
    }

    @Test
    fun `NoopOllamaService is not available`() {
        assertFalse(service.ollamaService.isAvailable())
    }

    @Test
    fun `NoopOllamaService generate throws AiIntegrationException`() {
        val request = AiReviewRequest(
            verificationResult = com.kenjdavidson.golf.handicap.verification.FileVerificationResult(
                memberProfile = com.kenjdavidson.golf.handicap.verification.MemberProfile(
                    fullName = "Test",
                    cardId = null,
                    isMatched = false,
                    profile = null
                ),
                status = com.kenjdavidson.golf.handicap.verification.VerificationStatus.PASS,
                matchPercentage = 0,
                matchedCount = 0,
                comparedCount = 0,
                mismatchedDates = emptyList(),
                notes = emptyList()
            )
        )
        val ex = runCatching { service.ollamaService.generate(request) }.exceptionOrNull()
        assertTrue(ex is AiIntegrationException)
    }
}
