package com.kenjdavidson.golf.handicap.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Application-scoped service that manages Ollama model downloads.
 *
 * Downloads are executed on a background thread pool so the Vaadin UI thread
 * is never blocked.  Callers supply a [StateChangeListener] callback that is
 * invoked on every state transition; it is the caller's responsibility to
 * marshal updates onto the Vaadin UI thread via `UI.access { ... }` before
 * mutating any Vaadin components.
 */
@Service
class OllamaModelDownloadService(
    private val ollamaProperties: OllamaProperties
) {
    private val log = LoggerFactory.getLogger(OllamaModelDownloadService::class.java)

    private val objectMapper = ObjectMapper()
    private val threadCounter = java.util.concurrent.atomic.AtomicInteger(0)
    private val executor = Executors.newCachedThreadPool { r ->
        val count = threadCounter.incrementAndGet()
        Thread(r, "ollama-download-$count").also { it.isDaemon = true }
    }

    /** Latest known state for each model tag. */
    private val states = ConcurrentHashMap<String, ModelDownloadState>()

    /** Active download futures – used for cancellation. */
    private val activeFutures = ConcurrentHashMap<String, Future<*>>()

    /** Registered listeners per model tag.  Multiple views may listen simultaneously. */
    private val listeners = ConcurrentHashMap<String, MutableList<StateChangeListener>>()

    // ── Public API ─────────────────────────────────────────────────────────────

    fun interface StateChangeListener {
        fun onStateChange(modelTag: String, state: ModelDownloadState)
    }

    /** Returns the last known [ModelDownloadState] for [modelTag]. */
    fun getState(modelTag: String): ModelDownloadState =
        states.getOrDefault(modelTag, ModelDownloadState.Idle)

    /**
     * Registers [listener] to receive state-change notifications for [modelTag].
     * The listener is called immediately with the current state.
     */
    fun addListener(modelTag: String, listener: StateChangeListener) {
        listeners.getOrPut(modelTag) { mutableListOf() }.add(listener)
        listener.onStateChange(modelTag, getState(modelTag))
    }

    /** Removes a previously registered [listener] for [modelTag]. */
    fun removeListener(modelTag: String, listener: StateChangeListener) {
        listeners[modelTag]?.remove(listener)
    }

    /**
     * Starts downloading [modelTag] from the configured Ollama endpoint.
     *
     * If a download for the same model is already in progress this call is a no-op.
     */
    fun startDownload(modelTag: String) {
        if (activeFutures.containsKey(modelTag)) {
            log.debug("Download already in progress for model '{}'", modelTag)
            return
        }

        val future = executor.submit {
            runDownload(modelTag)
        }
        activeFutures[modelTag] = future
    }

    /** Cancels an in-progress download for [modelTag] if one exists. */
    fun cancelDownload(modelTag: String) {
        activeFutures.remove(modelTag)?.cancel(true)
        updateState(modelTag, ModelDownloadState.Idle)
    }

    // ── Download execution ─────────────────────────────────────────────────────

    private fun runDownload(modelTag: String) {
        updateState(modelTag, ModelDownloadState.Downloading("Connecting…", -1.0))

        try {
            val url = URI("${ollamaProperties.baseUrl}/api/pull").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/x-ndjson")
            connection.connectTimeout = 10_000
            connection.readTimeout = 0 // streaming – no read timeout

            val body = """{"model":"$modelTag","stream":true}""".toByteArray()
            connection.outputStream.use { it.write(body) }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP ${connection.responseCode}"
                updateState(modelTag, ModelDownloadState.Failed("Ollama pull failed: $error"))
                return
            }

            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (Thread.currentThread().isInterrupted) break
                    val event = parsePullEvent(line!!) ?: continue
                    val newState = toDownloadState(modelTag, event)
                    updateState(modelTag, newState)
                    if (newState is ModelDownloadState.Complete || newState is ModelDownloadState.Failed) break
                }
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            updateState(modelTag, ModelDownloadState.Idle)
        } catch (ex: Exception) {
            log.error("Error downloading model '{}'", modelTag, ex)
            updateState(modelTag, ModelDownloadState.Failed("Download error: ${ex.message}"))
        } finally {
            activeFutures.remove(modelTag)
        }
    }

    private fun toDownloadState(modelTag: String, event: PullEvent): ModelDownloadState {
        if (event.status == "success") return ModelDownloadState.Complete

        val progress: Double = when {
            event.total != null && event.total > 0 && event.completed != null ->
                event.completed.toDouble() / event.total.toDouble()
            else -> -1.0
        }
        return ModelDownloadState.Downloading(event.status, progress)
    }

    private fun parsePullEvent(line: String): PullEvent? {
        if (line.isBlank()) return null
        return try {
            objectMapper.readValue(line, PullEvent::class.java)
        } catch (ex: Exception) {
            log.trace("Could not parse pull event line: {}", line)
            null
        }
    }

    private fun updateState(modelTag: String, state: ModelDownloadState) {
        states[modelTag] = state
        listeners[modelTag]?.toList()?.forEach { listener ->
            try {
                listener.onStateChange(modelTag, state)
            } catch (ex: Exception) {
                log.debug("Listener error for model '{}': {}", modelTag, ex.message)
            }
        }
    }

    // ── Ollama NDJSON event model ──────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class PullEvent(
        val status: String = "",
        val digest: String? = null,
        val total: Long? = null,
        val completed: Long? = null
    )
}
