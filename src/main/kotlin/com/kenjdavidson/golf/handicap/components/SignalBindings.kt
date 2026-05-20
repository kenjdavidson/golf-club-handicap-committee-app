package com.kenjdavidson.golf.handicap.components

import com.vaadin.flow.component.html.Span
import com.vaadin.signals.Signal
import com.vaadin.signals.ValueSignal

fun Span.bindText(signal: ValueSignal<String>): Runnable {
    return Signal.effect {
        val message = signal.value()
        val currentUi = ui.orElse(null)
        if (currentUi == null || currentUi.session == null) {
            text = message
        } else {
            currentUi.access {
                text = message
            }
        }
    }
}
