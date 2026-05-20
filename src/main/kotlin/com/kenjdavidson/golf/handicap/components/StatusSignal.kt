package com.kenjdavidson.golf.handicap.components

import org.slf4j.LoggerFactory

class StatusSignal(initialStatus: String) {
    private var status = initialStatus
    private val subscribers = mutableListOf<(String) -> Unit>()

    fun subscribe(subscriber: (String) -> Unit) {
        val currentStatus = synchronized(this) {
            subscribers += subscriber
            status
        }
        subscriber(currentStatus)
    }

    fun publish(statusText: String) {
        val currentSubscribers = synchronized(this) {
            status = statusText
            subscribers.toList()
        }
        currentSubscribers.forEach { subscriber ->
            runCatching { subscriber(statusText) }
                .onFailure { exception ->
                    logger.warn("Status signal subscriber failed for status: {}", statusText, exception)
                }
        }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(StatusSignal::class.java)
    }
}
