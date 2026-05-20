package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.views.UserProfileResolver
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class StatusBar(
    private val authenticationContext: AuthenticationContext,
    userProfileResolver: UserProfileResolver
) : HorizontalLayout() {
    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)

    private val status = Span("Status: Ready").apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }

    private val context = Span("Logged in as ${authenticatedUser.displayName}").apply {
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

    fun bind(statusSignal: StatusSignal) {
        statusSignal.subscribe { statusText ->
            updateStatusSafely(statusText)
        }
    }

    private fun updateStatusSafely(statusText: String) {
        val currentUi = ui.orElse(null)
        if (currentUi == null || currentUi.session == null) {
            return
        }
        currentUi.access {
            updateStatus(statusText)
        }
    }
}
