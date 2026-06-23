package com.kenjdavidson.golf.handicap.ai

enum class AiIntegrationType {
    /** AI features are disabled. */
    NONE,

    /**
     * An externally managed Ollama instance (e.g. a Docker container running alongside
     * this application).  The application connects to it via HTTP but does not manage
     * its lifecycle or model downloads.
     */
    EXTERNAL,

    /**
     * A locally installed Ollama instance managed by this application.  The application
     * can download models on demand through Ollama's pull API and track download progress.
     */
    LOCAL
}
