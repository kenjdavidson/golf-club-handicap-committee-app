package com.kenjdavidson.golf.handicap.ai

/**
 * Represents the download state of an Ollama model.
 */
sealed class ModelDownloadState {

    /** No download has been started for this model. */
    data object Idle : ModelDownloadState()

    /**
     * A download is currently in progress.
     *
     * @param statusMessage Human-readable status reported by Ollama.
     * @param progress      Value in [0.0, 1.0] or `-1.0` when indeterminate.
     */
    data class Downloading(
        val statusMessage: String,
        val progress: Double
    ) : ModelDownloadState()

    /** The model was downloaded successfully and is ready to use. */
    data object Complete : ModelDownloadState()

    /**
     * The download failed.
     *
     * @param reason Human-readable explanation of the failure.
     */
    data class Failed(val reason: String) : ModelDownloadState()
}
