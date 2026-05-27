package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.signals.local.ValueSignal
import com.vaadin.flow.spring.security.AuthenticationContext
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class StatusBar(
    authenticationContext: AuthenticationContext,
    userProfileResolver: UserProfileResolver
) : HorizontalLayout(), LocaleChangeObserver {
    private val authenticatedUser = userProfileResolver.resolveAuthenticatedUser(authenticationContext)
    private val statusSignal = ValueSignal(AppMessages.translateCurrent("status.ready"))

    private val status = Span().apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }

    private val context = Span(
        AppMessages.translateCurrent("status.loggedInAs", authenticatedUser.displayName)
    ).apply {
        addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY
        )
    }

    init {
        status.bindText(statusSignal)
        add(status, context)
        setWidthFull()
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        expand(status)
        addClassNames(
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.SMALL
        )
        style["background"] = "var(--lumo-base-color)"
        style["border-top"] = "1px solid var(--lumo-contrast-10pct)"
        style["flex-shrink"] = "0"
    }

    override fun localeChange(event: LocaleChangeEvent) {
        context.text = AppMessages.translate(event.locale, "status.loggedInAs", authenticatedUser.displayName)
    }

    fun updateStatus(statusText: String) {
        statusSignal.set(statusText)
    }

    @EventListener
    fun onStatusUpdate(event: StatusUpdateEvent) {
        updateStatus(event.message)
    }
}
