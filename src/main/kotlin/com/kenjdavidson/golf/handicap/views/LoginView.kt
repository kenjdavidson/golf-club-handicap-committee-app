package com.kenjdavidson.golf.handicap.views

import com.kenjdavidson.golf.handicap.i18n.AppMessages
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.theme.lumo.Lumo
import com.vaadin.flow.theme.lumo.LumoUtility
import org.springframework.beans.factory.annotation.Value

@Route("login")
@PageTitle("Login | Handicap Committee App")
@AnonymousAllowed
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("context://styles/global.css")
class LoginView(
    @Value("\${app.ui.title:Handicap Committee App}") private val appTitle: String,
) : VerticalLayout(), BeforeEnterObserver, LocaleChangeObserver {
    private val loginForm = LoginForm()
    private val heading = H2("⛳ $appTitle").apply {
        addClassNames(LumoUtility.Margin.Bottom.XSMALL)
    }
    private val message = Paragraph().apply {
        addClassNames(
            LumoUtility.Margin.Top.NONE,
            LumoUtility.TextColor.SECONDARY
        )
    }

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        style["justify-content"] = "center"
        style["background"] = "var(--lumo-contrast-5pct)"

        loginForm.action = "login"
        message.text = AppMessages.translateCurrent("login.signInMessage")

        val card = VerticalLayout(heading, message, loginForm).apply {
            alignItems = FlexComponent.Alignment.STRETCH
            isSpacing = true
            isPadding = true
            maxWidth = "420px"
            style["background"] = "var(--lumo-base-color)"
            style["border-radius"] = "var(--lumo-border-radius-l)"
            style["box-shadow"] = "var(--lumo-box-shadow-s)"
        }

        add(card)
    }

    override fun localeChange(event: LocaleChangeEvent) {
        message.text = AppMessages.translate(event.locale, "login.signInMessage")
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        loginForm.isError = event.location.queryParameters.parameters.containsKey("error")
    }
}
