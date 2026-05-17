package com.kenjdavidson.golf.handicap.views

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.theme.lumo.LumoUtility

@Route("login")
@PageTitle("Login | Handicap Committee App")
@AnonymousAllowed
class LoginView : VerticalLayout(), BeforeEnterObserver {
    private val loginForm = LoginForm()

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER)
        style["justify-content"] = "center"
        style["background"] = "var(--lumo-contrast-5pct)"

        val heading = H1("⛳ Handicap Committee App").apply {
            addClassNames(LumoUtility.Margin.Bottom.XSMALL)
        }

        val message = Paragraph("Sign in with your Golf Canada email and password to access the committee workspace.").apply {
            addClassNames(
                LumoUtility.Margin.Top.NONE,
                LumoUtility.TextColor.SECONDARY
            )
        }

        loginForm.action = "login"

        val card = VerticalLayout(heading, message, loginForm).apply {
            setAlignItems(FlexComponent.Alignment.STRETCH)
            isSpacing = true
            isPadding = true
            maxWidth = "420px"
            style["background"] = "var(--lumo-base-color)"
            style["border-radius"] = "var(--lumo-border-radius-l)"
            style["box-shadow"] = "var(--lumo-box-shadow-s)"
        }

        add(card)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        loginForm.isError = event.location.queryParameters.parameters.containsKey("error")
    }
}
