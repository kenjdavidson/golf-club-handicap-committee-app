package com.kenjdavidson.golf.handicap.components

import java.time.Instant

data class LogMessage(
    val timestamp: Instant,
    val message: String,
    val stackTrace: String? = null
)
