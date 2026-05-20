package com.kenjdavidson.golf.handicap.components

import com.vaadin.flow.component.html.Span

fun Span.bindText(signal: ValueSignal<String>) {
    var unsubscribe: (() -> Unit)? = null
    unsubscribe = signal.subscribe { message ->
        val currentUi = ui.orElse(null)
        if (currentUi == null || currentUi.session == null) {
            text = message
        } else {
            currentUi.access {
                text = message
            }
        }
    }
    addDetachListener {
        unsubscribe?.invoke()
        unsubscribe = null
    }
}
