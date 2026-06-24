package com.kenjdavidson.golf.handicap.ai

@Deprecated("Use AiIntegrationException instead.")
class OllamaServiceException(message: String, cause: Throwable? = null) :
    AiIntegrationException(message, cause)
