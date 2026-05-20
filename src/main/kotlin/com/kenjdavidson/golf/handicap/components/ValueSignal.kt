package com.kenjdavidson.golf.handicap.components

class ValueSignal<T>(initialValue: T) {
    private var value: T = initialValue
    private val subscribers = mutableListOf<(T) -> Unit>()

    fun subscribe(subscriber: (T) -> Unit): () -> Unit {
        val currentValue = synchronized(this) {
            subscribers += subscriber
            value
        }
        subscriber(currentValue)
        return {
            synchronized(this) {
                subscribers.remove(subscriber)
            }
        }
    }

    fun set(newValue: T) {
        val currentSubscribers = synchronized(this) {
            value = newValue
            subscribers.toList()
        }
        currentSubscribers.forEach { it(newValue) }
    }
}
