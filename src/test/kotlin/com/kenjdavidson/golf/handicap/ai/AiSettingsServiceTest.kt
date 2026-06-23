package com.kenjdavidson.golf.handicap.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AiSettingsServiceTest {

    private val properties = OllamaProperties("http://localhost:11434")
    private val service = AiSettingsService(properties)

    @Test
    fun `defaults to NONE integration type`() {
        assertEquals(AiIntegrationType.NONE, service.integrationType)
    }

    @Test
    fun `defaults to null selected model`() {
        assertEquals(null, service.selectedModelTag)
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
    fun `NoopOllamaService is not available`() {
        assertFalse(service.ollamaService.isAvailable())
    }

    @Test
    fun `NoopOllamaService generate throws OllamaServiceException`() {
        val ex = runCatching { service.ollamaService.generate("hello") }.exceptionOrNull()
        assertTrue(ex is OllamaServiceException)
    }
}
