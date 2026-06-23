package com.kenjdavidson.golf.handicap.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OllamaModelDownloadServiceTest {

    private val properties = OllamaProperties("http://localhost:11434")

    @Test
    fun `initial state for any model is Idle`() {
        val service = OllamaModelDownloadService(properties)
        assertEquals(ModelDownloadState.Idle, service.getState("llama3.2:1b"))
    }

    @Test
    fun `addListener is called immediately with current state`() {
        val service = OllamaModelDownloadService(properties)
        val received = mutableListOf<ModelDownloadState>()

        service.addListener("llama3.2:1b") { _, state -> received.add(state) }

        assertEquals(1, received.size)
        assertEquals(ModelDownloadState.Idle, received.first())
    }

    @Test
    fun `removeListener stops receiving notifications`() {
        val service = OllamaModelDownloadService(properties)
        val received = mutableListOf<ModelDownloadState>()
        val listener = OllamaModelDownloadService.StateChangeListener { _, state -> received.add(state) }

        service.addListener("llama3.2", listener)
        service.removeListener("llama3.2", listener)

        // No further calls expected (beyond the initial immediate call)
        val countAfterRemove = received.size
        service.cancelDownload("llama3.2") // triggers a state change
        assertEquals(countAfterRemove, received.size)
    }

    @Test
    fun `cancelDownload resets state to Idle`() {
        val service = OllamaModelDownloadService(properties)
        val received = mutableListOf<ModelDownloadState>()
        service.addListener("mistral") { _, state -> received.add(state) }

        service.cancelDownload("mistral")

        // Last state should be Idle
        assertEquals(ModelDownloadState.Idle, received.last())
    }

    @Test
    fun `multiple listeners receive state changes`() {
        val service = OllamaModelDownloadService(properties)
        val first = mutableListOf<ModelDownloadState>()
        val second = mutableListOf<ModelDownloadState>()

        service.addListener("llama3.2:1b") { _, s -> first.add(s) }
        service.addListener("llama3.2:1b") { _, s -> second.add(s) }

        service.cancelDownload("llama3.2:1b")

        // Both should have received the state change
        assertTrue(first.size >= 2)
        assertTrue(second.size >= 2)
    }

    @Test
    fun `ModelDownloadState Downloading with positive progress`() {
        val state = ModelDownloadState.Downloading("pulling ggml model", 0.42)
        assertEquals(0.42, state.progress)
        assertEquals("pulling ggml model", state.statusMessage)
    }

    @Test
    fun `ModelDownloadState Downloading with indeterminate progress`() {
        val state = ModelDownloadState.Downloading("pulling manifest", -1.0)
        assertTrue(state.progress < 0)
        assertTrue(state is ModelDownloadState.Downloading)
    }
}
