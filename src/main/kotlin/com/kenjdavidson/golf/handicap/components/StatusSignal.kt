package com.kenjdavidson.golf.handicap.components

import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class StatusSignal(initialStatus: String) {
    private val status = AtomicReference(initialStatus)
    private val subscribers = CopyOnWriteArrayList<(String) -> Unit>()

    fun subscribe(subscriber: (String) -> Unit): () -> Unit {
        subscribers += subscriber
        subscriber(status.get())
        return { subscribers.remove(subscriber) }
    }

    fun publish(statusText: String) {
        status.set(statusText)
        subscribers.forEach { subscriber ->
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
