package com.kenjdavidson.golf.handicap.components

import com.vaadin.flow.component.Component
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class UiComponentUpdater {
    fun update(component: Component, update: () -> Unit) {
        val currentUi = component.ui.orElse(null)
        if (currentUi == null || currentUi.session == null) {
            return
        }
        currentUi.access {
            update()
        }
    }
}
