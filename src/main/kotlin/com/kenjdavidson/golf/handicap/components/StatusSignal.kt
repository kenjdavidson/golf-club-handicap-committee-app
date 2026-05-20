package com.kenjdavidson.golf.handicap.components

class StatusSignal(initialStatus: String) {
    private var status = initialStatus
    private val subscribers = mutableListOf<(String) -> Unit>()

    fun subscribe(subscriber: (String) -> Unit) {
        subscribers += subscriber
        subscriber(status)
    }

    fun publish(statusText: String) {
        status = statusText
        subscribers.forEach { it(statusText) }
    }
}
