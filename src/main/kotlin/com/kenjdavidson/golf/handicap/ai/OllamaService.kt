package com.kenjdavidson.golf.handicap.ai

/**
 * Backward-compatible alias for existing Ollama-oriented call sites.
 *
 * New code should depend on [AiIntegrationService].
 */
interface OllamaService : AiIntegrationService
