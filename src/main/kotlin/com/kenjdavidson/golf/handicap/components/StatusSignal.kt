package com.kenjdavidson.golf.handicap.components

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
        currentSubscribers.forEach { it(statusText) }
    }
}
