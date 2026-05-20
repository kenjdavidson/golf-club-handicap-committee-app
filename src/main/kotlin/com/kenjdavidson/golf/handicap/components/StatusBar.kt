package com.kenjdavidson.golf.handicap.components

import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class StatusBar : HorizontalLayout() {

    private val status = Span("Status: Ready").apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }

    private val context = Span("Authentication active with Golf Canada").apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }

    init {
        add(status, context)
        setWidthFull()
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(status)
        addClassNames(
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.SMALL
        )
        style["border-top"] = "1px solid var(--lumo-contrast-10pct)"
    }

    fun updateStatus(statusText: String) {
        status.text = statusText
    }

    @EventListener
    fun onStatusUpdate(event: StatusUpdateEvent) {
        runInUiContext {
            updateStatus(event.statusText)
        }
    }

    private fun runInUiContext(update: () -> Unit) {
        val currentUi = ui.orElse(null)
        if (currentUi == null || currentUi.session == null) {
            return
        }
        currentUi.access {
            update()
        }
    }
}
