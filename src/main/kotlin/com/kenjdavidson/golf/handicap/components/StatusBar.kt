package com.kenjdavidson.golf.handicap.components

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.kenjdavidson.golf.handicap.views.MessagesView
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
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
    userProfileResolver: UserProfileResolver,
    private val loggingMessageService: LoggingMessageService
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

    private val errorIndicator = Button(VaadinIcon.EXCLAMATION_CIRCLE_O.create()).apply {
        addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR)
        isVisible = false
        addClickListener {
            UI.getCurrent()?.navigate(MessagesView::class.java)
        }
    }

    init {
        status.bindText(statusSignal)
        add(status, errorIndicator, context)
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
        refreshErrorIndicator(AppMessages.resolveLocale())
    }

    override fun localeChange(event: LocaleChangeEvent) {
        context.text = AppMessages.translate(event.locale, "status.loggedInAs", authenticatedUser.displayName)
        refreshErrorIndicator(event.locale)
    }

    fun updateStatus(statusText: String) {
        statusSignal.set(statusText)
    }

    @EventListener
    fun onStatusUpdate(event: StatusUpdateEvent) {
        updateStatus(event.message)
    }

    @EventListener
    fun onErrorLogged(event: ErrorLoggedEvent) {
        refreshErrorIndicator(AppMessages.resolveLocale())
    }

    private fun refreshErrorIndicator(locale: java.util.Locale) {
        val count = loggingMessageService.getMessageCount()
        val label = AppMessages.translate(locale, "status.viewMessages", count)
        errorIndicator.isVisible = count > 0
        errorIndicator.element.setAttribute("aria-label", label)
        errorIndicator.element.setAttribute("title", label)
    }
}
