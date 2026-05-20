package com.kenjdavidson.golf.handicap.verification

class VerificationProcessingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
